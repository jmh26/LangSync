package com.example.langsync

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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText
    private lateinit var loginButton: Button
    private lateinit var registroButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmailL)
        etContra = findViewById(R.id.etContrasenaL)
        loginButton = findViewById(R.id.btConfirmarL)
        registroButton = findViewById(R.id.tvNoTengoCuenta) // Asegurándome de que el ID es correcto

        registroButton.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }

        loginButton.setOnClickListener {
            val email = etEmail.text.toString()
            val contra = etContra.text.toString()

            if (email.isNotEmpty() && contra.isNotEmpty()) {
                loginUser(email, contra)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, contra: String) {
        auth.signInWithEmailAndPassword(email, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    val userId = user?.uid
                    val rol = "usuario" // Asigna el rol predeterminado

                    if (userId != null) {
                        // Verificar si el usuario ya existe en la base de datos
                        val databaseReference = FirebaseDatabase.getInstance().reference
                        val userRef = databaseReference.child("LangSync").child("Usuarios").child(userId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    // Si el usuario no existe, crearlo
                                    val nombre = email.substringBefore('@')
                                    val idiomaNativo = "Español" // Valor predeterminado o puedes obtenerlo de alguna fuente
                                    val idiomaInteres = "Inglés" // Valor predeterminado o puedes obtenerlo de alguna fuente
                                    Utilidades.crearUsuario(userId, email, contra, rol, nombre, idiomaNativo, idiomaInteres)
                                }
                                // Continuar con el resto del código
                                Log.d("Login", "Usuario logueado como: $rol")
                                val esAdmin = rol == "administrador"
                                val sharedPref = getSharedPreferences("login", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putBoolean("esAdmin", esAdmin)
                                editor.apply()

                                startActivity(Intent(this@Login, Home::class.java))
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Login", "Error al verificar el usuario en la base de datos: ${error.message}")
                            }
                        })
                    }
                } else {
                    Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "Error al iniciar sesión", task.exception)
                }
            }
    }
}
