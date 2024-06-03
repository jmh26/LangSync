package com.example.langsync.Respuestas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.langsync.Preguntas.Pregunta
import com.example.langsync.R

class ResponderPreguntaActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_pregunta)

        val pregunta: Pregunta? = intent.getParcelableExtra("pregunta")

        // Usa la pregunta para mostrarla en tu actividad y permite que el usuario la responda.
    }
}

