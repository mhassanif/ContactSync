package com.example.webapiex

data class Contact(
    val id: String,
    val name: String,
    val roll: String,
    val email: String,
    val dp: String = ""
)