package com.example.frontend_mobile.ui.profesores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontend_mobile.data.model.Curso
import com.example.frontend_mobile.data.model.Profesor
import com.example.frontend_mobile.data.repository.ProfesorRepository
import com.example.frontend_mobile.databinding.ItemProfesorCardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ProfesorAdapter(
    internal var listaProfesores: MutableList<Profesor>,
    private val listener: OnProfesorClickListener,
    private val profesorRepository: ProfesorRepository
) : RecyclerView.Adapter<ProfesorAdapter.ProfesorViewHolder>() {

    interface OnProfesorClickListener {
        fun onProfesorClick(profesor: Profesor)
        fun onProfesorLongClick(profesor: Profesor): Boolean
    }

    inner class ProfesorViewHolder(val binding: ItemProfesorCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(profesor: Profesor) {
            binding.tvCedulaProfesor.text = "Cédula: " + profesor.cedula
            binding.tvNombreProfesor.text = "Nombre: " + profesor.nombre
            binding.tvTelefonoProfesor.text = "Teléfono: " + profesor.telefono
            binding.tvEmailProfesor.text = "Correo: " + profesor.email
            binding.root.setOnClickListener { listener.onProfesorClick(profesor) }
            binding.root.setOnLongClickListener { listener.onProfesorLongClick(profesor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val binding =
            ItemProfesorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfesorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        holder.bind(listaProfesores[position])
    }

    override fun getItemCount(): Int = listaProfesores.size

    fun actualizarLista(nueva: List<Profesor>) {
        listaProfesores.clear()
        listaProfesores.addAll(nueva)
        notifyDataSetChanged()
    }

    suspend fun eliminarProfesor(pos: Int): Boolean {
        val profesor = listaProfesores[pos]
        val exito = profesorRepository.eliminarProfesor(profesor)  // suspend
        if (exito) {
            listaProfesores.removeAt(pos)
            withContext(Dispatchers.Main) {
                notifyItemRemoved(pos)
            }
        }
        return exito
    }
}