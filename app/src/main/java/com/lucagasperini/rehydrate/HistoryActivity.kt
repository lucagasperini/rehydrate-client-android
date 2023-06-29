package com.lucagasperini.rehydrate

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    lateinit var plot_controller: PlotController
    // static so information will be no erased on rotation
    companion object {
        var history_time_start: String = "none"
        var history_time_end: String = "none"
        var history_sum: String = UtilsStatic.OPTION_SUM_DAILY
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.history_layout)

        // init plot controller for history
        plot_controller = PlotController(findViewById(R.id.history_plot), "", HourBarFormat())

        // if is not on landscape
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // setup time widget
            val input_time_start: EditText = findViewById(R.id.edittext_time_start)
            val input_time_end: EditText = findViewById(R.id.edittext_time_end)

            date_picker(input_time_start)
            date_picker(input_time_end)

            // setup button update
            val button_update: Button = findViewById(R.id.button_update)
            button_update.setOnClickListener {// button update click action
                // setup date format
                val date_format = SimpleDateFormat("yyyy/MM/dd")
                // get start time string
                val input_time_start_string = input_time_start.text.toString()
                // if is not empty, then try to parse it, and convert to unix epoch
                if (input_time_start_string != "") {
                    try {
                        val input_time_start_time = date_format.parse(input_time_start_string)
                        history_time_start = (input_time_start_time.time / 1000).toString()
                    } catch (e: ParseException) {
                        Toast.makeText(this, this.getString(R.string.ui_toast_invalid_time_start_format), Toast.LENGTH_SHORT).show()
                    }
                }
                // get end time string
                val input_time_end_string = input_time_end.text.toString()
                // if is not empty, then try to parse it, and convert to unix epoch
                if (input_time_end_string != "") {
                    try {
                        val input_time_end_time = date_format.parse(input_time_end_string)
                        history_time_end = (input_time_end_time.time / 1000).toString()
                    } catch (e: ParseException) {
                        Toast.makeText(this, this.getString(R.string.ui_toast_invalid_time_end_format), Toast.LENGTH_SHORT).show()
                    }
                }

                // then update plot with new filters
                update_plot()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // update plot on form creation
        update_plot()
    }

    // setup a text edit into a clickable date picker
    fun date_picker(input_date: EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // if text edit is clicked, then show date picker
        input_date.setOnClickListener {
            val date_picker_dialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { date_view,
                                                     fetch_year,
                                                     fetch_month,
                                                     fetch_day ->
                    run {
                        // when selected a date, fill it to text edit
                        val y = fetch_year
                        val m = fetch_month + 1
                        val d = fetch_day
                        input_date.setText("$y/$m/$d")
                    }

                },
                year,
                month,
                day
            )

            date_picker_dialog.show()
        }
    }

    fun get_current_sum(): String {
        // if is landscape, use last value and return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return history_sum
        }
        //if is not landscape, try to fetch current value from spinner
        val spinner_sum = findViewById<Spinner>(R.id.spinner_sum)
        val history_sum_selected: String = spinner_sum.selectedItem.toString()

        if(history_sum_selected == this.getString(R.string.ui_history_sum_hourly)) {
            history_sum = UtilsStatic.OPTION_SUM_HOURLY
            return UtilsStatic.OPTION_SUM_HOURLY
        } else if (history_sum_selected == this.getString(R.string.ui_history_sum_daily)) {
            history_sum = UtilsStatic.OPTION_SUM_DAILY
            return UtilsStatic.OPTION_SUM_DAILY
        } else if (history_sum_selected == this.getString(R.string.ui_history_sum_weekly)) {
            history_sum = UtilsStatic.OPTION_SUM_WEEKLY
            return UtilsStatic.OPTION_SUM_WEEKLY
        } else if (history_sum_selected == this.getString(R.string.ui_history_sum_monthly)) {
            history_sum = UtilsStatic.OPTION_SUM_MONTHLY
            return UtilsStatic.OPTION_SUM_MONTHLY
        } else if (history_sum_selected == this.getString(R.string.ui_history_sum_yearly)) {
            history_sum = UtilsStatic.OPTION_SUM_YEARLY
            return UtilsStatic.OPTION_SUM_YEARLY
        }
        // if not match, return a default value
        history_sum = UtilsStatic.OPTION_SUM_DAILY
        return UtilsStatic.OPTION_SUM_DAILY
    }

    fun update_plot() {
        plot_controller.clear_plot()

        // connection controller receive request
        ConnectionController.getInstance().receive(
            this,
            applicationContext,
            history_time_start, // unix epoch start time if present
            history_time_end, // unix epoch end time if present
            get_current_sum(), // type of sum
            {
                // try to decode response
                val receive_request = Json.decodeFromString<List<ReceiveRequestModel>>(it)

                //generate a list of Int for y values on plot
                val y_values: MutableList<Int> = mutableListOf()
                //generate a list of String for x values on plot
                val x_values: MutableList<String> =  mutableListOf()

                // fill those list
                receive_request.forEach {
                    x_values.add(it.date)
                    y_values.add(it.quantity)
                }

                // set label for history
                plot_controller.set_x_format(HistoryHourBarFormat(x_values))
                // set y value into plot
                plot_controller.update_plot_line(y_values, "", Color.CYAN)
            }, {
                // if receive fails
                Toast.makeText(this, this.getString(R.string.ui_toast_connection_error), Toast.LENGTH_SHORT).show()
            }

        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}