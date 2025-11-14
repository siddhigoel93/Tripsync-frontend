package com.example.tripsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class recieved_RequestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recieved_activity_container)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.recieved_fragment_container, recieved_RequestsFragment())
                .commit()
        }
    }
}
