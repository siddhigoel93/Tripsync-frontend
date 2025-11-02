package com.example.tripsync.api.models

data class EmergencySOSRequest(
    val bgroup: String,
    val allergies: String,
    val medical: String,
    val ename: String,
    val enumber: String,
    val erelation: String
)
