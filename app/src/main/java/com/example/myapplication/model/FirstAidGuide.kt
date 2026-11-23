package com.example.myapplication.model

data class FirstAidGuide(
    val title: String,
    val category: String,
    val symptoms: String,
    val emergencySteps: List<String>,
    val donts: List<String>
)