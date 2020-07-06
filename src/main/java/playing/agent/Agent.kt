package playing.agent

import Node
import NodeCutOff
import Problem
import State
import java.util.*
import kotlin.collections.ArrayDeque

object Agent {

    /**
     * BFS - graph search
     */
    @ExperimentalStdlibApi
    fun bfs(problem: Problem): Node? {
        var node = Node(problem.initial)
        if (problem.goalTest(node.state)) return node
        val frontier = ArrayDeque(listOf(node))
        val explored = mutableSetOf<State>()
        while (frontier.size != 0) {
            node = frontier.removeFirst()
            explored.add(node.state)
            for (child in node.expand(problem)) {
                if (child.state !in explored && child !in frontier) {
                    if (problem.goalTest(child.state)) return child
                    frontier.addLast(child)
                }
            }
        }
        return null
    }

    /*
     * Uniform-cost search
     */
    @ExperimentalStdlibApi
    fun uniformCostSearch(problem: Problem): Node? {
        var node = Node(problem.initial, pathCost = 0)
        val frontier = PriorityQueue(listOf(node))
        val explored = mutableSetOf<State>()
        while (frontier.size != 0) {
            node = frontier.poll()
            //return at selected for expansion since node found can be in suboptimal path
            if (problem.goalTest(node.state)) return node
            explored.add(node.state)
            for (child in node.expand(problem)) {
                if (child.state !in explored && child !in frontier) {
                    frontier.add(child)
                } else if (child in frontier) {
                    if (child < frontier.elementAt(frontier.indexOf(child))) {
                        frontier.remove(child)
                        frontier.add(child)
                    }
                }
            }
        }
        return null
    }

    /**
     * dfs - graph search
     */
    @ExperimentalStdlibApi
    fun dfs(problem: Problem): Node? {
        var node = Node(problem.initial)
        val frontier = ArrayDeque(listOf(node))
        val explored = mutableSetOf<State>()
        while (frontier.size != 0) {
            node = frontier.removeFirst()
            if (problem.goalTest(node.state)) return node
            explored.add(node.state)
            for (child in node.expand(problem)) {
                if (child.state !in explored && child !in frontier) {
                    frontier.addFirst(child)
                }
            }
        }
        return null
    }


    /**
     * dfs limited level
     */
    @ExperimentalStdlibApi
    fun depthLimitedSearch(problem: Problem, limit: Int = 5): Node? {
        fun recursiveDLS(node: Node, problem: Problem, limit: Int): Node? {
            when {
                problem.goalTest(node.state) -> return node
                limit == 0 -> return NodeCutOff()
                else -> {
                    var cutoffOccurred = false
                    for (child in node.expand(problem)) {
                        val result = recursiveDLS(child, problem, limit - 1)
                        if (result is NodeCutOff) cutoffOccurred = true
                        else if (result != null) return result
                    }
                    return if (cutoffOccurred) NodeCutOff() else null
                }
            }
        }
        return recursiveDLS(Node(problem.initial), problem, limit)
    }
}