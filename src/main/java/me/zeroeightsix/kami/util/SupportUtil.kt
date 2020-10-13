package me.zeroeightsix.kami.util

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.result.Result;
import java.io.File

object SupportUtil {
    fun uploadJsonFile(fileToUpload: File) {
        Fuel.post("localhost:8000")
                .header(Headers.CONTENT_TYPE, "text/plain")
                .body(fileToUpload)
                .also { println(it) }
                .response { result ->
                    when (result) {
                        is Result.Failure -> {
                            println(result)
                        }
                        is Result.Success -> {
                            println(result)
                        }
                    }
                }
    }
}