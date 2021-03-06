package kr.co.chat.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.chat.R
import kr.co.chat.databinding.ItemViewpagerViewholderBinding
import kr.co.chat.home.entity.ItemEntity
import java.text.DecimalFormat

class ViewPagerAdapter(val itemClicked : (ItemEntity) -> Unit) : ListAdapter<ItemEntity,ViewPagerAdapter.ItemViewHolder>(diffUtil) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
       return ItemViewHolder(ItemViewpagerViewholderBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        holder.bind(currentList[position])

    }

    inner class ItemViewHolder(private val binding : ItemViewpagerViewholderBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(itemEntity: ItemEntity) = with(binding) {
            titleTextView.text = itemEntity.title
            priceTextView.text = "${DecimalFormat("###,###").format(itemEntity.price.toLong())} 원"
            Glide.with(thumbnailImageView.context).load(itemEntity.imageUrl).into(thumbnailImageView)

            root.setOnClickListener { itemClicked(itemEntity) }

        }
    }


    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<ItemEntity>() {

            override fun areItemsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
                    return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
                    return  oldItem == newItem
            }
        }

    }

}