package com.example.logit

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Task(
        val id: String,
        val subject: String,
        val task: String,
        val dueDate: String,
        val dueDateInt: Int,
        var completed: Boolean,
        val notes: String,
        var completedDate: Int
        ) : Parcelable