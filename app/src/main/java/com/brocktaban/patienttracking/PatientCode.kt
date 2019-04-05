package com.brocktaban.patienttracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_patient_code.*
import org.jetbrains.anko.design.snackbar

class PatientCode : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_code)

        et_patient_code.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(5))

        et_patient_code.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val code = et_patient_code.text.toString()

                checkCode(code)

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        btn_track_patient.setOnClickListener {
            val code = et_patient_code.text.toString()

            checkCode(code)
        }
    }

    private fun checkCode(code: String) {
        main.snackbar(code)
    }
}
