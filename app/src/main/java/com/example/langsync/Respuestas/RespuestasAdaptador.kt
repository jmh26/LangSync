package com.example.langsync.Respuestas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.langsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RespuestasAdaptador(
    private val listaRespuestas: MutableList<Respuesta>,
    private val context: Context,
    private val autorId: String,
    private val preguntaId: String,
    private val destacarListener: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TIPO_AUTOR = 0
    private val TIPO_OTROS = 1
    private var idRespuestaDestacada: String? = null

    override fun getItemViewType(position: Int): Int {
        return if (listaRespuestas[position].userId == autorId) TIPO_AUTOR else TIPO_OTROS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_AUTOR) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_respuesta_autor, parent, false)
            RespuestaAutorViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_respuesta_otros, parent, false)
            RespuestaOtrosViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val respuesta = listaRespuestas[position]
        if (holder is RespuestaAutorViewHolder) {
            holder.bind(respuesta, context, preguntaId, autorId, idRespuestaDestacada == respuesta.id, destacarListener)
        } else if (holder is RespuestaOtrosViewHolder) {
            holder.bind(respuesta, context, preguntaId, autorId, idRespuestaDestacada == respuesta.id, destacarListener)
        }
    }

    override fun getItemCount(): Int = listaRespuestas.size

    fun actualizarEstrella(respuestaId: String) {
        idRespuestaDestacada = respuestaId
    }

    class RespuestaAutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val texto = itemView.findViewById<TextView>(R.id.tv_textoRespuesta)
        private val fecha = itemView.findViewById<TextView>(R.id.tv_fechaRespuesta)
        private val imagen = itemView.findViewById<ImageView>(R.id.iv_imagenRespuesta)
        private val estrella = itemView.findViewById<ImageView>(R.id.iv_estrella)

        fun bind(respuesta: Respuesta, context: Context, preguntaId: String, autorId: String, esDestacada: Boolean, destacarListener: (String) -> Unit) {
            texto.text = respuesta.texto
            fecha.text = respuesta.fecha

            val databaseReference = FirebaseDatabase.getInstance().reference
            val userId = respuesta.userId
            if (userId != null) {
                databaseReference.child("LangSync").child("Usuarios").child(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val urlFoto = snapshot.child("url_foto").value as? String
                        urlFoto?.let {
                            Glide.with(context)
                                .load(it)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .into(imagen)
                        } ?: run {
                            imagen.setImageResource(R.drawable.baseline_person_2_24)
                        }
                    }
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser?.uid == autorId) {
                estrella.visibility = View.VISIBLE
                estrella.setImageResource(if (esDestacada) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24)
                estrella.setOnClickListener {
                    destacarListener(respuesta.id ?: "")
                }
            } else {
                estrella.visibility = View.GONE
            }
        }
    }

    class RespuestaOtrosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val texto = itemView.findViewById<TextView>(R.id.tv_textoRespuesta)
        private val fecha = itemView.findViewById<TextView>(R.id.tv_fechaRespuesta)
        private val imagen = itemView.findViewById<ImageView>(R.id.iv_imagenRespuesta)
        private val estrella = itemView.findViewById<ImageView>(R.id.iv_estrella)

        fun bind(respuesta: Respuesta, context: Context, preguntaId: String, autorId: String, esDestacada: Boolean, destacarListener: (String) -> Unit) {
            texto.text = respuesta.texto
            fecha.text = respuesta.fecha

            val databaseReference = FirebaseDatabase.getInstance().reference
            val userId = respuesta.userId
            if (userId != null) {
                databaseReference.child("LangSync").child("Usuarios").child(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val urlFoto = snapshot.child("url_foto").value as? String
                        urlFoto?.let {
                            Glide.with(context)
                                .load(it)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .into(imagen)
                        } ?: run {
                            imagen.setImageResource(R.drawable.baseline_person_2_24)
                        }
                    }
            }

            estrella.visibility = if (esDestacada) View.VISIBLE else View.INVISIBLE
            estrella.setImageResource(R.drawable.baseline_star_24)

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser?.uid == autorId) {
                estrella.visibility = View.VISIBLE
                estrella.setOnClickListener {
                    destacarListener(respuesta.id ?: "")
                }
            } else {
                estrella.visibility = if (esDestacada) View.VISIBLE else View.GONE
            }
        }
    }
}
