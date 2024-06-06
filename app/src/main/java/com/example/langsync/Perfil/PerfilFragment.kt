package com.example.langsync.Perfil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.langsync.Mensajes.EnviarMensajeActivity
import com.example.langsync.Preguntas.AnadirPregunta
import com.example.langsync.R
import com.example.langsync.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var sharedPref: SharedPreferences
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        userId = arguments?.getString("USER_ID") ?: currentUserId
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        cargarInformacionPerfil()
        setupModo()

        binding.ivEditar.setOnClickListener {
            val intent = Intent(activity, EditarPerfil::class.java)
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }

        binding.ivModo.setOnClickListener {
            toggleNightMode()
        }

        binding.ivMensaje.setOnClickListener {
            enviarMensaje()
        }

        binding.fabAddpregunta.setOnClickListener {
            val intent = Intent(activity, AnadirPregunta::class.java)
            startActivity(intent)
        }

        // Mostrar u ocultar botones según el perfil
        if (userId == currentUserId) {
            binding.ivEditar.visibility = View.VISIBLE
            binding.ivMensaje.visibility = View.GONE
        } else {
            binding.ivEditar.visibility = View.GONE
            binding.ivMensaje.visibility = View.VISIBLE
        }
    }

    private fun cargarInformacionPerfil() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(userId).get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombre").value as? String
            val urlFoto = snapshot.child("url_foto").value as? String
            val idiomaNativo = snapshot.child("idiomaNativo").value as? String
            val idiomasInteres = snapshot.child("idiomasInteres").value as? String

            if (_binding != null) {
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
            }
        }.addOnFailureListener {
            if (_binding != null) {
                binding.tvNombre.text = "Error al cargar el nombre"
                binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
            }
        }
    }

    private fun setupModo() {
        val isNightMode = sharedPref.getBoolean("NIGHT_MODE", false)
        updateMode(isNightMode)
    }

    private fun toggleNightMode() {
        val isNightMode = sharedPref.getBoolean("NIGHT_MODE", false)
        val newMode = !isNightMode
        with(sharedPref.edit()) {
            putBoolean("NIGHT_MODE", newMode)
            apply()
        }
        updateMode(newMode)
        // Recargar el fragmento actual
        reloadFragment()
    }

    private fun updateMode(isNightMode: Boolean) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.ivModo.setImageResource(R.drawable.baseline_nightlight_24) // Reemplaza con el ícono de luna
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.ivModo.setImageResource(R.drawable.baseline_wb_sunny_24) // Reemplaza con el ícono de sol
        }
    }

    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
    }

    private fun enviarMensaje() {
        // Lógica para enviar un mensaje
        val intent = Intent(activity, EnviarMensajeActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, requestCode, data)
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
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
