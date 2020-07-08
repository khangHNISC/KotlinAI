package playing.agent

import GraphProblem
import Node
import NodeCutOff
import Problem
import State
import java.util.*
import java.util.Collections.min
import java.util.stream.IntStream.range
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
            explored.add(node.state) //pre order
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
                        else if (result != null) return result //post order
                    }
                    return if (cutoffOccurred) NodeCutOff() else null
                }
            }
        }
        return recursiveDLS(Node(problem.initial), problem, limit)
    }


    fun iterativeDeepeningSearch(problem: Problem): Node? {
        for (depth in range(0, Int.MAX_VALUE)) {
            val node = depthLimitedSearch(problem, depth)
            if (node != NodeCutOff()) {
                println("depth :$depth")
                return node
            }
        }
        return null
    }


    fun bidirectionalSearch(problem: Problem): Node? {
        var e = 0
        if (problem is GraphProblem) e = problem.findMinEdge()
        val (gf, gb) = Pair({ Node(problem.initial) to 0 }, { Node(problem.goal.first()) to 0 })
        val (openF, openB) = Pair(listOf(Node(problem.initial)), listOf(Node(problem.goal.first())))
        val (closeF, closeB) = Pair(listOf<Node>(), listOf<Node>())
        var U = Integer.MAX_VALUE

        fun extend(
                U: Int,
                openDir: List<Node>,
                openOther: List<Node>,
                gDir: Pair<Node, Int>,
                gOther: Pair<Node, Int>,
                closedDir: List<Node>) {
            //Extend search in given direction
            
        }

        fun findMin(openDir: List<Node>, g: Map<Node, Int>): Triple<Int, Int, Int> {
            //Finds minimum priority, g and f values in open_dir
            var (prMin, prMinF) = Pair(Int.MAX_VALUE, Int.MAX_VALUE)
            for (n in openDir) {
                val f = (g[n] ?: 0) + (problem as GraphProblem).h(n)
                val pr = Math.max(f, 2 * (g[n] ?: 0))
                prMin = Math.min(prMin, pr)
                prMinF = Math.min(prMinF, f)
            }
            return Triple(prMin, prMinF, min(g.values))
        }

        fun findKey(prMin: Int, openDir: List<Node>, g: Map<Node, Int>): Node {
            //Finds key in open_dir with value equal to pr_min and minimum g value.
            var m = Integer.MAX_VALUE
            var node: Node = NodeCutOff()
            for (n in openDir) {
                val gVal = (g[n] ?: 0)
                val f = gVal + (problem as GraphProblem).h(n)
                val pr = Math.max(f, 2 * gVal)
                if (pr == prMin) {
                    if (gVal < m) {
                        m = gVal
                        node = n
                    }
                }
            }
            return node
        }
        return null
    }
}