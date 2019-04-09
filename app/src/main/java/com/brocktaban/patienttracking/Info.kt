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
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar


class Info(val code: String) : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var followed: Boolean = false
    private var reported: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        db.collection("patients").document(code).get().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                wtf("Could not get patients data", task.exception)
                return@addOnCompleteListener
            }

            val patient = task.result?.toObject(Patient::class.java)

            var meds = ""

            Glide.with(context!!).load("https://api.adorable.io/avatars/285/${patient?.id}.png").into(v.avatar)

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
                if (documentSnapshot.data?.get("followed") != null) {
                    followed = documentSnapshot.data?.get("followed") as Boolean

                    if (followed) {
                        v.visit.text = "unfollow"
                    } else {
                        v.visit.text = "follow"
                    }
                }
            }

        db
            .collection("reports")
            .whereEqualTo("code", code)
            .whereEqualTo("uid", mAuth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {task ->
                if (!task.isEmpty) {
                    v.report.text = "reported"
                    v.report.isEnabled = false
                }
            }

        v.visit.setOnClickListener {

            val patientMap = HashMap<String, Any>()

            followed = !followed

            patientMap["lastEdited"] = FieldValue.serverTimestamp()
            patientMap["followed"] = followed


            db
                .collection("users")
                .document(mAuth.currentUser?.uid!!)
                .collection("patients")
                .document(code)
                .update(patientMap)
                .addOnSuccessListener {
                    if (followed) {
                        v.visit.text = "unfollow"
                    } else {
                        v.visit.text = "follow"
                    }
                }
        }

        v.report.setOnClickListener {
            context?.alert("Are you sure about this?") {
                yesButton {

                    val patientMap = HashMap<String, Any>()

                    patientMap["timestamp"] = FieldValue.serverTimestamp()
                    patientMap["reported"] = reported
                    patientMap["uid"] = mAuth.currentUser?.uid!!
                    patientMap["code"] = code

                    db
                        .collection("reports")
                        .add(patientMap)
                        .addOnSuccessListener {
                            v.report.text = "reported"
                            v.report.isEnabled = false
                        }

                    v.main.snackbar("Report sent!", "Undo") {
                        db
                            .collection("reports")
                            .whereEqualTo("code", code)
                            .whereEqualTo("uid", mAuth.currentUser?.uid!!)
                            .get()
                            .addOnSuccessListener { task ->
                                for (x in task.documents)
                                    x.reference.delete().addOnSuccessListener {
                                        v.report.text = "report"
                                        v.report.isEnabled = true
                                    }
                            }
                    }
                }
                noButton {}
            }?.show()
        }

        return v
    }


}
