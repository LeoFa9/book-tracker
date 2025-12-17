package com.example.laba_9

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: Int? = null,
    val title: String,
    val year: Int,
    val author: String,
    val status: String, // "want", "reading", "dropped", "read"
    val dateAdded: String,
    val note: String
) : Parcelable


