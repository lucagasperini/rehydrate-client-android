package com.lucagasperini.rehydrate

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import com.google.android.material.navigation.NavigationView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.Format


class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var plot_controller: PlotController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)

        val pref_editor = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE).edit()
        pref_editor.clear()
        pref_editor.apply()


        plot_controller = PlotController(findViewById(R.id.daily_plot), "Today", HourBarFormat());

        val drawer_layout : DrawerLayout = findViewById(R.id.drawer_layout)
        val nav_view : NavigationView = findViewById(R.id.nav_menu)


        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.app_name,R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        nav_view.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    val pref_editor = this.getSharedPreferences(UtilsStatic.PREF_AUTH, Context.MODE_PRIVATE).edit()
                    pref_editor.clear()
                    pref_editor.apply()
                    finish()
                }
            }

            true
        }

        val button_drink = findViewById<Button>(R.id.button_drink)
        button_drink.setOnClickListener {
            var drink_quantity = PreferenceManager.getDefaultSharedPreferences(this).getString("drink_quantity", "200")
            if (drink_quantity == null) {
                drink_quantity = "200"
            }
            ConnectionController.getInstance().send(
                this,
                applicationContext,
                drink_quantity.toInt(),
                System.currentTimeMillis() / 1000,
                {
                    Toast.makeText(this, "Drinked water", Toast.LENGTH_SHORT).show()
                    Log.i("ReHydrate", "Drinked water")
                    update_plot()
                }, {
                    Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
                })
        }

        update_plot()
    }

    fun update_plot() {
        plot_controller.clear_plot()
        ConnectionController.getInstance().plan(this, applicationContext, {
            val plan_request = Json.decodeFromString<PlanRequestModel>(it)

            var values: MutableMap<Int, Int> =  mutableMapOf()
            for (i in 0 .. 23) {
                values.put(i, 0)
            }

            plan_request.plan.forEach {
                values.put(it.date.removeSuffix(":00").toInt(), it.quantity)
            }
            val values_list: MutableList<Int> = mutableListOf()

            values.forEach {
                values_list.add(it.key, it.value)
            }

            plot_controller.update_plot_bar(values_list, "Plan", Color.MAGENTA)

        }, {
            Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
        }

        )

        ConnectionController.getInstance().receive_today(this, applicationContext, {
            val receive_request = Json.decodeFromString<List<ReceiveRequestModel>>(it)

            var values: MutableMap<Int, Int> =  mutableMapOf()
            for (i in 0 .. 23) {
                values.put(i, 0)
            }

            receive_request.forEach {
                val index = it.date.removeSuffix(":00").drop(11).toInt()
                values.put(index, it.quantity)
            }
            val values_list: MutableList<Int> = mutableListOf()

            values.forEach {
                values_list.add(it.key, it.value)
            }

            plot_controller.update_plot_bar(values_list, "History", Color.CYAN)


        }, {
            Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
        }

        )
    }
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}