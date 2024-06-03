package com.example.langsync.Perfil

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.langsync.Preguntas.AnadirPregunta
import com.example.langsync.R
import com.example.langsync.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarInformacionPerfil()

        binding.ivEditar.setOnClickListener {
            val intent = Intent(activity, EditarPerfil::class.java)
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }

        binding.fabAddpregunta.setOnClickListener {
            val intent = Intent(activity, AnadirPregunta::class.java)
            startActivity(intent)
        }
    }

    private fun cargarInformacionPerfil() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(userId).get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombre").value as? String
            val urlFoto = snapshot.child("url_foto").value as? String

            binding.tvNombre.text = nombre ?: "Nombre no encontrado"

            urlFoto?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.baseline_person_2_24) // Coloca el drawable de tu imagen predeterminada
                    .into(binding.ivImagenPerfil)
            } ?: run {
                binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24) // Coloca el drawable de tu imagen predeterminada
            }
        }.addOnFailureListener {
            binding.tvNombre.text = "Error al cargar el nombre"
            binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24) // Coloca el drawable de tu imagen predeterminada
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // Actualizar la información del perfil
            cargarInformacionPerfil()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_EDIT_PROFILE = 1001
    }
}

