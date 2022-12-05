package com.example.homeworklogapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Task(
        val id: String,
        val subject: String,
        val task: String,
        val dueDate: String,
        val dateInt: Int,
        var status: Boolean,
        val notes: String
        ) : Parcelable