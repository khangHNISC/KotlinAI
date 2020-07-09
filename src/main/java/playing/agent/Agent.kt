package playing.agent

import GraphProblem
import Node
import NodeCutOff
import Problem
import Quadruple
import State
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.max
import kotlin.math.min

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
    private fun depthLimitedSearch(problem: Problem, limit: Int = 5): Node? {
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
        for (depth in 0..Int.MAX_VALUE) {
            val node = depthLimitedSearch(problem, depth)
            if (node != NodeCutOff()) {
                return node
            }
        }
        return null
    }


    fun bidirectionalSearch(problem: Problem): Int {
        //open list forward backward
        //close list forward backward
        //g value f value
        //h = heuristic function
        //pr = priority of node = max (f(n), 2 g(n))

        var e = 0
        if (problem is GraphProblem) e = problem.findMinEdge()
        var (gF, gB) = Pair(mutableMapOf(Node(problem.initial) to 0), mutableMapOf(Node(problem.goal.first()) to 0))
        var (openF, openB) = Pair(mutableListOf(Node(problem.initial)), mutableListOf(Node(problem.goal.first())))
        var (closeF, closeB) = Pair(mutableListOf<Node>(), mutableListOf<Node>())
        var U = Integer.MAX_VALUE

        //Extend search in given direction
        fun extend(
                C: Int,
                U: Int,
                openDir: MutableList<Node>,
                openOther: MutableList<Node>,
                gDir: MutableMap<Node, Int>,
                gOther: MutableMap<Node, Int>,
                closedDir: MutableList<Node>): Quadruple {

            //Finds key in open_dir with value equal to pr_min and minimum g value.
            fun findKey(prMin: Int, openDir: MutableCollection<Node>, g: Map<Node, Int>): Node {
                var m = Integer.MAX_VALUE
                var node: Node = NodeCutOff()
                for (n in openDir) {
                    val gVal = (g[n] ?: 0)
                    val f = gVal + (problem as GraphProblem).h(n)
                    val pr = max(f, 2 * gVal)
                    if (pr == prMin) {
                        if (gVal < m) {
                            m = gVal
                            node = n
                        }
                    }
                }
                return node
            }

            var Uvar = U
            val n = findKey(C, openDir, gDir)
            openDir.remove(n)
            closedDir.add(n)
            for (c in n.expand(problem)) {
                if (c in openDir || c in closedDir) {
                    if (gDir[c]!! <= problem.pathCost(gDir[n]!!, n.state, null, c.state)) continue
                    openDir.remove(c)
                }
                gDir[c] = problem.pathCost(gDir[n]!!, n.state, null, c.state)
                openDir.add(c)
                if (c in openOther) Uvar = min(Uvar, gDir[c]!! + gOther[c]!!)
            }
            return Quadruple(Uvar, openDir, closedDir, gDir)
        }

        //Finds minimum priority, g and f values in open_dir
        fun findMin(openDir: List<Node>, g: Map<Node, Int>): Triple<Int, Int, Int> {
            var (prMin, prMinF) = Pair(Int.MAX_VALUE, Int.MAX_VALUE)
            for (n in openDir) {
                val f = (g[n] ?: 0) + (problem as GraphProblem).h(n)
                val pr = max(f, 2 * (g[n] ?: 0))
                prMin = min(prMin, pr)
                prMinF = min(prMinF, f)
            }
            return Triple(prMin, prMinF, Collections.min(g.values))
        }


        while (openF.isNotEmpty() && openB.isNotEmpty()) {
            val (prMinF, fMinF, gMinF) = findMin(openF, gF)
            val (prMinB, fMinB, gMinB) = findMin(openB, gB)
            val C = min(prMinF, prMinB)
            if (U <= max(max(C, fMinF), max(fMinB, gMinF + gMinB + e))) return U
            if (C == prMinF) {
                //extend forward
                val (w, x, y, z) = extend(C, U, openF, openB, gF, gB, closeF)
                U = w
                openF = x
                closeF = y
                gF = z
            } else {
                //extend backward
                val (w, x, y, z) = extend(C, U, openB, openF, gB, gF, closeB)
                U = w
                openB = x
                closeB = y
                gB = z
            }
        }
        return Int.MAX_VALUE
    }


    @ExperimentalStdlibApi
    fun simpleBidirectionalSearch(problem: Problem): Pair<Node, Node?>? {
        val nodeI = Node(problem.initial)
        val nodeG = Node(problem.goal.first())
        val openF = ArrayDeque(listOf(nodeI))
        val openB = ArrayDeque(listOf(nodeG))
        val closed = mutableSetOf<State>()

        while (openF.isNotEmpty() && openB.isNotEmpty()) {
            if (openF.isNotEmpty()) {
                val nodeF = openF.removeFirst()
                closed.add(nodeF.state)
                if (nodeF == nodeG) return Pair(nodeF, null)
                if (nodeF in openB) return Pair(nodeF, openB[openB.indexOf(nodeF)])
                for (child in nodeF.expand(problem)) {
                    if (child.state !in closed && child !in openF) {
                        openF.addLast(child)
                    }
                }
            }
            if (openB.isNotEmpty()) {
                val nodeB = openB.removeFirst()
                closed.add(nodeB.state)
                if (nodeB == nodeI) return Pair(nodeB, null)
                if (nodeB in openF) return Pair(nodeB, openF[openF.indexOf(nodeB)])
                for (child in nodeB.expand(problem)) {
                    if (child.state !in closed && child !in openB) {
                        openB.addLast(child)
                    }
                }
            }
        }
        return null
    }
}