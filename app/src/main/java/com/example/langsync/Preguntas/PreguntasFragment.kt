package com.example.langsync.Preguntas

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.R
import com.example.langsync.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PreguntasFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var fabAddPregunta: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: PreguntasAdaptador
    private lateinit var listaPedidos: MutableList<Pregunta>

    private lateinit var viewModel: PreguntasViewModel
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = _binding!!.root

        fabAddPregunta = binding.fabAddpregunta


        recyclerView = binding.recyclerViewPreguntas
        recyclerView.layoutManager = LinearLayoutManager(context)

        fabAddPregunta.setOnClickListener {
            val intent = Intent(activity, AnadirPregunta::class.java)
            startActivity(intent)
        }




        return root
    }








    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PreguntasViewModel::class.java)
        // TODO: Use the ViewModel
    }
}

