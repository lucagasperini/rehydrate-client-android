package com.lucagasperini.rehydrate

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ConnectionWorker(context: Context, user_param: WorkerParameters) :
    Worker(context, user_param) {

    override fun doWork(): Result {
        // setup url for request work
        val url = URL(
            inputData.getString("url") ?: throw IllegalArgumentException("Cannot fetch url!")
        )
        // setup parameters for request work
        val param = inputData.getString("param") ?: throw IllegalArgumentException("Cannot fetch param!")

        Log.i("ReHydrate", "Sending POST parameters: $param")

        // http request
        with(url.openConnection() as HttpURLConnection) {
            // default is GET, we will make POST request to improve security
            requestMethod = "POST"

            // write to socket
            val wr = OutputStreamWriter(getOutputStream())
            wr.write(param)
            wr.flush()

            // if HTTP response code is not 200, so HTTP_OK, there is some error, then just return fail
            if (responseCode != 200) {
                val result_data = Data.Builder()
                    .putInt("response_code", responseCode)
                    .build()

                return Result.failure(result_data)
            }

            // if HTTP response is OK, then fetch response by this stream
            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                Log.i("ReHydrate", "Response : $response")

                // data is response
                // TODO: if token will be json too, we can decode all json response here!
                val result_data = Data.Builder()
                    .putString("response", response.toString())
                    .build()
                // return data with success
                return Result.success(result_data)

            }

        }
    }

    }