package com.relay.trakt.tv

import android.view.View
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun View.visible(){
    this.visibility=View.VISIBLE
}

fun View.gone(){
    this.visibility=View.GONE
}

fun View.invisible(){
    this.visibility=View.GONE
}

inline fun <T> withAll(vararg receiver: T, block: T.() -> Unit) {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    receiver.forEach {
        //        with(it, block)
        it.runCatching(block)
    }
}
