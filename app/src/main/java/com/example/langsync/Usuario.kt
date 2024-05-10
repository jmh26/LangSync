package com.example.langsync

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize

data class Usuario(
    var id: String? = null,
    var email: String? = null,
    var contrasena: String? = null,
    var url_foto: String? = null,
    var tipo: String? =""

): Parcelable