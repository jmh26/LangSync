package com.example.langsync

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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

lateinit var auth: FirebaseAuth
private var user: FirebaseUser? = null
private lateinit var etEmail: EditText
private lateinit var etContra: EditText
private lateinit var registro: Button
private lateinit var noTengoCuenta: TextView

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        noTengoCuenta = findViewById(R.id.tvNoTengoCuenta)
        noTengoCuenta.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }

        etEmail = findViewById(R.id.etEmailL)
        etContra = findViewById(R.id.etContrasenaL)
        registro = findViewById(R.id.btConfirmarL)

        registro.setOnClickListener {
            val email = etEmail.text.toString()
            val contra = etContra.text.toString()

            if (email.isNotEmpty() && contra.isNotEmpty()) {
                loginUser(email, contra)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun loginUser(email: String, contra: String) {
        auth.signInWithEmailAndPassword(email, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser: FirebaseUser? = auth.currentUser
                    val userId = currentUser?.uid
                    val rol = Utilidades.obtenerRol(email, contra, auth)

                    if (userId != null) {
                        // Verificar si el usuario ya existe en la base de datos
                        val databaseReference = FirebaseDatabase.getInstance().reference
                        val userRef =
                            databaseReference.child("LangSync").child("Usuarios").child(userId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    // Si el usuario no existe, crearlo
                                    val nombre = email.substringBefore('@')
                                    Utilidades.crearUsuario(userId, email, contra, rol, nombre)
                                }
                                // Continuar con el resto del código
                                Log.d("Login", "Usuario logueado como: $rol")
                                val esAdmin = rol == "administrador"
                                val sharedPref = getSharedPreferences("login", MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putBoolean("esAdmin", esAdmin)
                                editor.apply()

                                startActivity(Intent(this@Login, Home::class.java))
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "Login",
                                    "Error al verificar el usuario en la base de datos: ${error.message}"
                                )
                            }
                        })
                    }
                }
            }
    }
}

