package com.dinesh.quizapp.data.model

data class Question(
    val questionText: String,
    val options: List<String>,
    val answer: String
)