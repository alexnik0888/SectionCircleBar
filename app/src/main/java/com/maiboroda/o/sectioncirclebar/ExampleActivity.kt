package com.maiboroda.o.sectioncirclebar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        wheel.setListener { section -> Toast.makeText(this, section.toString(), Toast.LENGTH_SHORT).show() }
    }
}
