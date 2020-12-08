package com.example.samplehlsplayer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object Coroutines {

    val job = Job()

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO + job).launch {
            work()
        }


    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main + job).launch {
            work()
        }

}