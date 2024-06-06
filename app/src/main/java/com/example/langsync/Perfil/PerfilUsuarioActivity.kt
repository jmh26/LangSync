package com.example.langsync.Perfil

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.langsync.Chat.ChatActivity
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

        setupClickListeners()
    }

    private fun cargarInformacionPerfil() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(userId).get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombre").value as? String
            val urlFoto = snapshot.child("url_foto").value as? String
            val idiomaNativo = snapshot.child("idiomaNativo").value as? String
            val idiomasInteres = snapshot.child("idiomasInteres").value as? String

            binding.tvNombre.text = nombre ?: "Nombre no encontrado"
            binding.idiomaHablado.text = idiomaNativo ?: "Idioma nativo no encontrado"
            binding.idiomas.text = idiomasInteres ?: "Idiomas de interés no encontrados"

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

    private fun setupClickListeners() {
        binding.ivModo.setOnClickListener {
            toggleNightMode()
        }

        binding.ivMensaje.setOnClickListener {
            enviarMensaje()
        }
    }

    private fun toggleNightMode() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val newMode = if (isNightMode) {
            android.content.res.Configuration.UI_MODE_NIGHT_NO
        } else {
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun enviarMensaje() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }
}

