package com.example.logit

open class ListItem (
    val type: Int
){
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_TASK = 1
    }
}