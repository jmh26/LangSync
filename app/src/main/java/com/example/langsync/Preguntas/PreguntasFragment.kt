package com.example.langsync.Preguntas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.ResponderPreguntaActivity
import com.example.langsync.Respuestas.PreguntaClickListener
import com.example.langsync.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PreguntasFragment : Fragment(), PreguntaClickListener {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: PreguntasAdaptador
    private lateinit var lista: MutableList<Pregunta>
    private lateinit var fabAddPregunta: FloatingActionButton

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        lista = mutableListOf()
        adaptador = PreguntasAdaptador(lista, this) // Cambiar a 'this' para pasar el fragmento como contexto
        recyclerView = binding.recyclerViewPreguntas
        recyclerView.adapter = adaptador
        recyclerView.layoutManager = LinearLayoutManager(context)

        fabAddPregunta = binding.fabAddpregunta
        fabAddPregunta.setOnClickListener {
            val intent = Intent(activity, AnadirPregunta::class.java)
            startActivity(intent)
        }

        obtenerPreguntasDesdeFirebase()

        return rootView
    }

    private fun obtenerPreguntasDesdeFirebase() {
        val dbRefPreguntas: DatabaseReference = FirebaseDatabase.getInstance().getReference("LangSync").child("Preguntas")
        val dbRefUsuarios: DatabaseReference = FirebaseDatabase.getInstance().getReference("LangSync").child("Usuarios")

        dbRefPreguntas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    lista.clear()
                    val userIds = mutableSetOf<String>()
                    for (preguntaSnapshot in snapshot.children) {
                        val pregunta = preguntaSnapshot.getValue(Pregunta::class.java)
                        pregunta?.let {
                            lista.add(it)
                            it.userId?.let { userId ->
                                userIds.add(userId)
                            }
                        }
                    }
                    userIds.forEach { userId ->
                        dbRefUsuarios.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(usuarioSnapshot: DataSnapshot) {
                                if (usuarioSnapshot.exists()) {
                                    val nombre = usuarioSnapshot.child("nombre").getValue(String::class.java)
                                    val urlFoto = usuarioSnapshot.child("url_foto").getValue(String::class.java)
                                    lista.filter { pregunta -> pregunta.userId == userId }
                                        .forEach { pregunta ->
                                            pregunta.nombre = nombre
                                            pregunta.imagenPerfil = urlFoto
                                        }
                                    adaptador.notifyDataSetChanged()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("PreguntasFragment", "Error al obtener información del usuario: ${error.message}")
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al obtener las preguntas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPreguntaClick(pregunta: Pregunta) {
        val intent = Intent(context, ResponderPreguntaActivity::class.java)
        intent.putExtra("PREGUNTA_ID", pregunta.id)
        intent.putExtra("PREGUNTA_TEXTO", pregunta.texto)
        intent.putExtra("PREGUNTA_FECHA", pregunta.fecha)
        intent.putExtra("PREGUNTA_USER_ID", pregunta.userId)
        startActivity(intent)
    }

}
