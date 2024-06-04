package com.example.langsync.Perfil

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.langsync.R
import com.example.langsync.databinding.FragmentPerfilBinding
import com.google.firebase.database.FirebaseDatabase

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID") ?: ""

        cargarInformacionPerfil()
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
                    .placeholder(R.drawable.baseline_person_2_24)
                    .into(binding.ivImagenPerfil)
            } ?: run {
                binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
            }
        }.addOnFailureListener {
            binding.tvNombre.text = "Error al cargar el nombre"
            binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
        }

        // Deshabilitar el botón de editar perfil
        binding.ivEditar.visibility = View.GONE
    }
}

