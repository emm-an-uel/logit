package es.emmanuel.logit.log

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class ListItem (
    val type: Int
): Parcelable {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_TASK = 1
    }
}