package com.relay.trakt.trakttvapiservice

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.lang.Exception
import java.lang.ref.WeakReference

fun SharedPreferences.putString(key: String, value: String) {
    edit {
        putString(key, value)
        commit()
    }
}

fun WeakReference<Context>.doWithContext(block: (context: Context) -> Unit) {
    val context = get()
    if (context == null) {
        throw NullPointerException()
    } else {
        block(context)
    }
}

fun Exception.getNotNullMessage(): String = message ?: "Null"