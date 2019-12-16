package com.github.pvtitov.grannys.utils

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class MultipleClicksTrigger {

    val duration = 1000L
    val numberOfClicks = 8

    internal var eventQueue: Queue<Event> = ConcurrentLinkedQueue()

    fun doOnEvent(e: Event, r: () -> Unit) {
        eventQueue.add(e)
        if (eventQueue.size >= numberOfClicks) {
            if (e.time - eventQueue.peek().time < duration) {
                eventQueue.clear()
                r.invoke()
            } else {
                do {
                    eventQueue.remove()
                } while (eventQueue.size >= numberOfClicks)
            }
        }
    }

    fun doOnEvent(x: Float, y: Float, t: Long, r: () -> Unit) {
        doOnEvent(Event(x, y, t), r)
    }

    class Event(val x: Float, val y: Float, val time: Long)
}
