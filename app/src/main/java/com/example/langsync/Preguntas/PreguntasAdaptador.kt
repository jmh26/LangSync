package com.example.langsync.Preguntas

import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PreguntasAdaptador(
    private val listaPreguntas: MutableList<Pregunta>,
    private val clickListener: PreguntaClickListener
) : RecyclerView.Adapter<PreguntasAdaptador.PreguntaViewHolder>(), Filterable {

    private var listaPreguntasFiltrada = listaPreguntas

    class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto = itemView.findViewById<TextView>(R.id.tv_texto)
        val fecha = itemView.findViewById<TextView>(R.id.tv_fecha)
        val idioma = itemView.findViewById<TextView>(R.id.tv_idioma)
        val eliminar = itemView.findViewById<ImageView>(R.id.iv_eliminar)
        val nombre = itemView.findViewById<TextView>(R.id.nombrePregunta)
        val imagenPerfil = itemView.findViewById<ImageView>(R.id.iv_imagenPregunta)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString()
                val resultList = if (busqueda.isEmpty()) {
                    listaPreguntas
                } else {
                    val filteredList = mutableListOf<Pregunta>()
                    for (row in listaPreguntas) {
                        if (row.texto?.contains(busqueda, ignoreCase = true) == true) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                return FilterResults().apply { values = resultList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaPreguntasFiltrada = results?.values as MutableList<Pregunta>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreguntaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_pregunta, parent, false)
        return PreguntaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PreguntaViewHolder, position: Int) {
        val preguntaActual = listaPreguntasFiltrada[position]
        holder.texto.text = preguntaActual.texto
        holder.fecha.text = preguntaActual.fecha
        holder.idioma.text = preguntaActual.idioma
        holder.nombre.text = preguntaActual.nombre

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
                        Glide.with(holder.itemView.context)
                            .load(it)
                            .placeholder(R.drawable.baseline_person_2_24)
                            .into(holder.imagenPerfil)
                    } ?: run {
                        holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                    }
                }
                .addOnFailureListener {
                    holder.nombre.text = "Nombre no encontrado"
                    holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
                }
        } else {
            holder.nombre.text = "ID de usuario no válido"
            holder.imagenPerfil.setImageResource(R.drawable.baseline_person_2_24)
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val sharedPreferences = holder.itemView.context.getSharedPreferences("login", Context.MODE_PRIVATE)
        val esAdmin = sharedPreferences.getBoolean("esAdmin", false)

        // Mostrar el icono de eliminar si el usuario es admin o el propietario de la pregunta
        if (esAdmin || userId == currentUserId) {
            holder.eliminar.visibility = View.VISIBLE
            holder.eliminar.setOnClickListener {
                try {
                    val dbRef = FirebaseDatabase.getInstance().reference
                    listaPreguntas.remove(preguntaActual)
                    notifyDataSetChanged() // Asegurarse de actualizar la vista

                    dbRef.child("LangSync").child("Preguntas").child(preguntaActual.id!!).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Pregunta eliminada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Error al eliminar pregunta", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(holder.itemView.context, "Error al eliminar pregunta", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.eliminar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = listaPreguntasFiltrada.size
}
