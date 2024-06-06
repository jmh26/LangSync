package com.example.langsync.Mensajes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.langsync.R
import com.example.langsync.databinding.ActivityEnviarMensajeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EnviarMensajeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnviarMensajeBinding
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnviarMensajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID") ?: ""

        binding.btnEnviar.setOnClickListener {
            enviarMensaje()
        }

        createNotificationChannel()
    }

    private fun enviarMensaje() {
        val mensaje = binding.etMensaje.text.toString()
        if (mensaje.isNotEmpty()) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val databaseReference = FirebaseDatabase.getInstance().reference
            val mensajeId = databaseReference.child("LangSync").child("Mensajes").push().key

            val mensajeMap = mapOf(
                "de" to currentUserId,
                "para" to userId,
                "mensaje" to mensaje,
                "timestamp" to System.currentTimeMillis()
            )

            mensajeId?.let {
                databaseReference.child("LangSync").child("Mensajes").child(it).setValue(mensajeMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etMensaje.text.clear()
                        sendNotification(userId, mensaje)
                    }
                }
            }
        }
    }

    private fun sendNotification(userId: String, mensaje: String) {
        val intent = Intent(this, MensajesActivity::class.java).apply {
            putExtra("USER_ID", userId)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.logo1)
            .setContentTitle("Nuevo mensaje")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@EnviarMensajeActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(1, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MessageChannel"
            val descriptionText = "Channel for message notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}



