package dev.arctic.validtw

import com.typewritermc.core.TypewriterCore
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Library
import com.typewritermc.core.entries.Page
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.PriorityEntry
import com.typewritermc.core.extension.Initializable
import com.typewritermc.loader.Extension
import dev.arctic.validtw.validation.DependencyValidator

@Extension("validtw")
class ValidTWInitializer : Initializable {
    private val validator = DependencyValidator()

    override suspend fun initialize() {
        TypewriterCore.eventManager.register(this)
    }

    fun onEntryCreate(event: com.typewritermc.core.entries.Entry) {
        val result = validateEntry(event)
        if (!result.valid) {
            throw IllegalArgumentException(result.error ?: "Invalid dependency structure")
        }
    }

    fun onEntryModify(event: com.typewritermc.core.entries.Entry) {
        val result = validateEntry(event)
        if (!result.valid) {
            throw IllegalArgumentException(result.error ?: "Invalid dependency structure")
        }
    }

    private fun validateEntry(entry: Entry): ValidationResult {
        if (entry !is Page && entry !is PriorityEntry) {
            return ValidationResult(true)
        }

        fun getDependencies(e: Entry): List<Entry> = when (e) {
            is Page -> listOfNotNull(e.parent)
            is PriorityEntry -> listOfNotNull(e.parent) + e.references.mapNotNull { it.get() }
            else -> e.references.mapNotNull { it.get() }
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
