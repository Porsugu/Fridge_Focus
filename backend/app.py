from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)

# Configure database connection
DB_URI = "mysql+mysqlconnector://admin:biztech363team@fridgefocusbiztech.cjois86ewg9s.us-east-2.rds.amazonaws.com/fridgefocusbiztech"
app.config['SQLALCHEMY_DATABASE_URI'] = DB_URI
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Initialize the database
db = SQLAlchemy(app)

# Define the Inventory Table
class Inventory(db.Model):
    name = db.Column(db.String(100), primary_key=True)
    quantity = db.Column(db.String(100))
    unit = db.Column(db.String(100))

# Define the Recipes Table
class Recipes(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True)
    guide = db.Column(db.String(100000))
    url = db.Column(db.String(500))  # New field for recipe image URL
    
    # Define one-to-many relationship with Ingredients table
    ingredients = db.relationship('Ingredients', backref='recipe', lazy=True)

# Define the Ingredients Table
class Ingredients(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True)
    quantity = db.Column(db.Integer)  # For example, "2 cups", "1 tbsp", etc.
    unit = db.Column(db.String(100))
    
    # Foreign key to link to a specific recipe (one-to-many)
    recipe_id = db.Column(db.Integer, db.ForeignKey('recipes.id'), nullable=False)

# Route to create all tables the first time 
@app.route('/')
def index():
    try:
        # Test connection first
        connection = db.engine.connect()
        connection.close()
        print("Database connection successful!")
        
        # Create tables
        db.create_all()
        print("Tables created successfully!")
        return "Flask + Amazon RDS MySQL API is live!"
    except Exception as e:
        print(f"Error: {e}")
        return f"Error connecting to database: {e}", 500

@app.route('/inventory', methods=['GET'])
def get_inventory():
    inventory = Inventory.query.all()
    return jsonify([{'name': item.name, 'quantity': item.quantity, 'unit': item.unit} for item in inventory])

@app.route('/inventory/<string:name>', methods=['GET'])
def get_inventory_item(name):
    # Look up the item by name (now the primary key)
    item = Inventory.query.get_or_404(name)
    return jsonify({
        'name': item.name, 
        'quantity': item.quantity, 
        'unit': item.unit
    })

@app.route('/addInventory', methods=['POST'])
def create_inventory():
    data = request.get_json()
    
    # Validate required fields
    if not all(key in data for key in ['name', 'quantity', 'unit']):
        return jsonify({'error': 'Missing required fields'}), 400
    
    new_item = Inventory(
        name=data['name'],
        quantity=data['quantity'],
        unit=data['unit']
    )
    
    try:
        db.session.add(new_item)
        db.session.commit()
        return jsonify({
            'name': new_item.name,
            'quantity': new_item.quantity,
            'unit': new_item.unit
        }), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@app.route('/updateInventory/<string:name>', methods=['PUT'])
def update_inventory(name):
    item = Inventory.query.get_or_404(name)
    data = request.get_json()
    
    # Note: We can't update the name as it's now the primary key
    # If you need to change the name, you'd have to delete and recreate the item
    
    if 'quantity' in data:
        item.quantity = data['quantity']
    if 'unit' in data:
        item.unit = data['unit']
    
    try:
        db.session.commit()
        return jsonify({
            'name': item.name,
            'quantity': item.quantity,
            'unit': item.unit
        })
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@app.route('/deleteInventory/<string:name>', methods=['DELETE'])
def delete_inventory(name):
    item = Inventory.query.get_or_404(name)
    
    try:
        db.session.delete(item)
        db.session.commit()
        return jsonify({'message': f'Inventory item {name} deleted'})
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
   
# Recipe Routes
@app.route('/recipes', methods=['GET'])
def get_recipes():
    recipes = Recipes.query.all()
    # Include url in the response
    return jsonify([{
        'id': recipe.id, 
        'name': recipe.name, 
        'guide': recipe.guide,
        'url': recipe.url
    } for recipe in recipes])

@app.route('/recipes/<string:name>', methods=['GET'])
def get_recipe(name):
    # Look up the recipe by name instead of id
    recipe = Recipes.query.filter_by(name=name).first_or_404()
    
    # Get the ingredients for this recipe
    ingredients = Ingredients.query.filter_by(recipe_id=recipe.id).all()
    ingredients_list = [{'id': ing.id, 'name': ing.name, 'quantity': ing.quantity, 'unit': ing.unit} for ing in ingredients]
    
    return jsonify({
        'id': recipe.id,
        'name': recipe.name,
        'guide': recipe.guide,
        'url': recipe.url,  # Include url in the response
        'ingredients': ingredients_list
    })

