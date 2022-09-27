package com.example.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun loginClick(v: View) {
        val name = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val intent = Intent(this, SynchronizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.putExtra("name", name)
        startActivity(intent)
    }
}