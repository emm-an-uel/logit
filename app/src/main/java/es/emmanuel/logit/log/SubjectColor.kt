package es.emmanuel.logit.log

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SubjectColor (
    val subject: String,
    val colorIndex: Int // note: this colorIndex refers to the color for colorList[colorIndex]
        ) : Parcelable