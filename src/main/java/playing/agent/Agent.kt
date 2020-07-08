package playing.agent

import Node
import NodeCutOff
import Problem
import State
import java.util.*
import kotlin.collections.ArrayDeque

object Agent {

    /**
     * BFS - tree search
     */
    @ExperimentalStdlibApi
    fun bfsTree(problem: Problem): Node? {
        val frontier = ArrayDeque(listOf(Node(problem.initial)))
        while (frontier.isNotEmpty()) {
            val node = frontier.removeFirst()
            for (child in node.expand(problem)) {
                if (problem.goalTest(node.state)) return node
                frontier.addLast(child)
            }
        }
        return null
    }


    /**
     * DFS tree search
     */
    @ExperimentalStdlibApi
    fun dfsTree(problem: Problem): Node? {
        val frontier = ArrayDeque(listOf(Node(problem.initial)))
        while (frontier.isNotEmpty()) {
            val node = frontier.removeFirst()
            for (child in node.expand(problem)) {
                if (problem.goalTest(node.state)) return node
                frontier.addFirst(node)
            }
        }
        return null
    }

    /**
     * BFS - graph search
     */
    @ExperimentalStdlibApi
    fun bfs(problem: Problem): Node? {
        var node = Node(problem.initial)
        if (problem.goalTest(node.state)) return node
        val frontier = ArrayDeque(listOf(node))
        val explored = mutableSetOf<State>()
        while (frontier.isNotEmpty()) {
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
        val frontier = PriorityQueue(listOf(Node(problem.initial, pathCost = 0)))
        val explored = mutableSetOf<State>()
        while (frontier.isNotEmpty()) {
            val node = frontier.poll()
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
        val frontier = ArrayDeque(listOf(Node(problem.initial)))
        val explored = mutableSetOf<State>()
        while (frontier.isNotEmpty()) {
            val node = frontier.removeFirst()
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
                        println(child)
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
//inorder post order pre order
//https://eli.thegreenplace.net/2015/directed-graph-traversal-orderings-and-applications-to-data-flow-analysis/#:~:text=When%20traversing%20trees%20with%20DFS,after%20recursing%20into%20its%20children.