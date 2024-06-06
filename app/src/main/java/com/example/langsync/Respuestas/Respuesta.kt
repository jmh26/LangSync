package com.example.langsync.Respuestas


data class Respuesta(
    val id: String? = null,
    val texto: String? = null,
    val userId: String? = null,
    val fecha: String? = null,
    val urlFoto: String? = null,
    val destacada: Boolean = false
)


