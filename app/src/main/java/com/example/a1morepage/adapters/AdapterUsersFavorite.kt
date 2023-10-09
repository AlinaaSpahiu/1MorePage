package com.example.a1morepage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1morepage.R
import com.example.a1morepage.models.ModelUser
import com.example.a1morepage.databinding.RowUserFavoriteBinding

class AdapterUsersFavorite(private val context: Context, private val usersList: ArrayList<ModelUser>) : RecyclerView.Adapter<AdapterUsersFavorite.HolderUserFavorite>() {

    private lateinit var binding: RowUserFavoriteBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUserFavorite {
        binding = RowUserFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderUserFavorite(binding.root)
    }

    override fun getItemCount(): Int = usersList.size

    override fun onBindViewHolder(holder: HolderUserFavorite, position: Int) {
        val user = usersList[position]

        holder.userNameTextView.text = user.name
        Glide.with(context)
            .load(user.profileImage)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.userProfileImageView)
    }

    inner class HolderUserFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImageView = binding.userProfileImageView
        val userNameTextView = binding.userNameTextView
    }
}
