package com.example.witchesmeeting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.databinding.ItemReservedBinding


class ReserveAdapter() : RecyclerView.Adapter<ReserveAdapter.ViewHolder>() {

    interface OnRemoveClickListener {
        fun onRemoveClick(position: Int) {}
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
    }

    var removeClickListener: OnRemoveClickListener? = null
    var onItemClickListener: OnItemClickListener? = null

    fun updateIsClicked(position: Int, isClicked: Boolean) {
        if (position in 0 until reserveList.size) {
            reserveList[position].isClicked = isClicked
            notifyItemChanged(position)
        }
    }

    private var reserveList = mutableListOf<Reserve>()

    fun set(list: List<Reserve>) {
        reserveList.clear()
        reserveList.addAll(list)
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemReservedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun reserveBind(data: Reserve) = with(binding) {
            name.text = data.name
            department.text = data.department
            peopleNum.text = data.peopleNum.toString()
            start.text = data.start
            end.text = data.end
            contentsText.text = data.contents
            if (data.isMe) {
                removeBtn.isVisible = true
            }
            if (data.isClicked) {
                contentsText.isVisible = true
            }

        }

        init {
            binding.removeBtn.setOnClickListener {
                removeClickListener?.onRemoveClick(adapterPosition)
            }
            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReserveAdapter.ViewHolder =
        ViewHolder(ItemReservedBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = reserveList.size

    override fun onBindViewHolder(holder: ReserveAdapter.ViewHolder, position: Int) {
        holder.reserveBind(reserveList[position])
    }


}
