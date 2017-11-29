package com.maiboroda.o.example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.maiboroda.o.example.R.*
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_example)

        wheel.setListener { section -> Toast.makeText(this, section.toString(), Toast.LENGTH_SHORT).show() }
    }
}
