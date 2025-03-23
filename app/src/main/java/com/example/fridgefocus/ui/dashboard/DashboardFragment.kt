    package com.example.fridgefocus.ui.dashboard

    import Item
    import android.content.Intent
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.TextView
    import androidx.constraintlayout.widget.ConstraintLayout
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.ViewModelProvider
    import com.example.fridgefocus.R
    import com.example.fridgefocus.databinding.FragmentDashboardBinding
    import com.example.fridgefocus.mealDetail

    class DashboardFragment : Fragment() {

        private var _binding: FragmentDashboardBinding? = null

        // This property is only valid between onCreateView and
        // onDestroyView.
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            val root: View = binding.root

            return root
        }

        // This should be retrieved from the Gemini API, the not yet saved state. Where is the button to add the recipe?
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val button = view.findViewById<ConstraintLayout>(R.id.constraintLayout5)
            button.setOnClickListener {
                val intent = Intent(requireContext(), mealDetail::class.java)
                val mealName = "Ketchup Butter"
                intent.putExtra("mealName", mealName)

                val itemList = arrayListOf(
                    Item("Butter", 500, "g"),
                    Item("Ketchup", 20, "tsp")
                )
                intent.putParcelableArrayListExtra("itemList", itemList)

                val recipe = "Get a block of butter from your fridge._Cut a perfect 500g block of it._Put it onto a luxury plate._Add 20 tsp of ketchup._Enjoy!"
                intent.putExtra("recipe",recipe)

                startActivity(intent)
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }