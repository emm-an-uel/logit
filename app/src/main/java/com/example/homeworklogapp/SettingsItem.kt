package com.example.homeworklogapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SettingsItem (
    val item: String,
    var status: Boolean
        ) : Parcelable