@app.route('/addRecipes', methods=['POST'])
def create_recipe():
    data = request.get_json()
    
    # Validate required fields
    if not all(key in data for key in ['name', 'guide']):
        return jsonify({'error': 'Missing required fields'}), 400
    
    # Create new recipe (with url if provided)
    new_recipe = Recipes(
        name=data['name'],
        guide=data['guide'],
        url=data.get('url', '')  # Get the URL if provided, default to empty string
    )
    
    try:
        # Add recipe to get an ID
        db.session.add(new_recipe)
        db.session.commit()
        
        # Process ingredients if provided
        ingredients_added = []
        inventory_updates = []
        deleted_ingredients = []
        
        if 'ingredients' in data and isinstance(data['ingredients'], list):
            for ingredient_data in data['ingredients']:
                # Validate ingredient data
                if not all(key in ingredient_data for key in ['name', 'quantity', 'unit']):
                    continue  # Skip invalid ingredients
                
                # Check if ingredient exists in inventory
                inventory_item = Inventory.query.get(ingredient_data['name'])
                
                if inventory_item:
                    # Convert quantities to numbers for comparison
                    try:
                        inventory_qty = float(inventory_item.quantity)
                        recipe_qty = float(ingredient_data['quantity'])
                        
                        # Check if we have enough in inventory
                        if inventory_qty >= recipe_qty:
                            # Update inventory quantity
                            new_qty = inventory_qty - recipe_qty
                            
                            # If new quantity is zero, delete the inventory item
                            if new_qty == 0:
                                deleted_item = {
                                    'name': inventory_item.name,
                                    'quantity': inventory_item.quantity,
                                    'unit': inventory_item.unit
                                }
                                db.session.delete(inventory_item)
                                deleted_ingredients.append(deleted_item)
                            else:
                                # Otherwise update the quantity
                                inventory_item.quantity = str(new_qty)
                                inventory_updates.append({
                                    'name': inventory_item.name,
                                    'old_quantity': str(inventory_qty),
                                    'new_quantity': str(new_qty),
                                    'unit': inventory_item.unit
                                })
                        else:
                            # Not enough in inventory - delete the inventory item completely
                            deleted_item = {
                                'name': inventory_item.name,
                                'quantity': inventory_item.quantity,
                                'unit': inventory_item.unit
                            }
                            db.session.delete(inventory_item)
                            deleted_ingredients.append(deleted_item)
                    except ValueError:
                        # Handle case where quantities aren't numbers
                        return jsonify({'error': f'Invalid quantity format for {ingredient_data["name"]}'}), 400
                # If ingredient not found in inventory, we simply ignore it
                
                # Create ingredient with recipe_id from the newly created recipe
                new_ingredient = Ingredients(
                    name=ingredient_data['name'],
                    quantity=ingredient_data['quantity'],
                    unit=ingredient_data['unit'],
                    recipe_id=new_recipe.id
                )
                
                db.session.add(new_ingredient)
                ingredients_added.append({
                    'name': new_ingredient.name,
                    'quantity': new_ingredient.quantity,
                    'unit': new_ingredient.unit
                })
        
        # Commit all changes
        db.session.commit()
        
        # Prepare response
        response = {
            'id': new_recipe.id,
            'name': new_recipe.name,
            'guide': new_recipe.guide,
            'url': new_recipe.url,  # Include url in the response
            'ingredients': ingredients_added
        }
        
        # Add inventory update info to response if any
        if inventory_updates:
            response['inventory_updates'] = inventory_updates
        if deleted_ingredients:
            response['deleted_ingredients'] = deleted_ingredients
        
        return jsonify(response), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@app.route('/recipes/<int:id>', methods=['DELETE'])
def delete_recipe(id):
    recipe = Recipes.query.get_or_404(id)
    
    # Delete all associated ingredients first
    Ingredients.query.filter_by(recipe_id=id).delete()
    
    try:
        db.session.delete(recipe)
        db.session.commit()
        return jsonify({'message': f'Recipe {id} deleted'})
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

# Ingredients Routes
@app.route('/ingredients', methods=['GET'])
def get_ingredients():
    ingredients = Ingredients.query.all()
    return jsonify([
        {
            'id': ingredient.id,
            'name': ingredient.name,
            'quantity': ingredient.quantity,
            'unit': ingredient.unit,
            'recipe_id': ingredient.recipe_id
        } for ingredient in ingredients
    ])

# Get all ingredients for a specific recipe
@app.route('/recipes/<string:recipe_name>/ingredients', methods=['GET'])
def get_recipe_ingredients(recipe_name):
    # Find the recipe by name
    recipe = Recipes.query.filter_by(name=recipe_name).first_or_404()
    
    # Get all ingredients with this recipe's ID
    ingredients = Ingredients.query.filter_by(recipe_id=recipe.id).all()
    
    return jsonify([
        {
            'id': ingredient.id,
            'name': ingredient.name,
            'quantity': ingredient.quantity,
            'unit': ingredient.unit,
            'recipe_id': ingredient.recipe_id
        } for ingredient in ingredients
    ])
 
# Start the Flask application
if __name__ == '__main__':
    app.run(debug=True)