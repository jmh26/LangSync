package com.example.langsync.Notificaciones

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.langsync.Chat.ChatActivity
import com.example.langsync.R
import com.example.langsync.databinding.FragmentNotificacionesBinding
import com.example.langsync.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificacionesFragment : Fragment(), NotificacionAdapter.OnItemClickListener {

    private lateinit var binding: FragmentNotificacionesBinding
    private lateinit var notificacionAdapter: NotificacionAdapter
    private val usuariosList = mutableListOf<UsuarioNotificacion>()
    private val mensajesMap = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificacionAdapter = NotificacionAdapter(requireContext(), usuariosList, this)
        binding.recyclerViewNotificaciones.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificacionAdapter
        }

        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("LangSync").child("Mensajes")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let {
                        val usuarioId = if (it.de == currentUserId) it.para else it.de

                        if (!mensajesMap.containsKey(usuarioId)) {
                            mensajesMap[usuarioId] = it.mensaje
                            databaseReference.child("LangSync").child("Usuarios").child(usuarioId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val nombre = snapshot.child("nombre").value as? String ?: "Usuario"
                                        val usuario = UsuarioNotificacion(usuarioId, nombre, it.mensaje)
                                        usuariosList.add(usuario)
                                        notificacionAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else {
                            mensajesMap[usuarioId] = it.mensaje
                            usuariosList.find { usuario -> usuario.id == usuarioId }?.let { usuario ->
                                usuario.ultimoMensaje = it.mensaje
                                notificacionAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onItemClick(usuario: UsuarioNotificacion) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("USER_ID", usuario.id)
        startActivity(intent)
    }
}

