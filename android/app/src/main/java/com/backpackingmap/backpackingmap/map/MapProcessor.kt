package com.backpackingmap.backpackingmap.map

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

@ExperimentalCoroutinesApi
class MapProcessor(
    override val coroutineContext: CoroutineContext,
    initialState: MapState,
    private val layers: Collection<MapLayer>,
) : CoroutineScope {

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    sealed class Event {
        data class Gesture(val event: OmniGestureDetector.Event) : Event()

        data class MoveBy(val deltaX: Float, val deltaY: Float) : Event()

        data class SizeChanged(val size: MapSize) : Event()
    }

    suspend fun send(event: Event) {
        events.emit(event)
    }

    private fun computeNewState(oldState: MapState, event: Event) = when (event) {
        is Event.Gesture ->
            computeNewStateFromGesture(oldState, event.event)

        is Event.MoveBy ->
            oldState.withCenter(oldState.center.movedBy(oldState.zoom, event.deltaX, event.deltaY))

        is Event.SizeChanged ->
            oldState.withSize(event.size)
    }

    private val events =
        MutableSharedFlow<Event>(EVENT_BUFFER_SIZE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        launch {
            events.collect { event ->
                _state.value = computeNewState(_state.value, event)
            }
        }
    }

    private var flinger: Job? = null

    private fun computeNewStateFromGesture(
        oldState: MapState,
        event: OmniGestureDetector.Event,
    ): MapState {
        flinger?.cancel("Cancelling fling because of new gesture")

        return when (event) {
            is OmniGestureDetector.Event.Scroll ->
                oldState.withCenter(
                    oldState.center.movedBy(oldState.zoom, event.distanceX, event.distanceY))

            is OmniGestureDetector.Event.Fling -> {
                if (event.velocityX != null && event.velocityY != null) {
                    flinger = launch {
                        var deltaX = -event.velocityX / 15f
                        var deltaY = -event.velocityY / 15f

                        while (abs(deltaX) > 1 || abs(deltaY) > 1) {
                            send(Event.MoveBy(deltaX, deltaY))

                            deltaX *= 0.8f
                            deltaY *= 0.8f

                            delay(1_000 / 60) // 60 fps
                        }
                    }
                }
                oldState
            }

            is OmniGestureDetector.Event.Scale -> {
                if (event.scaleFactor != null) {
                    oldState.withZoom(oldState.zoom.scaledBy(1 / event.scaleFactor))
                } else {
                    oldState
                }
            }

            else -> oldState
        }
    }

    companion object {
        private const val EVENT_BUFFER_SIZE = 1_000
    }
}