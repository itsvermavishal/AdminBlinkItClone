package com.example.adminblinkitclone.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkitclone.utils.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.utils.Utils
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.adapter.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.model.Product
import com.example.adminblinkitclone.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()

    private val viewModel: AdminViewModel by viewModels()

    private val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
        val fiveImages = listOfUri.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)
        binding.rvProductImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = AdapterSelectedImage(imageUris)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
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

            if (productTitle.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty() ||
                productPrice.isEmpty() || productStock.isEmpty() || productCategory.isEmpty() || productType.isEmpty()) {
                Utils.hideDialog()
                Utils.showToast(requireContext(), "Please fill all the fields")
            } else if (imageUris.isEmpty()) {
                Utils.hideDialog()
                Utils.showToast(requireContext(), "Please upload some images")
            } else {
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUID = Utils.getCurrentUserId(),
                    productRandomId = Utils.getRandomId()
                )
                saveImage(product)
            }
        }
    }

    private fun saveImage(product: Product) {
        viewModel.saveInDB(imageUris)
        lifecycleScope.launch {
            viewModel.isImagesUploaded.collect { uploaded ->
                if (uploaded) {
                    Utils.hideDialog()
                    Utils.showToast(requireContext(), "Image uploaded successfully")
                }
                getUrls(product)
            }
        }
    }

    private fun getUrls(product: Product){
        Utils.showDialog(requireContext(), "Publishing Product...")
        lifecycleScope.launch {
            viewModel.downloadedUrls.collect { urls ->
                val urls = ArrayList(urls)
                product.productImageUris = urls
                saveProduct(product)
            }
        }
    }

    private fun saveProduct(product: Product){
        viewModel.saveProduct(product)
        lifecycleScope.launch {
            viewModel.isProductSaved.collect { saved ->
                if (saved) {
                    Utils.hideDialog()
                    startActivity(Intent(requireContext(), AdminMainActivity::class.java))
                    Utils.showToast(requireContext(), "Product saved successfully")
                } else {
                    Utils.hideDialog()
                    Utils.showToast(requireContext(), "Something went wrong")
                }
            }
        }
    }

    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener {
            selectedImage.launch("image/*")
        }
    }

    private fun setAutoCompleteTextView() {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductsCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }

}
