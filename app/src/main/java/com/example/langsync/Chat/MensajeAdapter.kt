

package com.example.langsync.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.R
import com.example.langsync.model.Mensaje
import com.google.firebase.auth.FirebaseAuth

class MensajeAdapter(
    private val context: Context,
    private val mensajes: List<Mensaje>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].de == currentUserId) {
            R.layout.item_mensaje_enviado
        } else {
            R.layout.item_mensaje_recibido
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_mensaje_enviado) {
            MensajeEnviadoViewHolder(view)
        } else {
            MensajeRecibidoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        if (holder is MensajeEnviadoViewHolder) {
            holder.bind(mensaje)
        } else if (holder is MensajeRecibidoViewHolder) {
            holder.bind(mensaje)
        }
    }

    override fun getItemCount(): Int = mensajes.size

    inner class MensajeEnviadoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMensaje: TextView = view.findViewById(R.id.tvMensajeEnviado)

        fun bind(mensaje: Mensaje) {
            tvMensaje.text = mensaje.mensaje
        }
    }

    inner class MensajeRecibidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMensaje: TextView = view.findViewById(R.id.tvMensajeRecibido)

        fun bind(mensaje: Mensaje) {
            tvMensaje.text = mensaje.mensaje
        }
    }
}
