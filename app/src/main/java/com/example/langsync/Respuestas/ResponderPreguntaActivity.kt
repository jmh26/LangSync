package com.example.langsync

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.langsync.Respuestas.Respuesta
import com.example.langsync.Respuestas.RespuestasAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResponderPreguntaActivity : AppCompatActivity() {

    private lateinit var preguntaId: String
    private lateinit var autorId: String
    private lateinit var respuestasAdapter: RespuestasAdaptador
    private val listaRespuestas = mutableListOf<Respuesta>()
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_pregunta)

        // Obtener los datos de la pregunta
        preguntaId = intent.getStringExtra("PREGUNTA_ID") ?: ""
        val preguntaTexto = intent.getStringExtra("PREGUNTA_TEXTO") ?: ""
        val preguntaFecha = intent.getStringExtra("PREGUNTA_FECHA") ?: ""
        autorId = intent.getStringExtra("PREGUNTA_USER_ID") ?: ""

        // Mostrar los detalles de la pregunta
        val tvTextoPregunta = findViewById<TextView>(R.id.tv_texto)
        val tvFechaPregunta = findViewById<TextView>(R.id.tv_fecha)
        val ivImagenPregunta = findViewById<CircleImageView>(R.id.iv_imagenPregunta)

        tvTextoPregunta.text = preguntaTexto
        tvFechaPregunta.text = preguntaFecha

        // Cargar imagen del usuario que preguntó
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(autorId).get()
            .addOnSuccessListener { snapshot ->
                val urlFoto = snapshot.child("url_foto").value as? String
                urlFoto?.let {
                    Glide.with(this)
                        .load(it)
                        .placeholder(R.drawable.baseline_person_2_24)
                        .into(ivImagenPregunta)
                } ?: run {
                    ivImagenPregunta.setImageResource(R.drawable.baseline_person_2_24)
                }
            }

        // Verificar si el usuario es administrador
        verificarAdministrador()

        // Configurar RecyclerView
        val recyclerViewRespuestas = findViewById<RecyclerView>(R.id.recyclerViewRespuestas)
        respuestasAdapter = RespuestasAdaptador(listaRespuestas, this, esAdmin) { respuestaId ->
            marcarRespuestaComoDestacada(respuestaId)
        }
        recyclerViewRespuestas.layoutManager = LinearLayoutManager(this)
        recyclerViewRespuestas.adapter = respuestasAdapter

        // Cargar respuestas desde Firebase
        cargarRespuestas()

        // Configurar el envío de respuestas
        val etRespuesta = findViewById<EditText>(R.id.etRespuesta)
        val btnEnviarRespuesta = findViewById<Button>(R.id.btnEnviarRespuesta)
        btnEnviarRespuesta.setOnClickListener {
            enviarRespuesta(etRespuesta.text.toString())
        }
    }

    private fun verificarAdministrador() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            esAdmin = Utilidades.esAdmin(it.email ?: "", "administrador")
        }
    }

    private fun cargarRespuestas() {
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("LangSync").child("Preguntas").child(preguntaId).get()
            .addOnSuccessListener { snapshot ->
                listaRespuestas.clear()
                var respuestaDestacadaId: String? = snapshot.child("destacada").value as? String
                var respuestaDestacada: Respuesta? = null

                val respuestasSnapshot = snapshot.child("Respuestas")
                respuestasSnapshot.children.forEach { dataSnapshot ->
                    val respuesta = dataSnapshot.getValue(Respuesta::class.java)
                    if (respuesta?.id == respuestaDestacadaId) {
                        respuestaDestacada = respuesta
                    } else {
                        respuesta?.let { listaRespuestas.add(it) }
                    }
                }

                respuestaDestacada?.let {
                    listaRespuestas.add(0, it)
                }

                respuestasAdapter.actualizarEstrella(respuestaDestacadaId ?: "")
                respuestasAdapter.notifyDataSetChanged()
            }
    }

    private fun enviarRespuesta(textoRespuesta: String) {
        if (textoRespuesta.isNotBlank()) {
            val dbRef = FirebaseDatabase.getInstance().reference
            val respuestaId = dbRef.child("LangSync").child("Preguntas").child(preguntaId).child("Respuestas").push().key
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val respuesta = Respuesta(respuestaId, textoRespuesta, userId, fechaActual)
            respuestaId?.let {
                dbRef.child("LangSync").child("Preguntas").child(preguntaId).child("Respuestas").child(it).setValue(respuesta)
                    .addOnSuccessListener {
                        findViewById<EditText>(R.id.etRespuesta).text.clear()
                        dbRef.child("LangSync").child("Preguntas").child(preguntaId).child("respondida").setValue(true)
                        Toast.makeText(this, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                        cargarRespuestas()  // Recargar las respuestas después de enviar una
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al enviar respuesta", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Escribe una respuesta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun marcarRespuestaComoDestacada(respuestaId: String) {
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("LangSync").child("Preguntas").child(preguntaId).child("destacada").setValue(respuestaId)
            .addOnSuccessListener {
                Toast.makeText(this, "Respuesta destacada", Toast.LENGTH_SHORT).show()
                cargarRespuestas()  // Recargar las respuestas para reflejar el cambio
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al marcar respuesta como destacada", Toast.LENGTH_SHORT).show()
            }
    }
}
