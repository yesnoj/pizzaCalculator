package com.pizzalab.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * A single phase within a dough preparation process.
 */
data class DoughPhase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null
)

/**
 * A complete dough preparation process composed of sequential phases.
 */
data class DoughProcess(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phases: List<DoughPhase>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isRunning: Boolean = false
) {
    companion object {
        /** Default process templates for common pizza styles. */
        val defaultTemplates: List<DoughProcess>
            get() = listOf(
                DoughProcess(
                    name = "Napoletana Classica",
                    phases = listOf(
                        DoughPhase(
                            name = "Autolisi",
                            description = "Riposo farina e acqua per sviluppare il glutine",
                            durationMinutes = 30
                        ),
                        DoughPhase(
                            name = "Impasto",
                            description = "Lavorazione dell'impasto con sale e lievito",
                            durationMinutes = 20
                        ),
                        DoughPhase(
                            name = "Puntata",
                            description = "Prima lievitazione in massa",
                            durationMinutes = 120
                        ),
                        DoughPhase(
                            name = "Frigo",
                            description = "Maturazione in frigorifero a 4°C",
                            durationMinutes = 1440
                        ),
                        DoughPhase(
                            name = "Appretto",
                            description = "Lievitazione finale dei panetti a temperatura ambiente",
                            durationMinutes = 120
                        )
                    )
                ),
                DoughProcess(
                    name = "Veloce 8h",
                    phases = listOf(
                        DoughPhase(
                            name = "Impasto",
                            description = "Lavorazione dell'impasto",
                            durationMinutes = 20
                        ),
                        DoughPhase(
                            name = "Puntata",
                            description = "Prima lievitazione breve",
                            durationMinutes = 30
                        ),
                        DoughPhase(
                            name = "Lievitazione",
                            description = "Lievitazione principale a temperatura ambiente",
                            durationMinutes = 420
                        ),
                        DoughPhase(
                            name = "Appretto",
                            description = "Lievitazione finale dei panetti",
                            durationMinutes = 30
                        )
                    )
                )
            )
    }
}

/**
 * A cooking preset for a specific pizza style.
 */
data class CookingPreset(
    val name: String,
    val durationSeconds: Int,
    val description: String
) {
    companion object {
        val defaultPresets: List<CookingPreset>
            get() = listOf(
                CookingPreset(
                    name = "Napoletana",
                    durationSeconds = 90,
                    description = "Forno a legna ~450°C"
                ),
                CookingPreset(
                    name = "Teglia",
                    durationSeconds = 240,
                    description = "Forno elettrico ~300°C"
                ),
                CookingPreset(
                    name = "Pala",
                    durationSeconds = 180,
                    description = "Forno elettrico ~350°C"
                ),
                CookingPreset(
                    name = "Padellino",
                    durationSeconds = 300,
                    description = "Forno elettrico ~250°C"
                )
            )
    }
}

/**
 * An active cooking timer instance.
 */
data class CookingTimer(
    val id: String = UUID.randomUUID().toString(),
    val preset: CookingPreset,
    val remainingSeconds: Int,
    val isRunning: Boolean = false,
    val pizzaNumber: Int = 1
)
