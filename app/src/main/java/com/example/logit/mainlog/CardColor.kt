package com.example.logit.mainlog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CardColor (
    val backgroundColor: Int,
    val textColor: Int
        ) : Parcelable