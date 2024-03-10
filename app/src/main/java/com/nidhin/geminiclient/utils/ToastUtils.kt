package com.nidhin.geminiclient.utils

import android.app.Activity
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.nidhin.geminiclient.R

fun Toast.showCustomToast(activity: Activity, message: String) {
        val layout = activity.layoutInflater.inflate(
            R.layout.view_toast,
            activity.findViewById(R.id.toast_container)
        )

        // set the text of the TextView of the message
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        // use the application extension function
        this.apply {
            setGravity(Gravity.BOTTOM, 0, 40)
            duration = LENGTH_LONG
            view = layout
            show()
        }
    }

    fun Activity.showToast(message: String, toast : Toast){
        toast.showCustomToast(this, message)
    }