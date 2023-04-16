package com.lucagasperini.rehydrate

import android.annotation.SuppressLint
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
import java.net.URLEncoder

class ConnectionWorker(context: Context, user_param: WorkerParameters) :
    Worker(context, user_param) {

    override fun doWork(): Result {
        val url = URL(
            inputData.getString("url") ?: throw IllegalArgumentException("Cannot fetch url!")
        )

        val param = inputData.getString("param") ?: throw IllegalArgumentException("Cannot fetch param!")


        Log.i("ReHydrate", "Sending POST parameters: $param")


        with(url.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

            val wr = OutputStreamWriter(getOutputStream());
            wr.write(param);
            wr.flush();

            if (responseCode != 200) {
                val result_data = Data.Builder()
                    .putInt("response_code", responseCode)
                    .build()

                return Result.failure(result_data)
            }

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                Log.i("ReHydrate", "Response : $response")


                val result_data = Data.Builder()
                    .putString("response", response.toString())
                    .build()

                return Result.success(result_data)

            }

        }
    }

    }