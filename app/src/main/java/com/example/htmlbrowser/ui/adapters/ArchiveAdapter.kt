package com.example.htmlbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.htmlbrowser.databinding.ItemArchiveBinding
import com.example.htmlbrowser.domain.model.HtmlArchive
import com.example.htmlbrowser.utils.DateUtils
import com.example.htmlbrowser.utils.FileSizeUtils

class ArchiveAdapter(
    private val onItemClick: (HtmlArchive) -> Unit,
    private val onDeleteClick: (HtmlArchive) -> Unit
) : ListAdapter<HtmlArchive, ArchiveAdapter.ArchiveViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HtmlArchive>() {
            override fun areItemsTheSame(oldItem: HtmlArchive, newItem: HtmlArchive): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HtmlArchive, newItem: HtmlArchive): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding = ItemArchiveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArchiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArchiveViewHolder(private val binding: ItemArchiveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(archive: HtmlArchive) {
            binding.title.text = archive.title
            binding.size.text = FileSizeUtils.formatBytes(archive.sizeBytes)
            binding.date.text = DateUtils.formatImportDate(archive.importDate)
            binding.root.setOnClickListener { onItemClick(archive) }
            binding.btnDelete.setOnClickListener { onDeleteClick(archive) }
        }
    }
}
