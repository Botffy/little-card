package hu.sarmin.yt2ig

class FakeNavigation {
    val navStack = mutableListOf<AppState>()
    val navStackHistory = mutableListOf<List<AppState>>()

    val navigation: Navigation = Navigation(
        navigateTo = { newState ->
            navStackHistory.add(navStack.toList())
            navStack.add(newState)
        },
        replaceState = { oldState, newState ->
            navStackHistory.add(navStack.toList())
            val index = navStack.indexOf(oldState)
            if (index != -1) {
                navStack[index] = newState
            }
        }
    )

    fun reset() {
        navStack.clear()
        navStackHistory.clear()
    }
}
