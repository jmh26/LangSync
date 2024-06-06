package com.example.langsync.Buscar

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.langsync.Preguntas.Pregunta
import com.example.langsync.Preguntas.PreguntasAdaptador
import com.example.langsync.Respuestas.PreguntaClickListener
import com.example.langsync.databinding.FragmentBuscarBinding
import com.google.firebase.database.*

class BuscarFragment : Fragment() {

    private var _binding: FragmentBuscarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adaptador: PreguntasAdaptador
    private lateinit var lista: MutableList<Pregunta>
    private lateinit var dbRefPreguntas: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuscarBinding.inflate(inflater, container, false)
        val rootView = binding.root

        lista = mutableListOf()
        adaptador = PreguntasAdaptador(lista, object : PreguntaClickListener {
            override fun onPreguntaClick(pregunta: Pregunta) {
            }
        })
        binding.recyclerViewPreguntas.adapter = adaptador
        binding.recyclerViewPreguntas.layoutManager = LinearLayoutManager(context)

        dbRefPreguntas = FirebaseDatabase.getInstance().getReference("LangSync").child("Preguntas")

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarPreguntas(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return rootView
    }

    private fun buscarPreguntas(texto: String) {
        dbRefPreguntas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                for (preguntaSnapshot in snapshot.children) {
                    val pregunta = preguntaSnapshot.getValue(Pregunta::class.java)
                    if (pregunta != null && pregunta.texto?.contains(texto, true) == true) {
                        lista.add(pregunta)
                    }
                }
                adaptador.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
