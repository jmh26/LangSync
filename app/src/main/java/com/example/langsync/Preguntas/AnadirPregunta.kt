package com.example.langsync.Preguntas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.langsync.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnadirPregunta : AppCompatActivity() {

    private lateinit var etPregunta: EditText
    private lateinit var spinnerIdiomaPregunta: Spinner
    private lateinit var fabCrearPregunta: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_pregunta)

        etPregunta = findViewById(R.id.et_pregunta)
        spinnerIdiomaPregunta = findViewById(R.id.spinner_idioma_pregunta)
        fabCrearPregunta = findViewById(R.id.fab_crearPregunta)

        setupSpinner()

        fabCrearPregunta.setOnClickListener {
            val preguntaTexto = etPregunta.text.toString()
            val idiomaPregunta = spinnerIdiomaPregunta.selectedItem.toString()

            if (preguntaTexto.isNotBlank() && idiomaPregunta.isNotBlank()) {
                enviarPregunta(preguntaTexto, idiomaPregunta)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val dbRefUsuarios = FirebaseDatabase.getInstance().getReference("LangSync").child("Usuarios").child(userId!!)

        dbRefUsuarios.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val idiomasInteres = snapshot.child("idiomasInteres").getValue(String::class.java)?.split(", ") ?: emptyList()
                val adapter = ArrayAdapter(this@AnadirPregunta, android.R.layout.simple_spinner_item, idiomasInteres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerIdiomaPregunta.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AnadirPregunta, "Error al cargar los idiomas de interés", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enviarPregunta(textoPregunta: String, idiomaPregunta: String) {
        val dbRef = FirebaseDatabase.getInstance().reference
        val preguntaId = dbRef.child("LangSync").child("Preguntas").push().key
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val pregunta = Pregunta(preguntaId, textoPregunta, userId, fechaActual, null, null, idiomaPregunta, respondida = false)

        preguntaId?.let {
            dbRef.child("LangSync").child("Preguntas").child(it).setValue(pregunta)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pregunta añadida correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al añadir la pregunta", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
