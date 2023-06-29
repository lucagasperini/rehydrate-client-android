package com.lucagasperini.rehydrate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lucagasperini.rehydrate.UtilsStatic.Companion.APP_SRV_NAME
import com.lucagasperini.rehydrate.UtilsStatic.Companion.APP_SRV_VERSION
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AuthActivity : AppCompatActivity() {

    /**
     * Function to handle auth to server
     */
    fun auth(user: String, pass: String) {
        // call connection controller to auth with user and pass
        ConnectionController.getInstance().auth(
            this,
            applicationContext,
            user,
            pass,
            {// if auth success
                // token is server response
                ConnectionController.getInstance().token = it
                // open auth preferences
                val shared_pref_editor = getSharedPreferences(
                    UtilsStatic.PREF_AUTH,
                    MODE_PRIVATE
                ).edit()

                // add token to preferences
                shared_pref_editor.putString(
                    UtilsStatic.PREF_AUTH_TOKEN,
                    ConnectionController.getInstance().token
                )

                // add url to preferences
                shared_pref_editor.putString(
                    UtilsStatic.PREF_AUTH_URL,
                    ConnectionController.getInstance().url
                )

                // applies changes
                shared_pref_editor.apply()
                // start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            }, {// if auth fail
                Toast.makeText(this, this.getString(R.string.ui_toast_failed_login), Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Actions to setup auth GUI
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_layout)
        // load previous token if is set
        val load_token = getSharedPreferences(
            UtilsStatic.PREF_AUTH,
            MODE_PRIVATE
        ).getString(UtilsStatic.PREF_AUTH_TOKEN, null)

        // load previous url if is set
        val load_url = getSharedPreferences(
            UtilsStatic.PREF_AUTH,
            MODE_PRIVATE
        ).getString(UtilsStatic.PREF_AUTH_URL, null)

        // if token and url are set, then start MainActivity
        if(load_token != null && load_url != null) {
            ConnectionController.getInstance().token = load_token
            ConnectionController.getInstance().url = load_url
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Action when button login is pressed
        val button_login = findViewById<Button>(R.id.button_login)
        button_login.setOnClickListener {
            // get all field on the form
            val input_user = findViewById<EditText>(R.id.edittext_user).text.toString()
            val input_pass = findViewById<EditText>(R.id.edittext_password).text.toString()
            val input_url = findViewById<EditText>(R.id.edittext_server_url).text.toString()
            // set the url of the server on connection controller instance
            ConnectionController.getInstance().url = input_url
            // try to get a hello from the server
            ConnectionController.getInstance().hello(
                this,
                applicationContext,
                {// if hello success
                    try {
                        // try to decode hello response
                        val hello = Json.decodeFromString<HelloModel>(it)
                        // if name and version is compatible to what is expected from this client, then auth
                        if(hello.name == APP_SRV_NAME && hello.version == APP_SRV_VERSION) {
                            auth(input_user, input_pass)
                        }
                    } catch (ex: Exception) {
                        // if cannot decode, then this response is not a valid json
                        Toast.makeText(this, this.getString(R.string.ui_toast_invalid_respose_server), Toast.LENGTH_SHORT).show()
                    }
                },
                {// if hello fail
                    // if cannot connect to server
                    Toast.makeText(this, this.getString(R.string.ui_toast_cannot_reach_server), Toast.LENGTH_SHORT).show()
                }
            )

        }
    }

    /**
     * Action to do before application goes background
     */
    override fun onPause() {
        super.onPause()
        // get all field on the form
        val input_user = findViewById<EditText>(R.id.edittext_user).text.toString()
        val input_pass = findViewById<EditText>(R.id.edittext_password).text.toString()
        val input_url = findViewById<EditText>(R.id.edittext_server_url).text.toString()

        // open auth preferences
        val pref_editor = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE).edit()
        // put preferences to save
        pref_editor.putString(UtilsStatic.SAVE_AUTH_SERVER_URL, input_url)
        pref_editor.putString(UtilsStatic.SAVE_AUTH_PASS, input_pass)
        pref_editor.putString(UtilsStatic.SAVE_AUTH_USER, input_user)
        // apply changes
        pref_editor.apply()
    }

    /**
     * Action to do after application goes foreground
     */
    override fun onResume() {
        super.onResume()
        // get all field on the form
        val view_input_user = findViewById<EditText>(R.id.edittext_user)
        val view_input_pass = findViewById<EditText>(R.id.edittext_password)
        val view_input_url = findViewById<EditText>(R.id.edittext_server_url)
        // open auth preferences
        val pref = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE)
        // load preferences
        view_input_user.setText(pref.getString(UtilsStatic.SAVE_AUTH_USER, "") ?: "")
        view_input_pass.setText(pref.getString(UtilsStatic.SAVE_AUTH_PASS, "") ?: "")
        view_input_url.setText(pref.getString(UtilsStatic.SAVE_AUTH_SERVER_URL, "") ?: "")
    }

}