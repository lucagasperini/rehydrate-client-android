package com.lucagasperini.rehydrate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    lateinit var plot_controller: PlotController
    var history_time_start: String = "today"
    var history_time_end: String = "none"
    var history_sum: String = UtilsStatic.OPTION_SUM_HOURLY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.history_layout)

        plot_controller = PlotController(findViewById(R.id.history_plot), "History", HourBarFormat());


        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {

            val input_time_start: EditText = findViewById(R.id.edittext_time_start)
            val input_time_end: EditText = findViewById(R.id.edittext_time_end)

            time_picker(input_time_start)
            time_picker(input_time_end)

            val button_update: Button = findViewById(R.id.button_update)

            val data = arrayOf(
                UtilsStatic.OPTION_SUM_HOURLY,
                UtilsStatic.OPTION_SUM_DAILY,
                UtilsStatic.OPTION_SUM_WEEKLY,
                UtilsStatic.OPTION_SUM_MONTHLY,
                UtilsStatic.OPTION_SUM_YEARLY
            )

            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data)
            val spinner_sum = findViewById<Spinner>(R.id.spinner_sum)
            spinner_sum.adapter = adapter

            spinner_sum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    history_sum = data[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    history_sum = UtilsStatic.OPTION_SUM_HOURLY
                }
            }

            button_update.setOnClickListener {
                val date_format = SimpleDateFormat("yyyy/MM/dd HH:mm")

                val input_time_start_string = input_time_start.text.toString()
                if (input_time_start_string != "") {
                    try {
                        val input_time_start_time = date_format.parse(input_time_start_string)
                        history_time_start = (input_time_start_time.time / 1000).toString()
                    } catch (e: ParseException) {
                        Toast.makeText(this, "Invalid time start format", Toast.LENGTH_SHORT).show()
                    }
                }
                val input_time_end_string = input_time_end.text.toString()
                if (input_time_end_string != "") {
                    try {
                        val input_time_end_time = date_format.parse(input_time_end_string)
                        history_time_end = (input_time_end_time.time / 1000).toString()
                    } catch (e: ParseException) {
                        Toast.makeText(this, "Invalid time end format", Toast.LENGTH_SHORT).show()
                    }
                }
                update_plot()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        update_plot()
    }

    fun time_picker(input_time: EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        input_time.setOnClickListener {
            val date_picker_dialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener {
                    date_view,
                    fetch_year,
                    fetch_month,
                    fetch_day ->
                    run {
                        val time_picker_dialog = TimePickerDialog(
                            this,
                            TimePickerDialog.OnTimeSetListener { time_view,
                                                                 fetch_hour,
                                                                 fetch_minute ->
                                run {
                                    input_time.setText("$fetch_year/$fetch_month/$fetch_day $fetch_hour:$fetch_minute")
                                }
                            },
                            hour,
                            minute,
                            true
                        )
                        time_picker_dialog.show()

                    }

                },
                year,
                month,
                day
            )

            date_picker_dialog.show()
        }
    }

    fun update_plot() {
        plot_controller.set_domain_label_distance(40.0f)
        plot_controller.clear_plot()

        ConnectionController.getInstance().receive(this, applicationContext,
            history_time_start,
            history_time_end,
            history_sum,
            {
                val receive_request = Json.decodeFromString<List<ReceiveRequestModel>>(it)

                var y_values: MutableList<Int> = mutableListOf()
                var x_values: MutableList<String> =  mutableListOf()


                receive_request.forEach {
                    x_values.add(it.date)
                    y_values.add(it.quantity)
                }
                plot_controller.set_x_format(HistoryHourBarFormat(x_values))

                plot_controller.update_plot_line(y_values, "History", Color.CYAN)
            }, {
                Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
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