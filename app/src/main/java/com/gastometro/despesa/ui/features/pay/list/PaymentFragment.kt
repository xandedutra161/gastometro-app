package com.gastometro.despesa.ui.features.pay.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gastometro.despesa.databinding.FragmentPaymentBinding
import com.gastometro.despesa.ui.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideNavigation()
        listerBtnOk()
    }

    private fun listerBtnOk() {
        val receveidText = arguments?.getString("stringPayment")
        binding.btnOk.setOnClickListener {
            showNavigation()
            val action = PaymentFragmentDirections.actionPaymentFragmentToPayFragment(receveidText)
            findNavController().navigate(action)

        }
    }

    override fun onDestroy() {
        showNavigation()
        super.onDestroy()
        _binding = null
    }

    private fun showNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.showBottomNavigation() // Chamar o método para mostrar a BottomNavigationView
    }

    private fun hideNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.hideBottomNavigation() // Chamada para um método na MainActivity
    }

}