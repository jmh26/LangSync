package com.example.langsync.Perfil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.langsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class EditarPerfil : AppCompatActivity() {
    private lateinit var imagenPerfil: ImageView
    private lateinit var nombreEditText: EditText
    private lateinit var btnAceptar: Button

    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        imagenPerfil = findViewById(R.id.iv_imagenPerfil)
        nombreEditText = findViewById(R.id.nombreEditText)
        btnAceptar = findViewById(R.id.btnAceptar)

        imagenPerfil.setOnClickListener {
            seleccionarImagenDeGaleria()
        }

        btnAceptar.setOnClickListener {
            guardarCambios()
        }

        cargarNombreActual()
    }

    private fun seleccionarImagenDeGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, CODIGO_SELECCION_IMAGEN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCION_IMAGEN && resultCode == RESULT_OK && data != null) {
            imagenUri = data.data
            imagenPerfil.setImageURI(imagenUri)
        }
    }

    private fun cargarNombreActual() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("LangSync").child("Usuarios").child(userId).get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombre").value as? String
            val urlFoto = snapshot.child("url_foto").value as? String
            nombreEditText.setText(nombre)
            urlFoto?.let { uri ->
                Glide.with(this).load(uri).into(imagenPerfil)
            }
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = nombreEditText.text.toString()

        if (imagenUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("perfil_imagenes/$userId.jpg")
            storageReference.putFile(imagenUri!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val updates = hashMapOf<String, Any>(
                        "nombre" to nuevoNombre,
                        "url_foto" to uri.toString()
                    )
                    databaseReference.child("LangSync").child("Usuarios").child(userId)
                        .updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar los cambios: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val updates = hashMapOf<String, Any>("nombre" to nuevoNombre)
            databaseReference.child("LangSync").child("Usuarios").child(userId)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar los cambios: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val CODIGO_SELECCION_IMAGEN = 1001
    }
}


