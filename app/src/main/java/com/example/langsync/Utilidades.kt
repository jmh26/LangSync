package com.example.langsync

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.langsync.Preguntas.Pregunta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Utilidades {

    companion object{
        fun crearUsuario(id:String, email: String, contra:String, rol: String,nombre: String){
            var db_ref = FirebaseDatabase.getInstance().reference
            val urlFotoPredeterminada = "android.resource://com.example.langsync/drawable/baseline_person_2_24"
            val nombre = email.substringBefore('@')
            val usuario = Usuario(id, email, contra, rol, urlFotoPredeterminada, nombre)
            db_ref.child("LangSync").child("Usuarios").child(id).setValue(usuario)
        }

        fun esAdmin(email: String, contra: String): Boolean {
            return email == "administrador@gmail.com" && contra == "administrador"
        }

        fun obtenerRol(email:String, contra: String, auth: FirebaseAuth): String{
            return if (email == "administrador@gmail.com" && contra == "administrador") {
                "administrador"
            }else{
                "usuario"
            }
        }

        fun crearPregunta(dtb_ref: DatabaseReference, pregunta: Pregunta){
            dtb_ref.child("LangSync").child("Preguntas").child(pregunta.id!!).setValue(pregunta)
        }

        fun toastCorrutina(activity: AppCompatActivity, contexto: Context, texto: String){
            activity.runOnUiThread{
                Toast.makeText(contexto, texto, Toast.LENGTH_SHORT).show()
            }
        }


    }



}