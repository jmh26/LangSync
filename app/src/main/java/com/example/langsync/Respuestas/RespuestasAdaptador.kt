package com.example.langsync.Respuestas

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.langsync.Perfil.PerfilUsuarioActivity
import com.example.langsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RespuestasAdaptador(
    private val listaRespuestas: MutableList<Respuesta>,
    private val context: Context,
    private val esAdmin: Boolean,  // Pasar si es administrador
    private val destacarListener: (String) -> Unit
) : RecyclerView.Adapter<RespuestasAdaptador.RespuestaViewHolder>() {

    private var idRespuestaDestacada: String? = null

    override fun getItemViewType(position: Int): Int {
        return if (listaRespuestas[position].userId == FirebaseAuth.getInstance().currentUser?.uid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RespuestaViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_respuesta_autor else R.layout.item_respuesta_otros
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return RespuestaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RespuestaViewHolder, position: Int) {
        val respuesta = listaRespuestas[position]
        holder.bind(respuesta, context, esAdmin, idRespuestaDestacada == respuesta.id, destacarListener)
    }

    override fun getItemCount(): Int = listaRespuestas.size

    fun actualizarEstrella(respuestaId: String) {
        idRespuestaDestacada = respuestaId
        notifyDataSetChanged()
    }

    inner class RespuestaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val texto = itemView.findViewById<TextView>(R.id.tv_textoRespuesta)
        private val fecha = itemView.findViewById<TextView>(R.id.tv_fechaRespuesta)
        private val imagen = itemView.findViewById<ImageView>(R.id.iv_imagenRespuesta)
        private val estrella = itemView.findViewById<ImageView>(R.id.iv_estrella)

        fun bind(respuesta: Respuesta, context: Context, esAdmin: Boolean, esDestacada: Boolean, destacarListener: (String) -> Unit) {
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

            if (esAdmin) {
                estrella.visibility = View.VISIBLE
                estrella.setImageResource(if (esDestacada) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24)
                estrella.setOnClickListener {
                    destacarListener(respuesta.id ?: "")
                }
            } else {
                estrella.visibility = if (esDestacada) View.VISIBLE else View.INVISIBLE
                estrella.setImageResource(R.drawable.baseline_star_24)
            }

            // Add click listener to the profile image
            imagen.setOnClickListener {
                val intent = Intent(context, PerfilUsuarioActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                context.startActivity(intent)
            }
        }
    }
}

