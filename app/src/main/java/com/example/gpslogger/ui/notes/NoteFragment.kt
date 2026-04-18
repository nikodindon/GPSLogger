package com.example.gpslogger.ui.notes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpslogger.R
import com.example.gpslogger.databinding.FragmentNoteBinding

class NoteFragment : Fragment() {
    
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NoteCaptureViewModel by viewModels()
    private lateinit var photoAdapter: NotePhotoAdapter
    
    private val permissionRequestCode = 1001
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        photoAdapter = NotePhotoAdapter(emptyList())
        binding.photoGallery.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.currentPhotoPath.observe(viewLifecycleOwner, Observer { path ->
            path?.let {
                // Photo captured, add to note
                viewModel.addPhotoToNote(it)
                photoAdapter = NotePhotoAdapter(viewModel.noteWithPhotos.value?.photoUris ?: emptyList())
                binding.photoGallery.adapter = photoAdapter
            }
        })
        
        viewModel.noteWithPhotos.observe(viewLifecycleOwner, Observer { note ->
            note?.let {
                // Update UI with note data
                updateNoteUI(it)
            }
        })
    }
    
    private fun setupListeners() {
        binding.btnAddPhoto.setOnClickListener {
            checkAndRequestPermissions()
        }
        
        binding.btnSaveNote.setOnClickListener {
            viewModel.saveNote()
            Toast.makeText(context, "Note saved!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                permissionRequestCode
            )
        } else {
            viewModel.dispatchTakePictureIntent(requireContext())
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.dispatchTakePictureIntent(requireContext())
            } else {
                Toast.makeText(context, "Permissions required!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateNoteUI(note: NoteWithPhotos) {
        binding.apply {
            // Update title, description, etc.
            
            // Update photo gallery
            photoAdapter = NotePhotoAdapter(note.photoUris)
            photoGallery.adapter = photoAdapter
            photoGallery.visibility = if (note.photoUris.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}