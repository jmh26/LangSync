package com.example.langsync.Preguntas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.ResponderPreguntaActivity
import com.example.langsync.Respuestas.PreguntaClickListener
import com.example.langsync.databinding.FragmentPreguntasInteresBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PreguntasInteresFragment : Fragment() {

    private var _binding: FragmentPreguntasInteresBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: PreguntasAdaptador
    private lateinit var lista: MutableList<Pregunta>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreguntasInteresBinding.inflate(inflater, container, false)
        val rootView = binding.root

        lista = mutableListOf()
        adaptador = PreguntasAdaptador(lista, object : PreguntaClickListener {
            override fun onPreguntaClick(pregunta: Pregunta) {
                val intent = Intent(activity, ResponderPreguntaActivity::class.java)
                intent.putExtra("PREGUNTA_ID", pregunta.id)
                intent.putExtra("PREGUNTA_TEXTO", pregunta.texto)
                intent.putExtra("PREGUNTA_FECHA", pregunta.fecha)
                intent.putExtra("PREGUNTA_USER_ID", pregunta.userId)
                startActivity(intent)
            }
        })
        recyclerView = binding.recyclerViewPreguntas
        recyclerView.adapter = adaptador
        recyclerView.layoutManager = LinearLayoutManager(context)

        obtenerPreguntasDesdeFirebase()

        return rootView
    }

    private fun obtenerPreguntasDesdeFirebase() {
        val dbRefPreguntas: DatabaseReference = FirebaseDatabase.getInstance().getReference("LangSync").child("Preguntas")
        val dbRefUsuarios: DatabaseReference = FirebaseDatabase.getInstance().getReference("LangSync").child("Usuarios")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        dbRefUsuarios.child(currentUserId ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {
                    val idiomasInteres = userSnapshot.child("idiomasInteres").getValue(String::class.java)?.split(", ") ?: emptyList()

                    dbRefPreguntas.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                lista.clear()
                                for (preguntaSnapshot in snapshot.children) {
                                    val pregunta = preguntaSnapshot.getValue(Pregunta::class.java)
                                    pregunta?.let {
                                        if (idiomasInteres.contains(it.idioma) && it.respondida) {
                                            lista.add(it)
                                        }
                                    }
                                }
                                adaptador.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
