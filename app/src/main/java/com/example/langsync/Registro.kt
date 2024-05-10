package com.example.langsync

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Registro: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText
    private lateinit var registro: Button
    private lateinit var yaTengoCuenta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        auth = FirebaseAuth.getInstance()

        yaTengoCuenta = findViewById(R.id.tvYaTengoCuenta)
        etEmail = findViewById(R.id.etEmail)
        etContra = findViewById(R.id.etContrasena)
        registro = findViewById(R.id.btConfirmar)

        yaTengoCuenta.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        registro.setOnClickListener {

            val email = etEmail.text.toString()
            val contra = etContra.text.toString()

            if (email.isNotEmpty() && contra.isNotEmpty()) {
                registerUser(email, contra)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()

            }

        }

    }


    private fun registerUser(email: String, contra: String) {
        auth.createUserWithEmailAndPassword(email, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        val rol = if (Utilidades.esAdmin(email, contra)) {
                            "administrador"
                        } else {
                            "usuario"
                        }
                        Utilidades.crearUsuario(userId, email, contra, rol)

                        val esAdmin = rol == "administrador"
                        val sharedPref = getSharedPreferences("login", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("esAdmin", esAdmin)
                        editor.apply()

                        if (esAdmin) {
                            startActivity(Intent(this, Home::class.java))
                        } else {
                            startActivity(Intent(this, Home::class.java))
                        }

                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}