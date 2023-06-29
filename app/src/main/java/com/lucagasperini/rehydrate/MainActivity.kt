package com.lucagasperini.rehydrate

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var plot_controller: PlotController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        // clear eventually saved preferences with username and password
        val pref_editor = getSharedPreferences(UtilsStatic.SAVE_AUTH, MODE_PRIVATE).edit()
        pref_editor.clear()
        pref_editor.apply()

        // create plot with bars
        plot_controller = PlotController(findViewById(R.id.daily_plot), "", HourBarFormat())

        // setup nav menu
        val drawer_layout : DrawerLayout = findViewById(R.id.drawer_layout)
        val nav_view : NavigationView = findViewById(R.id.nav_menu)

        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.app_name,R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Action to do when some item is selected on nav menu
        nav_view.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_settings -> {
                    // go to settings
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    // go to history
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    // go to just logout and exit from activity (so back to auth)
                    val pref_editor = this.getSharedPreferences(UtilsStatic.PREF_AUTH, Context.MODE_PRIVATE).edit()
                    pref_editor.clear()
                    pref_editor.apply()
                    finish()
                }
            }
            true
        }

        // setup action when drink button is pressed
        val button_drink = findViewById<Button>(R.id.button_drink)
        button_drink.setOnClickListener {
            // get from preference the quantity of water
            var drink_quantity = PreferenceManager.getDefaultSharedPreferences(this).getString("drink_quantity", "200")
            // if cannot fetch a preference, then use default
            if (drink_quantity == null) {
                drink_quantity = "200"
            }
            // send request
            ConnectionController.getInstance().send(
                this,
                applicationContext,
                drink_quantity.toInt(),
                System.currentTimeMillis() / 1000,
                {// if send success
                    Toast.makeText(this, this.getString(R.string.ui_toast_drank_water), Toast.LENGTH_SHORT).show()
                    // update plot
                    update_plot()
                }, {// if send fail
                    Toast.makeText(this, this.getString(R.string.ui_toast_connection_error), Toast.LENGTH_SHORT).show()
                })
        }
        // update plot when create the form
        update_plot()
    }

    fun update_plot() {
        // clear old plot eventually
        plot_controller.clear_plot()
        // request a plan
        ConnectionController.getInstance().plan(
            this,
            applicationContext, {// if plan success
                // try to decode response
                // TODO: try catch
                val plan_request = Json.decodeFromString<PlanRequestModel>(it)

                // fill x values into a map
                val values: MutableMap<Int, Int> =  mutableMapOf()
                for (i in 0 .. 23) {
                    values.put(i, 0)
                }
                // fill y value into a map
                plan_request.plan.forEach {
                    // get date without all information except to hour
                    values.put(it.date.removeSuffix(":00").toInt(), it.quantity)
                }
                // convert the map into a list
                val values_list: MutableList<Int> = mutableListOf()
                values.forEach {
                    values_list.add(it.key, it.value)
                }

                // give to android plot the list of values as plan
                plot_controller.update_plot_bar(values_list, this.getString(R.string.ui_plot_line_plan), Color.MAGENTA)

        }, {// if plan fail
            Toast.makeText(this, this.getString(R.string.ui_toast_connection_error), Toast.LENGTH_SHORT).show()
        }
        )

        // request receive today
        ConnectionController.getInstance().receive_today(
            this,
            applicationContext, {// if receive today success
                // try to decode response
                // TODO: try catch
                val receive_request = Json.decodeFromString<List<ReceiveRequestModel>>(it)
                    // fill x values into a map
                val values: MutableMap<Int, Int> =  mutableMapOf()
                for (i in 0 .. 23) {
                    values.put(i, 0)
                }
                // fill y values into a map
                receive_request.forEach {
                    // get date without all information except to hour
                    val index = it.date.removeSuffix(":00").drop(11).toInt()
                    values.put(index, it.quantity)
                }

                // convert the map into a list
                val values_list: MutableList<Int> = mutableListOf()
                values.forEach {
                    values_list.add(it.key, it.value)
                }

                // give to android plot the list of values as history
                plot_controller.update_plot_bar(values_list, this.getString(R.string.ui_plot_line_history), Color.CYAN)
        }, {//if receive today fail
            Toast.makeText(this, this.getString(R.string.ui_toast_connection_error), Toast.LENGTH_SHORT).show()
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