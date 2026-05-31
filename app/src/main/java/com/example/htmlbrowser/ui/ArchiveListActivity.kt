package com.example.htmlbrowser.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.htmlbrowser.databinding.ActivityArchiveListBinding
import com.example.htmlbrowser.domain.model.HtmlArchive
import com.example.htmlbrowser.ui.adapters.ArchiveAdapter
import com.example.htmlbrowser.ui.viewmodels.ArchiveListViewModel
import com.example.htmlbrowser.ui.viewmodels.ImportState
import kotlinx.coroutines.launch

class ArchiveListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchiveListBinding
    private lateinit var viewModel: ArchiveListViewModel
    private lateinit var adapter: ArchiveAdapter

    private var progressDialog: ProgressDialog? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.importArchive(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openFilePicker()
        } else {
            Toast.makeText(
                this,
                "Storage permission is required to import archives",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ArchiveListViewModel::class.java]

        setupAdapter()
        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeArchives()
        observeImportState()
    }

    private fun setupAdapter() {
        adapter = ArchiveAdapter(
            onItemClick = { archive ->
                val intent = Intent(this, HtmlViewerActivity::class.java).apply {
                    putExtra("archive_id", archive.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { archive ->
                showDeleteConfirmationDialog(archive)
            }
        )
    }

    private fun setupRecyclerView() {
        binding.archiveList.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val archive = adapter.currentList[viewHolder.adapterPosition]
                showDeleteConfirmationDialog(archive)
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.archiveList)
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            checkPermissionAndOpenPicker()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: ACTION_OPEN_DOCUMENT doesn't need READ_EXTERNAL_STORAGE
            openFilePicker()
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
        filePickerLauncher.launch(intent)
    }

    private fun observeArchives() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.archives.collect { archives ->
                    adapter.submitList(archives)
                    binding.emptyView.visibility =
                        if (archives.isEmpty()) android.view.View.VISIBLE
                        else android.view.View.GONE
                }
            }
        }
    }

    private fun observeImportState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importState.collect { state ->
                    when (state) {
                        is ImportState.Idle -> {
                            // nothing to do
                        }
                        is ImportState.Loading -> {
                            showProgressDialog()
                        }
                        is ImportState.Success -> {
                            dismissProgressDialog()
                            viewModel.resetImportState()
                        }
                        is ImportState.Error -> {
                            dismissProgressDialog()
                            Toast.makeText(this@ArchiveListActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetImportState()
                        }
                    }
                }
            }
        }
    }

    private fun showProgressDialog() {
        if (progressDialog == null || progressDialog?.isShowing == false) {
            @Suppress("DEPRECATION")
            progressDialog = ProgressDialog(this).apply {
                setMessage("Importing archive…")
                isIndeterminate = true
                setCancelable(false)
                show()
            }
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showDeleteConfirmationDialog(archive: HtmlArchive) {
        AlertDialog.Builder(this)
            .setMessage("Delete this archive? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteArchive(archive)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
    }
}
