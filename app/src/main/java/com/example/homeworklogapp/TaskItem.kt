package com.example.homeworklogapp

class TaskItem (
    val subject: String,
    val task: String,
    val dueDate: String,
    val notes: String
        ) : ListItem(TYPE_TASK)