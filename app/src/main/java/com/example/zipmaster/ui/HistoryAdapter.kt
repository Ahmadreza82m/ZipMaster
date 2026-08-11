package com.example.zipmaster.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zipmaster.R
import com.example.zipmaster.data.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        private val tvDetails: TextView = view.findViewById(R.id.tvDetails)
        private val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(item: HistoryItem) {
            tvFileName.text = item.fileName
            val type = if (item.operationType == "EXTRACT") "Extracted" else "Compressed"
            tvDetails.text = "$type • ${sdf.format(Date(item.timestamp))}"
            
            ivStatus.setImageResource(
                if (item.isSuccess) android.R.drawable.presence_online 
                else android.R.drawable.presence_busy
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem) = oldItem == newItem
    }
}
