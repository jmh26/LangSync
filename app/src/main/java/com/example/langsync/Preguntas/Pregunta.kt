package com.example.langsync.Preguntas


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pregunta(
    var id: String? = null,
    var texto: String? = null,
    var userId: String? = null,
    var fecha: String? = null,
    var nombre: String? = null,
    var imagenPerfil: String? = null





): Parcelable
