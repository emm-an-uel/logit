package com.example.logit

class TaskItem (
    val subject: String,
    val task: String,
    val dueDate: String,
    val notes: String
        ) : ListItem(TYPE_TASK)