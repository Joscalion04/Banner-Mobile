package com.example.frontend_mobile.data.repository

import com.example.frontend_mobile.data.model.Usuario
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.net.HttpURLConnection
import java.net.URL

object UsuarioRepository {
    private const val BASE_URL = "http://10.0.2.2:8080/api"
    private val gson = Gson()

    suspend fun registrarUsuario(usuario: Usuario): Boolean {
        val url = URL("$BASE_URL/insertarUsuario")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")

        // Enviar JSON
        conn.outputStream.use { os ->
            val json = gson.toJson(usuario)
            os.write(json.toByteArray())
        }

        val responseCode = conn.responseCode
        try {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val response = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                println("Respuesta del servidor: $response")
            } else {
                val errorStream = conn.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                println("Error del servidor (code $responseCode): $errorResponse")
            }
        } catch (e: EOFException) {
            // No hay cuerpo o cuerpo inesperado, solo loguear y continuar
            println("EOFException: cuerpo vacío o formato inesperado en la respuesta.")
        }

        return responseCode == HttpURLConnection.HTTP_OK
    }

    suspend fun getUsuarioRemoto(cedula: String, clave: String): Usuario? =
        withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true

            try {
                // Solo se envían cedula y clave
                val usuarioJson = gson.toJson(Usuario(cedula = cedula, clave = clave))
                conn.outputStream.bufferedWriter().use { it.write(usuarioJson) }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    gson.fromJson(response, Usuario::class.java) // Aquí ya viene con tipoUsuario y más
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error desconocido"
                    println("Error al hacer login ($responseCode): $error")
                    null
                }
            } catch (e: Exception) {
                println("Error en la conexión: ${e.message}")
                null
            } finally {
                conn.disconnect()
            }
        }
}