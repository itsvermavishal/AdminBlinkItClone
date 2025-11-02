package com.example.adminblinkitclone.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.databinding.FragmentSignInBinding

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        getUserNumber()
        onContinueClick()
        return binding.root
    }
    private fun onContinueClick() {
        binding.btnContinue.setOnClickListener {
            try {
                val number = binding.etUserInput.text.toString()
                if (!number.matches(Regex("^\\d{10}$"))) {
                    Utils.showToast(requireContext(), "Please enter a valid 10-digit number")
                } else {
                    val bundle = Bundle().apply {
                        putString("number", number)
                    }
                    findNavController().navigate(R.id.action_signInFragment_to_OTPFragment, bundle)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showToast(requireContext(), "Navigation failed: ${e.message}")
            }
        }
    }
    private fun getUserNumber() {
        binding.etUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val len = number?.length

                if (len == 10) {
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

                }
                else{
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greyish_blue))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
}