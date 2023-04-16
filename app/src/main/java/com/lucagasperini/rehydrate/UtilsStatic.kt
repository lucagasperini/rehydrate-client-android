package com.lucagasperini.rehydrate

import android.graphics.Color
import android.widget.Toast
import com.androidplot.util.PixelUtils
import com.androidplot.xy.BarFormatter
import com.androidplot.xy.BarRenderer
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYPlot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UtilsStatic {
    companion object {
        val PREF_AUTH = "pref_auth"
        val PREF_AUTH_TOKEN = "auth_token"
        val PREF_AUTH_URL = "auth_url"

        val SAVE_AUTH = "save_auth"
        val SAVE_AUTH_SERVER_URL = "save_server_url"
        val SAVE_AUTH_PASS = "save_pass"
        val SAVE_AUTH_USER = "save_user"

        val OPTION_SUM_HOURLY = "hourly"
        val OPTION_SUM_DAILY = "daily"
        val OPTION_SUM_WEEKLY = "weekly"
        val OPTION_SUM_MONTHLY = "monthly"
        val OPTION_SUM_YEARLY = "yearly"
    }
}