package com.example.langsync.Preguntas

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.R
import com.example.langsync.Utilidades
import com.example.langsync.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PreguntasFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: PreguntasAdaptador
    private lateinit var lista: MutableList<Pregunta>
    private lateinit var fabAddPregunta: FloatingActionButton
    private var contextoapp = this.context

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
        adaptador = PreguntasAdaptador(lista)
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
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("LangSync/Preguntas")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    lista.clear()
                    for (preguntaSnapshot in snapshot.children) {
                        val pregunta = preguntaSnapshot.getValue(Pregunta::class.java)
                        pregunta?.let { lista.add(it) }
                    }
                    adaptador.notifyDataSetChanged()
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
}

