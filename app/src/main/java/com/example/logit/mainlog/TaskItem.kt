package com.example.logit.mainlog

import com.example.logit.mainlog.ListItem

class TaskItem (
    val subject: String,
    val task: String,
    val dueDate: String,
    val notes: String
        ) : ListItem(TYPE_TASK)