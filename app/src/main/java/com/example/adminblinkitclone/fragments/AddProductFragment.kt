package com.example.adminblinkitclone.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.adminblinkitclone.Contants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.adapter.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.model.Product

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()
    val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
        val fiveImages = listOfUri.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)

        binding.rvProductImages.adapter = AdapterSelectedImage(imageUris)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAutoCompleteTextView()
        onImageSelectClicked()
        onAddButtonClicked()
        return binding.root

    }

    private fun onAddButtonClicked() {
        binding.btnAddProduct.setOnClickListener {
            Utils.showDialog(requireContext(), "Uploading Product...")
            val productTitle = binding.etProductTitle.text.toString()
            val productQuantity = binding.etProductQuantity.text.toString()
            val productUnit = binding.etProductUnit.text.toString()
            val productPrice = binding.etProductPrice.text.toString()
            val productStock = binding.etProductStock.text.toString()
            val productCategory = binding.etProductCategory.text.toString()
            val productType = binding.etProductType.text.toString()

            if (productTitle.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty() || productPrice.isEmpty() || productStock.isEmpty() || productCategory.isEmpty() || productType.isEmpty()){
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), "Please fill all the fields")
                }
            }
            else if(imageUris.isEmpty()){
                    Utils.apply {
                        hideDialog()
                        showToast(requireContext(), "Please upload some images")
                    }
            }
            else{
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUID = Utils.getCurrentUserId()
                )
                saveImage(product)
            }
        }
    }

    private fun saveImage(product : Product){

    }

    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener {
            selectedImage.launch("image/*")
        }
    }

    private fun setAutoCompleteTextView(){
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Contants.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Contants.allProductsCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Contants.allProductType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }

    private fun setStatusBarColor() {
        requireActivity().window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}