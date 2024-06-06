package com.example.langsync.Perfil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.langsync.Chat.ChatActivity
import com.example.langsync.Login
import com.example.langsync.Preguntas.AnadirPregunta
import com.example.langsync.R
import com.example.langsync.databinding.FragmentPerfilBinding
import com.example.langsync.model.TranslationResponse
import com.example.langsync.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Semaphore

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var sharedPref: SharedPreferences
    private lateinit var userId: String
    private val semaphore = Semaphore(1)  // Semáforo para controlar el acceso a la carga de información

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

        binding.fabLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }

        binding.btnTranslate.setOnClickListener {
            val textToTranslate = binding.etTranslateText.text.toString()
            val targetLanguage = binding.spinnerLanguages.selectedItem.toString()
            if (textToTranslate.isNotEmpty()) {
                translateText(textToTranslate, targetLanguage)
            } else {
                Toast.makeText(requireContext(), "Enter text to translate", Toast.LENGTH_SHORT).show()
            }
        }

        setupLanguageSpinner()

        // Mostrar u ocultar botones según el perfil
        if (userId == currentUserId) {
            binding.ivEditar.visibility = View.VISIBLE
            binding.ivMensaje.visibility = View.GONE
        } else {
            binding.ivEditar.visibility = View.GONE
            binding.ivMensaje.visibility = View.VISIBLE
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("en", "es", "fr", "de") // Add other supported languages
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguages.adapter = adapter
    }

    private fun translateText(text: String, targetLanguage: String) {
        val sourceLanguage = "es" // Cambia esto según el idioma de origen, si es necesario
        RetrofitClient.instance.translate(text, targetLanguage, sourceLanguage).enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (response.isSuccessful) {
                    val translation = response.body()
                    translation?.let {
                        binding.tvTranslationResult.text = it.data.translations[0].translatedText
                    }
                } else {
                    Toast.makeText(requireContext(), "Translation failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarInformacionPerfil() {
        CoroutineScope(Dispatchers.IO).launch {
            semaphore.acquire()  // Adquirir semáforo antes de cargar la información
            try {
                val databaseReference = FirebaseDatabase.getInstance().reference
                val snapshot = databaseReference.child("LangSync").child("Usuarios").child(userId).get().await()
                withContext(Dispatchers.Main) {
                    val nombre = snapshot.child("nombre").value as? String
                    val urlFoto = snapshot.child("url_foto").value as? String
                    val idiomaNativo = snapshot.child("idiomaNativo").value as? String
                    val idiomasInteres = snapshot.child("idiomasInteres").value as? String

                    if (_binding != null) {
                        binding.tvNombre.text = nombre ?: "Nombre no encontrado"
                        binding.idiomaHablado.text = idiomaNativo ?: "Idioma nativo no encontrado"
                        binding.idiomas.text = idiomasInteres ?: "Idiomas de interés no encontrados"

                        urlFoto?.let {
                            Glide.with(this@PerfilFragment)
                                .load(it)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .into(binding.ivImagenPerfil)
                        } ?: run {
                            binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding != null) {
                        binding.tvNombre.text = "Error al cargar el nombre"
                        binding.ivImagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                    }
                }
            } finally {
                semaphore.release()  // Liberar semáforo después de cargar la información
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
        val intent = Intent(activity, ChatActivity::class.java)
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
