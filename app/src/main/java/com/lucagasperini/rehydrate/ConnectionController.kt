package com.lucagasperini.rehydrate

import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.net.URLEncoder

class ConnectionController private constructor() {
    var url = ""
    var token = ""
    companion object {
        private val instance = ConnectionController()


        @Synchronized
        fun getInstance(): ConnectionController {
            return instance
        }
    }

    private fun send_request(input_data: Data, owner: androidx.lifecycle.LifecycleOwner, work_manager: WorkManager, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {
        val request = OneTimeWorkRequestBuilder<ConnectionWorker>().setInputData(input_data).build()
        work_manager.enqueue(request)

        val workLiveData = work_manager.getWorkInfoByIdLiveData(request.id)
        workLiveData.observe(owner, Observer { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                ifsuccess(workInfo.outputData.getString("response") ?: throw IllegalArgumentException("Result has no response!"))

            } else if (workInfo.state == WorkInfo.State.FAILED) {
                val response_code = workInfo.outputData.getInt("response_code", 0)
                Log.e("ReHydrate", "Error on http connection with response code: $response_code")
                iffail(response_code)
            }
        })
    }

    fun auth(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, user: String, pass: String, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "p=" + URLEncoder.encode(pass, "UTF-8") + "&" +
                "u=" + URLEncoder.encode(user, "UTF-8")

        send_request(
            Data.Builder()
                .putString("url", url)
                .putString("param", param)
                .build(),
            owner,
            WorkManager.getInstance(context),
            ifsuccess,
            iffail
        )
    }

    fun send(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, quantity: Int, time: Long, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=send" + "&" +
                "quantity=" + quantity + "&" +
                "time=" + time

        send_request(
            Data.Builder()
                .putString("url", url)
                .putString("param", param)
                .build(),
            owner,
            WorkManager.getInstance(context),
            ifsuccess,
            iffail
        )

    }

    fun plan(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=plan"

        send_request(
            Data.Builder()
                .putString("url", url)
                .putString("param", param)
                .build(),
            owner,
            WorkManager.getInstance(context),
            ifsuccess,
            iffail
        )
    }

    fun receive_today(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=receive&time_start=today&sum=hourly"

        send_request(
            Data.Builder()
                .putString("url", url)
                .putString("param", param)
                .build(),
            owner,
            WorkManager.getInstance(context),
            ifsuccess,
            iffail
        )
    }

    fun receive(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, time_start: String, time_end: String, sum: String, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=receive"+"&"+
                "time_start="+ time_start + "&" +
                "time_end="+ time_end + "&" +
                "sum=" + sum

        send_request(
            Data.Builder()
                .putString("url", url)
                .putString("param", param)
                .build(),
            owner,
            WorkManager.getInstance(context),
            ifsuccess,
            iffail
        )
    }

}