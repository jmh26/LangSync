package com.example.langsync

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize

data class Usuario(
    var id: String? = null,
    var email: String? = null,
    var contrasena: String? = null,
    var rol: String? ="",
    var url_foto: String? = null,
    var nombre: String? = null


): Parcelable