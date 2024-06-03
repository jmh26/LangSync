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
import com.bumptech.glide.Glide
import com.example.langsync.R
import com.example.langsync.Respuestas.PreguntaClickListener
import com.google.firebase.database.FirebaseDatabase

class PreguntasAdaptador (private val listaPreguntas: MutableList<Pregunta>, private var contexto: Context, private val clickListener: PreguntaClickListener ): RecyclerView.Adapter<PreguntasAdaptador.PreguntaViewHolder>(),

    Filterable {
    private var listaPreguntasFiltrada = listaPreguntas



    class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto = itemView.findViewById<TextView>(R.id.tv_texto)
        val fecha = itemView.findViewById<TextView>(R.id.tv_fecha)
        val eliminar = itemView.findViewById<ImageView>(R.id.iv_eliminar)
        val nombre = itemView.findViewById<TextView>(R.id.nombrePregunta)
        val imagenPerfil = itemView.findViewById<ImageView>(R.id.iv_imagenPregunta)

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

        holder.itemView.setOnClickListener {
            clickListener.onPreguntaClick(preguntaActual)
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        val userId = preguntaActual.userId
        if (userId != null) {
            databaseReference.child("LangSync").child("Usuarios").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val nombre = snapshot.child("nombre").value as? String
                    val urlFoto = snapshot.child("url_foto").value as? String

                    holder.nombre.text = nombre ?: "Nombre no encontrado"

                    urlFoto?.let {
                        Glide.with(contexto)
                            .load(it)
                            .placeholder(R.drawable.baseline_person_2_24)
                            .into(holder.imagenPerfil)
                    } ?: run {
                        holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                    }
                }
                .addOnFailureListener {
                    // Manejar el caso de falla al obtener la información del usuario
                    holder.nombre.text = "Nombre no encontrado"
                    holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                }
        } else {
            // Manejar el caso en el que el ID de usuario es nulo
            holder.nombre.text = "ID de usuario no válido"
            holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
        }

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
        } else {
            holder.eliminar.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = listaPreguntasFiltrada.size
}
