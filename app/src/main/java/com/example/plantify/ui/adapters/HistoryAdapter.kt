package com.example.plantify.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plantify.R
import com.example.plantify.data.Scan
import com.example.plantify.databinding.ItemHistoryScanBinding

class HistoryAdapter(private val scans: List<Scan>) :
    RecyclerView.Adapter<HistoryAdapter.ScanViewHolder>() {

    inner class ScanViewHolder(val binding: ItemHistoryScanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding =
            ItemHistoryScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scan = scans[position]
        with(holder.binding) {
            diseaseNameText.text = scan.diseaseName
            scanDateText.text = scan.date
            Glide.with(root.context).load(scan.imageUrl).into(scanImage)

            if (scan.isHealthy) {
                statusChip.text = "Healthy"
                statusChip.setChipBackgroundColorResource(R.color.chip_green_bg)
                statusChip.setTextColor(ContextCompat.getColor(root.context, R.color.chip_green_text))
            } else {
                statusChip.text = "Diseased"
                statusChip.setChipBackgroundColorResource(R.color.chip_red_bg)
                statusChip.setTextColor(ContextCompat.getColor(root.context, R.color.chip_red_text))
            }
        }
    }

    override fun getItemCount() = scans.size
}
