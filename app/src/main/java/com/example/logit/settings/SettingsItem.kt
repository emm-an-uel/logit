package com.example.logit.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SettingsItem (
    val item: String,
    var option: Int
        ) : Parcelable