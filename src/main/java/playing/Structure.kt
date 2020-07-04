package playing

class State

class Action(val DestState: State)

abstract class Problem(private val initial: State, private val goal: List<State>) {

    //get all list neighbor
    abstract fun actions(state: State): Set<State>

    abstract fun result(state: State, action: Action): State

    fun goalTest(state: State): Boolean = goal.contains(state)

    abstract fun pathCost(costSoFar: Int, state1: State, action: Action, state2: State): Int

    open fun value(state: State) {}
}

class GraphProblem(
        private val initial: State,
        private val goal: List<State>,
        private val graph: Map<State, Map<State, Int>>
) : Problem(initial, goal) {

    override fun actions(state: State): Set<State> {
        return graph[state]?.keys ?: emptySet()
    }

    override fun result(state: State, action: Action): State {
        return action.DestState
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action, state2: State): Int {
        return costSoFar + graph[state1][state2] ?: Int.MAX_VALUE
    }

}