package com.brocktaban.patienttracking

data class Patient(
    var id: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var name: String? = null,
    var status: String? = null,
    var medicines: List<String>? = null
)