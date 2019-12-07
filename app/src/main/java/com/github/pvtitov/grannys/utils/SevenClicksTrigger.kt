package com.github.pvtitov.grannys.utils

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class SevenClicksTrigger {

    val secondInMillis = 1000L

    internal var eventQueue: Queue<Event> = ConcurrentLinkedQueue()

    fun doOnEvent(e: Event, r: () -> Unit) {
        eventQueue.add(e)
        if (eventQueue.size >= 7) {
            if (e.time - eventQueue.peek().time < 2 * secondInMillis) {
                eventQueue.clear()
                r.invoke()
            } else {
                do {
                    eventQueue.remove()
                } while (eventQueue.size >= 7)
            }
        }
    }

    fun doOnEvent(x: Float, y: Float, t: Long, r: () -> Unit) {
        doOnEvent(Event(x, y, t), r)
    }

    class Event(val x: Float, val y: Float, val time: Long)
}
