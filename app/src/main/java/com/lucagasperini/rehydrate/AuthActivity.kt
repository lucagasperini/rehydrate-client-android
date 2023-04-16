package com.lucagasperini.rehydrate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_layout)

        val load_token = getSharedPreferences(
            UtilsStatic.PREF_AUTH,
            MODE_PRIVATE
        ).getString(UtilsStatic.PREF_AUTH_TOKEN, null)

        val load_url = getSharedPreferences(
            UtilsStatic.PREF_AUTH,
            MODE_PRIVATE
        ).getString(UtilsStatic.PREF_AUTH_URL, null)

        if(load_token != null && load_url != null) {
            ConnectionController.getInstance().token = load_token
            ConnectionController.getInstance().url = load_url
            startActivity(Intent(this, MainActivity::class.java))
        }

        val button_login = findViewById<Button>(R.id.button_login)
        button_login.setOnClickListener {
            val input_user = findViewById<EditText>(R.id.edittext_user).text.toString()
            val input_pass = findViewById<EditText>(R.id.edittext_password).text.toString()
            val input_url = findViewById<EditText>(R.id.edittext_server_url).text.toString()
            ConnectionController.getInstance().url = input_url
            ConnectionController.getInstance().auth(
                this,
                applicationContext,
                input_user,
                input_pass,
                {
                    ConnectionController.getInstance().token = it
                    val shared_pref_editor = getSharedPreferences(
                        UtilsStatic.PREF_AUTH,
                        MODE_PRIVATE
                    ).edit()

                    shared_pref_editor.putString(
                        UtilsStatic.PREF_AUTH_TOKEN,
                        ConnectionController.getInstance().token
                    )

                    shared_pref_editor.putString(
                        UtilsStatic.PREF_AUTH_URL,
                        ConnectionController.getInstance().url
                    )

                    shared_pref_editor.apply()
                    startActivity(Intent(this, MainActivity::class.java))
                }, {
                    Toast.makeText(this, "Failed to login!", Toast.LENGTH_SHORT).show()
                }

            )
        }
    }
    override fun onPause() {
        super.onPause()
        val input_user = findViewById<EditText>(R.id.edittext_user).text.toString()
        val input_pass = findViewById<EditText>(R.id.edittext_password).text.toString()
        val input_url = findViewById<EditText>(R.id.edittext_server_url).text.toString()

        val pref_editor = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE).edit()
        pref_editor.putString(UtilsStatic.SAVE_AUTH_SERVER_URL, input_url)
        pref_editor.putString(UtilsStatic.SAVE_AUTH_PASS, input_pass)
        pref_editor.putString(UtilsStatic.SAVE_AUTH_USER, input_user)
        pref_editor.apply()
    }

    override fun onResume() {
        super.onResume()
        val view_input_user = findViewById<EditText>(R.id.edittext_user)
        val view_input_pass = findViewById<EditText>(R.id.edittext_password)
        val view_input_url = findViewById<EditText>(R.id.edittext_server_url)

        val pref = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE)
        view_input_user.setText(pref.getString(UtilsStatic.SAVE_AUTH_USER, "") ?: "")
        view_input_pass.setText(pref.getString(UtilsStatic.SAVE_AUTH_PASS, "") ?: "")
        view_input_url.setText(pref.getString(UtilsStatic.SAVE_AUTH_SERVER_URL, "") ?: "")
    }

}