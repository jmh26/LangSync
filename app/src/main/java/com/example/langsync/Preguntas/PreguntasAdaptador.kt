package com.example.langsync.Preguntas

import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.R
import com.google.firebase.database.FirebaseDatabase

class PreguntasAdaptador (private val listaPreguntas: MutableList<Pregunta>): RecyclerView.Adapter<PreguntasAdaptador.PreguntaViewHolder>(),

    Filterable {
    private lateinit var contexto: Context
    private var listaPreguntasFiltrada = listaPreguntas


    class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto = itemView.findViewById<TextView>(R.id.tv_texto)
        val fecha = itemView.findViewById<TextView>(R.id.tv_fecha)
        val eliminar = itemView.findViewById<ImageView>(R.id.iv_eliminar)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString()
                if (busqueda.isEmpty()) {
                    listaPreguntasFiltrada = listaPreguntas
                } else {
                    val resultList = mutableListOf<Pregunta>()
                    for (row in listaPreguntas) {
                        if (row.texto?.toLowerCase()!!.contains(busqueda.toLowerCase())) {
                            resultList.add(row)
                        }
                    }
                    listaPreguntasFiltrada = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listaPreguntasFiltrada
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaPreguntasFiltrada = results?.values as MutableList<Pregunta>
                notifyDataSetChanged()
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PreguntasAdaptador.PreguntaViewHolder {
        contexto = parent.context
        val itemView = LayoutInflater.from(contexto).inflate(R.layout.item_pregunta, parent, false)
        return PreguntasAdaptador.PreguntaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PreguntaViewHolder, position: Int) {
        val preguntaActual = listaPreguntas[position]
        holder.texto.text = preguntaActual.texto
        holder.fecha.text = preguntaActual.fecha


        var sharedPreferences = contexto.getSharedPreferences("login", Context.MODE_PRIVATE)
        var esAdmin = sharedPreferences.getBoolean("esAdmin", false)

        if (esAdmin){
            holder.eliminar.visibility = View.VISIBLE
            holder.eliminar.setOnClickListener {
                try {
                    val db_ref = FirebaseDatabase.getInstance().reference
                    listaPreguntas.remove(preguntaActual)

                    db_ref.child("LangSync").child("Preguntas").child(preguntaActual.id!!).removeValue()
                    Toast.makeText(contexto, "Pregunta eliminada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(contexto, "Error al eliminar pregunta", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            holder.eliminar.visibility = View.GONE
        }

    }


    override fun getItemCount(): Int = listaPreguntasFiltrada.size
}
