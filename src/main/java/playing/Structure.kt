package playing

class State(val stateName: String) {
    override fun equals(other: Any?): Boolean {
        return stateName == (other as State).stateName
    }

    override fun hashCode(): Int {
        return stateName.hashCode()
    }

    override fun toString(): String {
        return "State(stateName='$stateName')"
    }
}

class Action(val destState: State, val cost: Int)

abstract class Problem(private val initial: State, private val goal: List<State>) {

    //get all list neighbor
    abstract fun actions(state: State): Set<State>

    abstract fun result(state: State, action: Action): State

    fun goalTest(state: State): Boolean = goal.contains(state)

    abstract fun pathCost(costSoFar: Int, state1: State, action: Action, state2: State): Int

    open fun value(state: State) {}
}

open class Graph(
        private val graphDict: Map<State, Map<State, Int>>,
        private val directed: Boolean = true
) {
    private val graphDictConstructed: MutableMap<State, MutableMap<State, Int>> = mutableMapOf()

    init {
        if (!directed) makeUndirected()
    }

    private fun makeUndirected() {
        for (a in graphDict.keys) {
            for ((b, dist) in graphDict[a]?.entries!!) {
                connect1(a, b, dist)
                connect1(b, a, dist)
            }
        }
    }

    private fun connect1(fromState: State, toState: State, distance: Int) {
        val map = graphDictConstructed.getOrDefault(fromState, mutableMapOf())
        map[toState] = distance
        graphDictConstructed[fromState] = map
    }

    fun get(fromState: State) : MutableMap<State, Int>{
        return graphDictConstructed.getOrDefault(fromState, mutableMapOf())
    }

    fun getCost(fromState: State, toState: State) : Int {
        return graphDictConstructed[fromState]?.get(toState) ?: Int.MAX_VALUE
    }
}

class UndirectedGraph(
        private val graphDict: Map<State, Map<State, Int>>
) : Graph(graphDict, false)


class GraphProblem(
        private val initial: State,
        private val goal: List<State>,
        private val graph: Graph
) : Problem(initial, goal) {

    override fun actions(state: State): Set<State> {
        return graph.get(state).keys
    }

    override fun result(state: State, action: Action): State {
        return action.destState
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action, state2: State): Int {
        return costSoFar + graph.getCost(state1, state2)
    }

}