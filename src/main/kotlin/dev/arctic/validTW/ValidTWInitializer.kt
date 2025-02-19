package dev.arctic.validtw

import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Quest
import com.typewritermc.core.entries.Objective
import com.typewritermc.core.initialization.Initializable
import com.typewritermc.core.initialization.Initializer
import com.typewritermc.core.initialization.Singleton
import dev.arctic.validtw.validation.DependencyValidator
import com.typewritermc.core.events.EntryCreateEvent
import com.typewritermc.core.events.EntryModifyEvent
import com.typewritermc.core.TypeWriter

@Initializer
@Singleton
object ValidTWInitializer : Initializable {
    private val validator = DependencyValidator()

    override suspend fun initialize() {
        // Register validation handlers for entry creation/modification
        TypeWriter.eventBus.subscribe<EntryCreateEvent> { event ->
            val result = validateEntry(event.entry)
            if (!result.valid) {
                event.cancel(result.error ?: "Invalid dependency structure")
            }
        }

        TypeWriter.eventBus.subscribe<EntryModifyEvent> { event ->
            val result = validateEntry(event.entry)
            if (!result.valid) {
                event.cancel(result.error ?: "Invalid dependency structure")
            }
        }
    }

    fun validateEntry(entry: Entry): ValidationResult {
        if (entry !is Quest && entry !is Objective) {
            return ValidationResult(true)
        }

        fun getDependencies(e: Entry): List<Entry> = when (e) {
            is Quest -> listOf(e.quest).filterNotNull()
            is Objective -> listOfNotNull(e.quest) + e.children.mapNotNull { it.ref().entry }
            else -> e.children.mapNotNull { it.ref().entry }
        }

        val cycle = validator.detectCycle(
            entry,
            { getDependencies(it) },
            { it.id }
        )

        return if (cycle != null) {
            val cycleStr = cycle.joinToString(" -> ")
            ValidationResult(
                false,
                "Circular dependency detected:\n$cycleStr\n\nThis creates an infinite loop and must be fixed."
            )
        } else {
            ValidationResult(true)
        }
    }

    override suspend fun shutdown() {
        // No shutdown logic needed
    }
}

data class ValidationResult(
    val valid: Boolean,
    val error: String? = null
) 