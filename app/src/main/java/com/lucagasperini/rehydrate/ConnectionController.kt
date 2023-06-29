package com.lucagasperini.rehydrate

import android.util.Log
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

        // this will be static, so there is only one instance for all the application lifetime
        @Synchronized
        fun getInstance(): ConnectionController {
            return instance
        }
    }
    // send a generic request on the server, with callback function if success or if fails
    private fun send_request(owner: androidx.lifecycle.LifecycleOwner, work_manager: WorkManager, param: String, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {
        // build data for send request with url and parameters
        val data = Data.Builder()
            .putString("url", url)
            .putString("param", param)
            .build()
        val request = OneTimeWorkRequestBuilder<ConnectionWorker>().setInputData(data).build()
        // append request on work manager
        work_manager.enqueue(request)
        // try to get live data from work manager
        val workLiveData = work_manager.getWorkInfoByIdLiveData(request.id)
        // manage if request is succeeded or failed
        workLiveData.observe(owner, Observer { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                // go to success callback function with response as String
                ifsuccess(workInfo.outputData.getString("response") ?: throw IllegalArgumentException("Result has no response!"))

            } else if (workInfo.state == WorkInfo.State.FAILED) {
                // go to fail callback function with response code as Int
                val response_code = workInfo.outputData.getInt("response_code", 0)
                Log.e("ReHydrate", "Error on http connection with response code: $response_code")
                iffail(response_code)
            }
        })
    }

    // hello request to get server information
    fun hello(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {
        send_request(
            owner,
            WorkManager.getInstance(context),
            "type=hello",
            ifsuccess,
            iffail
        )
    }

    // auth request to login on server and get access token
    fun auth(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, user: String, pass: String, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {
        val param = "p=" + URLEncoder.encode(pass, "UTF-8") + "&" +
                "u=" + URLEncoder.encode(user, "UTF-8")

        send_request(
            owner,
            WorkManager.getInstance(context),
            param,
            ifsuccess,
            iffail
        )
    }

    // send request to add a drink entry
    fun send(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, quantity: Int, time: Long, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=send" + "&" +
                "quantity=" + quantity + "&" +
                "time=" + time

        send_request(
            owner,
            WorkManager.getInstance(context),
            param,
            ifsuccess,
            iffail
        )

    }

    // plan request to get a plan for today
    fun plan(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=plan"

        send_request(
            owner,
            WorkManager.getInstance(context),
            param,
            ifsuccess,
            iffail
        )
    }
    // receive today request to get today history
    fun receive_today(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=receive&time_start=today&sum=hourly"

        send_request(
            owner,
            WorkManager.getInstance(context),
            param,
            ifsuccess,
            iffail
        )
    }
    // receive request for generic history
    fun receive(owner: androidx.lifecycle.LifecycleOwner, context: android.content.Context, time_start: String, time_end: String, sum: String, ifsuccess: (String) -> Unit, iffail: (Int) -> Unit) {

        val param = "token=" + URLEncoder.encode(token, "UTF-8") + "&" +
                "type=receive"+"&"+
                "time_start="+ time_start + "&" +
                "time_end="+ time_end + "&" +
                "sum=" + sum

        send_request(
            owner,
            WorkManager.getInstance(context),
            param,
            ifsuccess,
            iffail
        )
    }

}