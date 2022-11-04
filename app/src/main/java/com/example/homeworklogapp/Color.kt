package com.example.homeworklogapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Color (
    val backgroundColor: Int,
    val textColor: Int
        ) : Parcelable