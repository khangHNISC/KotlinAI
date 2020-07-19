package playing.problem

import Action
import Graph
import Node
import Problem
import State
import java.util.*

class GraphProblem(
        override val initial: State,
        override val goal: List<State>,
        val graph: Graph //delegation
) : Problem(initial, goal) {

    override fun actions(state: State): List<Action> {
        return graph.get(state)
    }

    override fun result(state: State, action: Action): State {
        return graph.getDestState(action)
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action?, state2: State): Int {
        return costSoFar + graph.getCost(state1, state2)
    }

    fun findMinEdge(): Int {
        var m = Integer.MAX_VALUE
        for (action in graph.graphDict.values) {
            val localMin = Collections.min(action.values)
            m = Math.min(localMin, m)
        }
        return m
    }

    /**
     * !   !
     * !---!
     * !   !SLD = straight-line distance
     */
    fun h(node: Node): Int {
        val locs = graph.location
        if (locs != null)
            return locs[node.state]?.distance(locs[goal.first()])?.toInt() ?: Int.MAX_VALUE
        else return Int.MAX_VALUE
    }

    fun f(node: Node): Int {
        return h(node) + node.g()
    }
}