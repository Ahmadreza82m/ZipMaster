package com.example.zipmaster.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zipmaster.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val files: List<File>,
    private val selectionMode: Boolean,
    private val selectedFiles: MutableSet<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val tvName: TextView = view.findViewById(R.id.tvFileName)
        val tvDetails: TextView = view.findViewById(R.id.tvFileDetails)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.tvName.text = file.name
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(file.lastModified()))
        
        if (file.isDirectory) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_gallery) // Replace with folder icon
            val count = file.listFiles()?.size ?: 0
            holder.tvDetails.text = "$count items | $dateStr"
            holder.cbSelect.visibility = View.GONE
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_save) // Replace with file icon
            val size = formatSize(file.length())
            holder.tvDetails.text = "$size | $dateStr"
            
            if (selectionMode) {
                holder.cbSelect.visibility = View.VISIBLE
                holder.cbSelect.isChecked = selectedFiles.contains(file)
                holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedFiles.add(file) else selectedFiles.remove(file)
                }
            } else {
                holder.cbSelect.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount() = files.size

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
