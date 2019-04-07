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
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.item_history.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.wtf

/**
 * A simple [Fragment] subclass.
 */
class History : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private lateinit var mAdapter: Adapter
    private val items = ArrayList<Patient>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_history, container, false)

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
            .get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    v.main.snackbar("Database Error")
                    wtf("Could not get user's patients", task.exception)
                    return@addOnCompleteListener
                }

                if (task.result?.isEmpty!!) {
                    v.main.snackbar("You don't have any history")
                    return@addOnCompleteListener
                }

                for (x in task.result!!.documents) {

                    val id = x.id

                    db
                        .collection("patients")
                        .document(id)
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

            Glide.with(context).load(patient.avatar).into(holder.avatar)

            holder.name.text = patient.name

            holder.main.setOnClickListener {
                activity.changeFragment(Info(patient.id!!))
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
