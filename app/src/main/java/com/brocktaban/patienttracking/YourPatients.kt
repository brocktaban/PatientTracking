package com.brocktaban.patienttracking


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_your_patients.view.*
import kotlinx.android.synthetic.main.item_history.view.*
import org.jetbrains.anko.design.snackbar

class YourPatients : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private lateinit var mAdapter: Adapter
    private val items = ArrayList<Patient>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_your_patients, container, false)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        mAdapter = Adapter(items, context!!, (activity as MainActivity))

        v.recyclerview.layoutManager = LinearLayoutManager(context)
        v.recyclerview.adapter = mAdapter

        getPatients(v)

        return v
    }

    private fun getPatients(v: View) {
        db
            .collection("users")
            .document(mAuth.currentUser?.uid!!)
            .collection("patients")
            .whereEqualTo("followed", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    v.main.snackbar("You don't have any patients")
                    return@addOnSuccessListener
                }

                for (x in querySnapshot.documents) {
                    db
                        .collection("patients")
                        .document(x.id)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val patient = documentSnapshot.toObject(Patient::class.java)

                            patient?.let {
                                items.add(it)
                                mAdapter.notifyDataSetChanged()
                            }

                        }
                }
            }
    }


    private class Adapter(val items: ArrayList<Patient>, val context: Context, val activity: MainActivity) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_history,
                parent,
                false
            )
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val patient = items[position]

            Glide.with(context).load("https://api.adorable.io/avatars/285/${patient.id}.png").into(holder.avatar)

            holder.name.text = patient.name

            holder.main.setOnClickListener {
                activity.changeFragment(Info(patient.id!!), "Patient Info")
                activity.activateNavItem(1)
            }

        }

        override fun getItemCount() = items.size
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val main = view.item_main
        val avatar = view.item_avatar
        val name = view.item_name
        val more = view.item_more
    }
}
