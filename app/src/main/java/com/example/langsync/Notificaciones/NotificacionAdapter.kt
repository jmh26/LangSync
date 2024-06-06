package com.example.langsync.Notificaciones

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.langsync.R

class NotificacionAdapter(
    private val context: Context,
    private val usuarios: List<UsuarioNotificacion>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<NotificacionAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(usuario: UsuarioNotificacion)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvUltimoMensaje: TextView = view.findViewById(R.id.tvUltimoMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_usuario_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.tvNombre.text = usuario.nombre
        holder.tvUltimoMensaje.text = usuario.ultimoMensaje

        holder.itemView.setOnClickListener {
            listener.onItemClick(usuario)
        }
    }

    override fun getItemCount(): Int = usuarios.size
}



