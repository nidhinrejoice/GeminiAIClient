package com.nidhin.geminiclient.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.DecimalFormat

fun Float.twoDecimalPlaces(): Float {
    val df = DecimalFormat("#.#")
    df.maximumFractionDigits = 2
    df.minimumFractionDigits = 0

    return df.format(this).toFloat()
}

fun Double.twoDecimalPlaces(): String {
    val df = DecimalFormat("#.#")
    df.maximumFractionDigits = 2
    df.minimumFractionDigits = 0

    return df.format(this)
}

fun Double.formatCurrency(): String {
    val df = DecimalFormat("#.#")
    df.maximumFractionDigits = 2
    df.minimumFractionDigits = 0

    val baseCurrency =  "₹"
    return baseCurrency + df.format(this)

}

fun JSONObject.convertToReqBody(): RequestBody {
    return toString()
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
}