package es.emmanuel.logit.log

class TaskItem (
    val subject: String,
    val task: String,
    val dueDate: String,
    val notes: String
        ) : ListItem(TYPE_TASK)