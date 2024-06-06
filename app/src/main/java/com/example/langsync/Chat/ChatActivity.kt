package com.example.langsync.Chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.langsync.R
import com.example.langsync.databinding.ActivityChatBinding
import com.example.langsync.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mensajesAdapter: MensajeAdapter
    private val mensajesList = mutableListOf<Mensaje>()
    private lateinit var userId: String
    private lateinit var nombreUsuario: String
    private lateinit var urlFotoUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID") ?: ""

        cargarInformacionUsuario()

        mensajesAdapter = MensajeAdapter(this, mensajesList)
        binding.recyclerViewMensajes.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = mensajesAdapter
        }

        binding.btnEnviar.setOnClickListener {
            enviarMensaje()
        }

        cargarMensajes()
    }

    private fun cargarInformacionUsuario() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(userId).get().addOnSuccessListener { snapshot ->
            nombreUsuario = snapshot.child("nombre").value as? String ?: "Usuario"
            urlFotoUsuario = snapshot.child("url_foto").value as? String ?: ""

            binding.tvProfileName.text = nombreUsuario
            if (urlFotoUsuario.isNotEmpty()) {
                Glide.with(this)
                    .load(urlFotoUsuario)
                    .placeholder(R.drawable.baseline_person_2_24)
                    .into(binding.ivProfileImage)
            } else {
                binding.ivProfileImage.setImageResource(R.drawable.baseline_person_2_24)
            }
        }
    }

    private fun cargarMensajes() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("LangSync").child("Mensajes")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajesList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val mensaje = dataSnapshot.getValue(Mensaje::class.java)
                        if ((mensaje?.de == currentUserId && mensaje.para == userId) || (mensaje?.de == userId && mensaje.para == currentUserId)) {
                            mensaje?.let { mensajesList.add(it) }
                        }
                    }
                    mensajesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores
                }
            })
    }

    private fun enviarMensaje() {
        val mensajeTexto = binding.etMensaje.text.toString()
        if (mensajeTexto.isNotEmpty()) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val databaseReference = FirebaseDatabase.getInstance().reference
            val mensajeId = databaseReference.child("LangSync").child("Mensajes").push().key

            val mensajeMap = mapOf(
                "de" to currentUserId,
                "para" to userId,
                "mensaje" to mensajeTexto,
                "timestamp" to System.currentTimeMillis()
            )

            mensajeId?.let {
                databaseReference.child("LangSync").child("Mensajes").child(it).setValue(mensajeMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etMensaje.text.clear()
                    }
                }
            }
        }
    }
}

