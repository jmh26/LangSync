package com.example.langsync.Mensajes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.langsync.Chat.MensajeAdapter
import com.example.langsync.databinding.ActivityMensajesBinding
import com.example.langsync.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MensajesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMensajesBinding
    private lateinit var adapter: MensajeAdapter
    private val mensajesList = mutableListOf<Mensaje>()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID") ?: ""

        adapter = MensajeAdapter(this, mensajesList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.btnEnviar.setOnClickListener {
            enviarMensaje()
        }

        cargarMensajes()
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

    private fun cargarMensajes() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("LangSync").child("Mensajes")
            .orderByChild("de").equalTo(currentUserId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let {
                        if ((it.de == currentUserId && it.para == userId) || (it.de == userId && it.para == currentUserId)) {
                            mensajesList.add(it)
                            adapter.notifyItemInserted(mensajesList.size - 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

