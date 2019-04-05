package com.brocktaban.patienttracking

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_enter_code.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.wtf

class EnterCode : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_enter_code, container, false)

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

        return v
    }

    private fun checkCode(code: String) {
        btn_track_patient.isEnabled = false

        val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputManager.hideSoftInputFromWindow(
            activity?.currentFocus!!.windowToken,
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

//            (activity as MainActivity).changeFragment()
        }
    }
}
