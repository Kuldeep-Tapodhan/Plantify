package com.example.plantify.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plantify.R
import com.example.plantify.data.HistoryItem
import com.example.plantify.databinding.ItemHistoryScanBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryScanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // Set the disease name
            diseaseNameText.text = historyItem.diseaseName

            // Format and set the date
            historyItem.timestamp?.let {
                scanDateText.text = formatTimestamp(it)
            }

            // Set the status chip text and color
            if (historyItem.type == "Healthy") {
                statusChip.text = "Healthy"
                statusChip.setChipBackgroundColorResource(R.color.chip_green_bg)
                statusChip.setTextColor(ContextCompat.getColor(context, R.color.chip_green_text))
            } else {
                statusChip.text = "Diseased"
                statusChip.setChipBackgroundColorResource(R.color.chip_red_bg)
                statusChip.setTextColor(ContextCompat.getColor(context, R.color.chip_red_text))
            }
        }
    }

    override fun getItemCount(): Int = historyList.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}