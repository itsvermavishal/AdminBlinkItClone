package com.example.adminblinkitclone.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapter.AdapterOrders
import com.example.adminblinkitclone.databinding.FragmentOrderBinding
import com.example.adminblinkitclone.model.OrderedItem
import com.example.adminblinkitclone.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class OrderFragment : Fragment() {
    private lateinit var binding: FragmentOrderBinding
    private val viewModel : AdminViewModel by viewModels()
    private lateinit var adapterOrders: AdapterOrders

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOrderBinding.inflate(layoutInflater)
        setStatusBarColor()
        getAllOrders()
        return binding.root
    }

    private fun getAllOrders() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getAllProducts().collect{orderList ->
                if (orderList.isNotEmpty()){
                    val orderedList = ArrayList<OrderedItem>()
                    for (orders in orderList){
                        val title = StringBuilder()
                        var totalPrice = 0
                        for (products in orders.orderList!!){
                            val price = products.productPrice
                                ?.replace("₹", "")
                                ?.trim()
                                ?.toIntOrNull() ?: 0

                            val quantity = products.productCount ?: 1

                            totalPrice += price * quantity

                            title.append("${products.productCategory}, ")
                        }

                        val orderedItem = OrderedItem(orders.orderId, title.toString(), totalPrice, orders.orderDate, orders.orderStatus, orders.userAddress)
                        orderedList.add(orderedItem)
                    }
                    adapterOrders = AdapterOrders(requireContext(), ::onOrderItemViewClicked)
                    binding.rvOrders.adapter = adapterOrders
                    adapterOrders.differ.submitList(orderedList)
                    binding.shimmerViewContainer.visibility = View.GONE
                }
            }
        }
    }

    fun onOrderItemViewClicked(orderedItems: OrderedItem){
        val bundle = Bundle()
        bundle.putInt("status", orderedItems.itemStatus!!)
        bundle.putString("orderId", orderedItems.orderId)
        bundle.putString("userAddress", orderedItems.userAddress)
        findNavController().navigate(R.id.action_orderFragment_to_orderDetailFragment, bundle)

    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.orange)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}