package com.example.logit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ParentActivity : AppCompatActivity() {
    private lateinit var binding: ParentActivity2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent2)
    }
}