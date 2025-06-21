package com.hcondor.t3_examen.ui.markers

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hcondor.t3_examen.databinding.DialogAddMarkerBinding

class AddMarkerDialog(
    private val defaultDate: String,
    private val onAdd: (name: String, date: String, note: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAddMarkerBinding.inflate(LayoutInflater.from(context))

        binding.etDate.setText(defaultDate)

        return AlertDialog.Builder(requireContext())
            .setTitle("Agregar marcador")
            .setView(binding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = binding.etName.text.toString()
                val date = binding.etDate.text.toString()
                val note = binding.etNote.text.toString()

                if (name.isBlank()) {
                    Toast.makeText(requireContext(), "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                } else {
                    onAdd(name, date, note)
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
