package com.example.langsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Registro : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText
    private lateinit var registro: Button
    private lateinit var yaTengoCuenta: TextView
    private lateinit var spinnerIdiomaNativo: Spinner
    private lateinit var spinnerIdiomasInteres1: Spinner
    private lateinit var spinnerIdiomasInteres2: Spinner

    private lateinit var idiomas: Array<String>

    private var isSpinnerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        auth = FirebaseAuth.getInstance()

        yaTengoCuenta = findViewById(R.id.tvYaTengoCuenta)
        etEmail = findViewById(R.id.etEmail)
        etContra = findViewById(R.id.etContrasena)
        registro = findViewById(R.id.btConfirmar)
        spinnerIdiomaNativo = findViewById(R.id.spinnerIdiomaNativo)
        spinnerIdiomasInteres1 = findViewById(R.id.spinnerIdiomasInteres1)
        spinnerIdiomasInteres2 = findViewById(R.id.spinnerIdiomasInteres2)

        yaTengoCuenta.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        idiomas = resources.getStringArray(R.array.idiomas_array)

        setupSpinners()

        registro.setOnClickListener {
            val email = etEmail.text.toString()
            val contra = etContra.text.toString()
            val idiomaNativo = spinnerIdiomaNativo.selectedItem.toString()
            val idiomaInteres1 = spinnerIdiomasInteres1.selectedItem.toString()
            val idiomaInteres2 = spinnerIdiomasInteres2.selectedItem.toString()

            if (email.isNotEmpty() && contra.isNotEmpty() && idiomaNativo.isNotEmpty() && idiomaInteres1.isNotEmpty() && idiomaInteres1 != "Ninguno") {
                val idiomasInteres = if (idiomaInteres2 != "Ninguno") {
                    "$idiomaInteres1, $idiomaInteres2"
                } else {
                    idiomaInteres1
                }
                registerUser(email, contra, email.substringBefore('@'), idiomaNativo, idiomasInteres)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        val adapterIdiomas = ArrayAdapter(this, android.R.layout.simple_spinner_item, idiomas)
        adapterIdiomas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerIdiomaNativo.adapter = adapterIdiomas
        spinnerIdiomasInteres1.adapter = adapterIdiomas
        spinnerIdiomasInteres2.adapter = adapterIdiomas

        spinnerIdiomaNativo.setSelection(0) // Asigna el primer idioma al spinner de idioma nativo
        spinnerIdiomasInteres1.setSelection(1) // Asigna el segundo idioma al primer spinner de idioma de interés
        spinnerIdiomasInteres2.setSelection(2) // Asigna el tercer idioma al segundo spinner de idioma de interés

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isSpinnerInitialized) {
                    actualizarSpinners()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerIdiomaNativo.onItemSelectedListener = spinnerListener
        spinnerIdiomasInteres1.onItemSelectedListener = spinnerListener
        spinnerIdiomasInteres2.onItemSelectedListener = spinnerListener

        isSpinnerInitialized = true
    }

    private fun actualizarSpinners() {
        val idiomaNativoSeleccionado = spinnerIdiomaNativo.selectedItem.toString()
        val idiomaInteres1Seleccionado = spinnerIdiomasInteres1.selectedItem.toString()
        val idiomaInteres2Seleccionado = spinnerIdiomasInteres2.selectedItem.toString()

        val idiomasFiltradosNativo = idiomas.filter { it != idiomaInteres1Seleccionado && it != idiomaInteres2Seleccionado }
        val idiomasFiltradosInteres1 = idiomas.filter { it != idiomaNativoSeleccionado && it != idiomaInteres2Seleccionado }
        val idiomasFiltradosInteres2 = idiomas.filter { it != idiomaNativoSeleccionado && it != idiomaInteres1Seleccionado }

        actualizarSpinner(spinnerIdiomaNativo, idiomasFiltradosNativo, idiomaNativoSeleccionado)
        actualizarSpinner(spinnerIdiomasInteres1, idiomasFiltradosInteres1, idiomaInteres1Seleccionado)
        actualizarSpinner(spinnerIdiomasInteres2, idiomasFiltradosInteres2, idiomaInteres2Seleccionado)
    }

    private fun actualizarSpinner(spinner: Spinner, items: List<String>, selectedItem: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val selectedIndex = items.indexOf(selectedItem)
        if (selectedIndex >= 0) {
            spinner.setSelection(selectedIndex)
        }
    }

    private fun registerUser(email: String, contra: String, nombre: String, idiomaNativo: String, idiomasInteres: String) {
        auth.createUserWithEmailAndPassword(email, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        val esAdmin = Utilidades.esAdmin(email, contra)

                        val sharedPrefEditor = getSharedPreferences("login", MODE_PRIVATE).edit()
                        sharedPrefEditor.putString("email", email)
                        sharedPrefEditor.putBoolean("esAdmin", esAdmin)
                        sharedPrefEditor.putString("nombre", nombre)
                        sharedPrefEditor.apply()

                        val rol = if (esAdmin) "administrador" else "usuario"
                        Utilidades.crearUsuario(userId, email, contra, rol, nombre, idiomaNativo, idiomasInteres)

                        startActivity(Intent(this, Home::class.java))

                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error al registrar el usuario: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Registro", "Error al registrar el usuario", task.exception)
                }
            }
    }
}
