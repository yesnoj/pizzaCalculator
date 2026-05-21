package com.pizzalab.ui.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzalab.data.model.CookingPreset
import com.pizzalab.data.model.CookingTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing multiple concurrent cooking timers.
 */
class CookingViewModel : ViewModel() {

    private val _timers = MutableStateFlow<List<CookingTimer>>(emptyList())
    val timers: StateFlow<List<CookingTimer>> = _timers.asStateFlow()

    private val _pizzeCount = MutableStateFlow(0)
    val pizzeCount: StateFlow<Int> = _pizzeCount.asStateFlow()

    private val _selectedPreset = MutableStateFlow(CookingPreset.defaultPresets.first())
    val selectedPreset: StateFlow<CookingPreset> = _selectedPreset.asStateFlow()

    /** Emitted when a timer reaches zero. */
    private val _timerCompletedEvent = MutableSharedFlow<CookingTimer>(extraBufferCapacity = 1)
    val timerCompletedEvent: SharedFlow<CookingTimer> = _timerCompletedEvent.asSharedFlow()

    /** Track per-timer coroutine jobs. */
    private val timerJobs = mutableMapOf<String, Job>()

    fun resetPizzeCount() {
        _pizzeCount.value = 0
    }

    fun selectPreset(preset: CookingPreset) {
        _selectedPreset.value = preset
    }

    /**
     * Add a new timer for the given preset and start it immediately.
     */
    fun addTimer(preset: CookingPreset) {
        val nextNumber = _timers.value.size + _pizzeCount.value + 1
        val timer = CookingTimer(
            preset = preset,
            remainingSeconds = preset.durationSeconds,
            isRunning = true,
            pizzaNumber = nextNumber
        )
        _timers.update { it + timer }
        startCountdown(timer.id)
    }

    /**
     * Add a timer with a custom duration (seconds).
     */
    fun addCustomTimer(name: String, durationSeconds: Int) {
        val preset = CookingPreset(name = name, durationSeconds = durationSeconds, description = "Timer personalizzato")
        addTimer(preset)
    }

    fun startTimer(id: String) {
        _timers.update { list ->
            list.map { if (it.id == id) it.copy(isRunning = true) else it }
        }
        startCountdown(id)
    }

    fun pauseTimer(id: String) {
        timerJobs[id]?.cancel()
        timerJobs.remove(id)
        _timers.update { list ->
            list.map { if (it.id == id) it.copy(isRunning = false) else it }
        }
    }

    fun resetTimer(id: String) {
        timerJobs[id]?.cancel()
        timerJobs.remove(id)
        _timers.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    remainingSeconds = it.preset.durationSeconds,
                    isRunning = false
                ) else it
            }
        }
    }

    fun removeTimer(id: String) {
        timerJobs[id]?.cancel()
        timerJobs.remove(id)
        _timers.update { list -> list.filter { it.id != id } }
    }

    // ── Countdown logic ────────────────────────────────────────────────

    private fun startCountdown(id: String) {
        timerJobs[id]?.cancel()
        timerJobs[id] = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                var completed = false
                _timers.update { list ->
                    list.map { timer ->
                        if (timer.id == id && timer.isRunning) {
                            val newRemaining = timer.remainingSeconds - 1
                            if (newRemaining <= 0) {
                                completed = true
                                timer.copy(remainingSeconds = 0, isRunning = false)
                            } else {
                                timer.copy(remainingSeconds = newRemaining)
                            }
                        } else {
                            timer
                        }
                    }
                }
                if (completed) {
                    val completedTimer = _timers.value.find { it.id == id }
                    if (completedTimer != null) {
                        _timerCompletedEvent.tryEmit(completedTimer)
                        _pizzeCount.update { it + 1 }
                    }
                    timerJobs.remove(id)
                    break
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
    }
}
