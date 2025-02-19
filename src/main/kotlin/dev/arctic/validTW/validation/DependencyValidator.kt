package dev.arctic.validtw.validation

class DependencyValidator {
    fun <T> detectCycle(
        item: T,
        getDependencies: (T) -> List<T>,
        getIdentifier: (T) -> String
    ): List<String>? {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        val cycleList = mutableListOf<String>()

        fun dfs(current: T): Boolean {
            val currentId = getIdentifier(current)
            if (currentId in recursionStack) {
                cycleList.add(currentId)
                return true
            }
            if (currentId in visited) return false

            visited.add(currentId)
            recursionStack.add(currentId)

            for (dependency in getDependencies(current)) {
                if (dfs(dependency)) {
                    if (cycleList.first() != cycleList.last() || cycleList.size == 1) {
                        cycleList.add(currentId)
                    }
                    return true
                }
            }

            recursionStack.remove(currentId)
            return false
        }

        return if (dfs(item)) cycleList.reversed() else null
    }
} 