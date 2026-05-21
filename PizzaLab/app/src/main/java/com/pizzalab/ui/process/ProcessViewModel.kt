package com.pizzalab.ui.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzalab.data.model.DoughPhase
import com.pizzalab.data.model.DoughProcess
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
import java.time.LocalDateTime

/**
 * ViewModel that manages a dough preparation process with sequential timed phases.
 */
class ProcessViewModel : ViewModel() {

    private val _process = MutableStateFlow<DoughProcess?>(null)
    val process: StateFlow<DoughProcess?> = _process.asStateFlow()

    private val _templates = MutableStateFlow(DoughProcess.defaultTemplates)
    val templates: StateFlow<List<DoughProcess>> = _templates.asStateFlow()

    /** Elapsed seconds for the currently active phase. */
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    /** Notification events emitted when a phase completes. */
    private val _phaseCompletedEvent = MutableSharedFlow<DoughPhase>(extraBufferCapacity = 1)
    val phaseCompletedEvent: SharedFlow<DoughPhase> = _phaseCompletedEvent.asSharedFlow()

    private var timerJob: Job? = null

    // ── Process lifecycle ──────────────────────────────────────────────

    /**
     * Start or resume the current process. Activates the first incomplete phase
     * and begins counting.
     */
    fun startProcess() {
        val current = _process.value ?: return
        val phases = current.phases.toMutableList()
        val activeIndex = phases.indexOfFirst { !it.isCompleted }
        if (activeIndex == -1) return

        phases[activeIndex] = phases[activeIndex].copy(
            isActive = true,
            startedAt = phases[activeIndex].startedAt ?: LocalDateTime.now()
        )

        _process.value = current.copy(phases = phases, isRunning = true)
        startTimer()
    }

    /** Pause the running process (stops the timer but keeps state). */
    fun pauseProcess() {
        timerJob?.cancel()
        timerJob = null
        _process.update { it?.copy(isRunning = false) }
    }

    /** Mark the currently active phase as completed and advance to the next one. */
    fun completePhase() {
        val current = _process.value ?: return
        val phases = current.phases.toMutableList()
        val activeIndex = phases.indexOfFirst { it.isActive }
        if (activeIndex == -1) return

        val completed = phases[activeIndex].copy(
            isActive = false,
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )
        phases[activeIndex] = completed

        // Emit notification
        _phaseCompletedEvent.tryEmit(completed)

        // Advance to next phase if available
        val nextIndex = activeIndex + 1
        if (nextIndex < phases.size) {
            phases[nextIndex] = phases[nextIndex].copy(
                isActive = true,
                startedAt = LocalDateTime.now()
            )
            _elapsedSeconds.value = 0L
            _process.value = current.copy(phases = phases, isRunning = true)
            startTimer()
        } else {
            // All phases done
            timerJob?.cancel()
            timerJob = null
            _elapsedSeconds.value = 0L
            _process.value = current.copy(phases = phases, isRunning = false)
        }
    }

    /** Cancel the entire process. */
    fun cancelProcess() {
        timerJob?.cancel()
        timerJob = null
        _elapsedSeconds.value = 0L
        _process.value = null
    }

    // ── Phase editing ──────────────────────────────────────────────────

    fun addPhase(phase: DoughPhase) {
        _process.update { proc ->
            proc?.copy(phases = proc.phases + phase)
        }
    }

    fun removePhase(id: String) {
        _process.update { proc ->
            proc?.copy(phases = proc.phases.filter { it.id != id })
        }
    }

    fun editPhase(id: String, updated: DoughPhase) {
        _process.update { proc ->
            proc?.copy(
                phases = proc.phases.map { if (it.id == id) updated else it }
            )
        }
    }

    fun movePhase(fromIndex: Int, toIndex: Int) {
        _process.update { proc ->
            if (proc == null) return@update null
            val mutable = proc.phases.toMutableList()
            if (fromIndex !in mutable.indices || toIndex !in mutable.indices) return@update proc
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            proc.copy(phases = mutable)
        }
    }

    // ── Templates ──────────────────────────────────────────────────────

    /** Start a new process from a template (deep-copies phases with new IDs). */
    fun startFromTemplate(template: DoughProcess) {
        timerJob?.cancel()
        timerJob = null
        _elapsedSeconds.value = 0L

        val freshPhases = template.phases.map { phase ->
            phase.copy(
                id = java.util.UUID.randomUUID().toString(),
                isActive = false,
                isCompleted = false,
                startedAt = null,
                completedAt = null
            )
        }
        _process.value = DoughProcess(
            name = template.name,
            phases = freshPhases,
            isRunning = false
        )
    }

    /** Save the current process as a reusable template. */
    fun saveAsTemplate(name: String) {
        val current = _process.value ?: return
        val template = current.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            isRunning = false,
            phases = current.phases.map {
                it.copy(
                    isActive = false,
                    isCompleted = false,
                    startedAt = null,
                    completedAt = null
                )
            }
        )
        _templates.update { it + template }
    }

    /** Create a brand-new empty process. */
    fun createNewProcess(name: String) {
        timerJob?.cancel()
        timerJob = null
        _elapsedSeconds.value = 0L
        _process.value = DoughProcess(name = name, phases = emptyList(), isRunning = false)
    }

    // ── Timer internals ────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _elapsedSeconds.update { it + 1 }

                // Check if the active phase duration has been reached
                val proc = _process.value ?: break
                val activePhase = proc.phases.firstOrNull { it.isActive } ?: break
                val targetSeconds = activePhase.durationMinutes * 60L
                if (_elapsedSeconds.value >= targetSeconds) {
                    completePhase()
                    break // completePhase will restart timer for next phase if needed
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
