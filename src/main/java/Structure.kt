import java.awt.Point

class State(val state: Any) {
    override fun equals(other: Any?): Boolean {
        return other is State && state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

    override fun toString(): String {
        return "State(statePresent='$state')"
    }
}

class Action(val destState: State, private val cost: Int) {
    override fun toString(): String {
        return "Action(destState=$destState, cost=$cost)"
    }
}

abstract class Problem(open val initial: State, open val goal: List<State>? = null) {

    //get all list actions out of state
    abstract fun actions(state: State): List<Action>

    //transition Model
    abstract fun result(state: State, action: Action): State

    //is this goal
    open fun goalTest(state: State): Boolean = goal?.contains(state) ?: false

    //total cost to 2
    abstract fun pathCost(costSoFar: Int, state1: State, action: Action?, state2: State): Int
}


open class Graph(
        val graphDict: Map<State, Map<State, Int>>,
        var location: Map<State, Point>? = null,
        directed: Boolean = true
) {
    private val graphDictConstructed: MutableMap<State, MutableMap<State, Int>> = mutableMapOf()

    init {
        if (!directed) makeUndirected()
    }

    //construct new undirected dict from directed dict
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

    //get Actions
    fun get(fromState: State): List<Action> {
        return graphDictConstructed.getOrDefault(fromState, mutableMapOf()).map { entry -> Action(entry.key, entry.value) }
    }

    //get path cost from A -> B
    fun getCost(fromState: State, toState: State): Int {
        return graphDictConstructed[fromState]?.get(toState) ?: Int.MAX_VALUE
    }

    //get result from current state with an action
    fun getDestState(action: Action): State {
        return action.destState
    }

    //return nodes
    fun nodes(): Set<State> {
        val s1 = graphDict.keys
        val s2 = graphDict.values.map { it.keys }.flatten()
        return s1.union(s2)
    }
}


class UndirectedGraph(
        graphDict: Map<State, Map<State, Int>>
) : Graph(graphDict, directed = false)


open class Node(
        val state: State,
        var parent: Node? = null,
        private val action: Action? = null,
        private val pathCost: Int = 0
) : Comparable<Node> {
    private var depth: Int = 0
    var fValue: Int = 0

    init {
        if (parent != null) depth += 1
    }

    /**
     * return list of nodes reachable from current Node
     */
    fun expand(problem: Problem): List<Node> {
        return problem.actions(state).map { action ->
            childNode(problem, action)
        }
    }

    /**
     * construct childNode from this Node
     */
    private fun childNode(problem: Problem, action: Action): Node {
        val nextState = problem.result(state, action)
        return Node(nextState, this, action, problem.pathCost(pathCost, state, action, nextState))
    }

    /**
     * list of nodes from root to this node
     */
    fun path(): List<Node> {
        var node: Node? = this
        val pathBack = mutableListOf<Node>()
        while (node != null) {
            pathBack.add(node)
            node = node.parent
        }
        return pathBack.asReversed()
    }

    /*
     *  list of actions from root to this node
     */
    fun solution(): List<Action> {
        val listNodeFromRoot = path()
        return listNodeFromRoot.subList(1, listNodeFromRoot.size).map {
            it.action!!
        }
    }

    fun g(): Int = pathCost

    override fun equals(other: Any?): Boolean {
        return other is Node && state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

    override fun toString(): String {
        return "Node(state=${state.state})"
    }

    override fun compareTo(other: Node): Int {
        return pathCost.compareTo(other.pathCost)
    }
}


class NodeCutOff(state: State = State("")) : Node(state)