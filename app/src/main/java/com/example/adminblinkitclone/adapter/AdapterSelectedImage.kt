package com.example.adminblinkitclone.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkitclone.databinding.ItemViewImageSectionBinding

class AdapterSelectedImage(val imageUris: ArrayList<Uri>) : RecyclerView.Adapter<AdapterSelectedImage.SelectedImageViewHolder>() {


    class SelectedImageViewHolder (val binding: ItemViewImageSectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedImageViewHolder {
        return SelectedImageViewHolder(ItemViewImageSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: SelectedImageViewHolder,
        position: Int
    ) {
        val image = imageUris[position]
        holder.binding.apply {
            ivImage.setImageURI(image)
        }

        holder.binding.closeButton.setOnClickListener {
            if (position < imageUris.size){
                imageUris.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

}