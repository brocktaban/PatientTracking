package com.brocktaban.patienttracking

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.inputmethod.EditorInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_patient_code.*
import org.jetbrains.anko.design.snackbar
import android.view.inputmethod.InputMethodManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.wtf


class PatientCode : AppCompatActivity(), AnkoLogger {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_code)

        db = FirebaseFirestore.getInstance()

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
        btn_track_patient.isEnabled = false

        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

        db.collection("patients").document(code).get().addOnCompleteListener { task ->
            btn_track_patient.isEnabled = true

            if (!task.isSuccessful) {
                wtf("Could not get patients document", task.exception)
                return@addOnCompleteListener
            }

            if (!task.result!!.exists()) {
                main.snackbar("Code is not exist")
                return@addOnCompleteListener
            }

            startActivity(intentFor<PatientInfo>("code" to code))
        }
    }
}
