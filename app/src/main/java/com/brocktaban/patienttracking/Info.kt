package com.brocktaban.patienttracking


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_info.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.wtf


class Info(val code: String) : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var bookmarked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        db.collection("patients").document(code).get().addOnCompleteListener { task ->
            if(!task.isSuccessful) {
                wtf("Could not get patients data", task.exception)
                return@addOnCompleteListener
            }

            val patient = task.result?.toObject(Patient::class.java)

            var meds = ""

            Glide.with(context!!).load(patient?.avatar).into(v.avatar)

            v.name.text = patient?.name
            v.status.text = "Status: ${patient?.status}"
            v.description.text = patient?.description

            if (patient?.medicines != null) {
                for (x in patient.medicines!!) {
                    meds += x + "\n"
                }
            }
            v.medicines.text = meds
        }

        db
            .collection("users")
            .document(mAuth.currentUser?.uid!!)
            .collection("patients")
            .document(code)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.data?.get("bookmarked") != null) {
                    bookmarked = documentSnapshot.data?.get("bookmarked") as Boolean

                    if (bookmarked) {
                        v.visit.text = "remove from your list"
                    } else {
                        v.visit.text = "add to your list"
                    }
                }
            }

        v.visit.setOnClickListener {

            val patientMap = HashMap<String, Any>()

            bookmarked = !bookmarked

            patientMap["lastEdited"] = FieldValue.serverTimestamp()
            patientMap["bookmarked"] = bookmarked


            db
                .collection("users")
                .document(mAuth.currentUser?.uid!!)
                .collection("patients")
                .document(code)
                .update(patientMap)
                .addOnSuccessListener {
                    if (bookmarked) {
                        v.visit.text = "remove from your list"
                    } else {
                        v.visit.text = "add to your list"
                    }
                }
        }

        return v
    }


}
