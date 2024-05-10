package com.example.langsync.Preguntas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.langsync.Home
import com.example.langsync.R
import com.example.langsync.Utilidades
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AnadirPregunta: AppCompatActivity(), CoroutineScope {

    private lateinit var crear: Button
    private lateinit var texto: EditText
    private lateinit var db_ref: DatabaseReference
    private lateinit var job: Job



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_pregunta)
        val thisActivity = this
        texto = findViewById(R.id.et_pregunta)
        crear = findViewById(R.id.fab_crearPregunta)

        crear.setOnClickListener {
            val texto = texto.text.toString()
            if (texto.isNotEmpty()) {
                var id_generada = db_ref.child("LangSync").child("Preguntas").push().key

                launch {

                    var pregunta = Pregunta(
                        id_generada,
                        texto
                    )
                    Utilidades.crearPregunta(db_ref, pregunta)

                    Toast.makeText(thisActivity, "Pregunta añadida con éxito", Toast.LENGTH_SHORT).show()

                    Utilidades.toastCorrutina(thisActivity, applicationContext, "Pregunta añadida con éxito")

                    val activity = Intent(applicationContext, Home::class.java)
                    startActivity(activity)

                }


            } else {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }



    }
    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


}