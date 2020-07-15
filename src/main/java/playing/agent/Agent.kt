package playing.agent

import Node
import NodeCutOff
import Problem
import Quadruple
import State
import playing.problem.GraphProblem
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.max
import kotlin.math.min

object Agent {

    /**
     * BFS - tree search
     * Complete? only if branching b is finite
     * Time O(b^d)
     * Space O(b^d)
     * Optimal? if step costs are identical
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
     * Complete? No because loop
     * Time 0(b^m)
     * Space 0(bm)
     * Optimal No
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


    /*
    * Uniform-cost search
    * Complete if b is finite and step costs >= e for positive e
    * Time O(b^(1+[C*:e])
    * Space O(b^(1+[C*:e])
    * Optimal yes
    */
    @ExperimentalStdlibApi
    fun uniformCostSearch(problem: Problem): Node? {
        return greedyBestFirstSearch(problem, f = Node::g)
    }


    /**
     * more complete version of depthLimitedSearch
     * preferred method when search space is large and depth of solution is unknown
     */
    fun IDS(problem: Problem): Node? {
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

        for (depth in 0..Int.MAX_VALUE) {
            val node = depthLimitedSearch(problem, depth)
            if (node != NodeCutOff()) {
                return node
            }
        }
        return null
    }


    /**
     * more general of IDS
     */
    fun ILS(problem: Problem): Node? {
        fun recursiveILS(node: Node, problem: Problem, limit: Int): Pair<Node?, Int> {
            when {
                problem.goalTest(node.state) -> return Pair(node, 0)
                node.g() > limit -> return Pair(NodeCutOff(node.state), node.g())
                else -> {
                    var (nodeDiscarded, localMinDiscarded) = Pair<Node?, Int>(null, Int.MAX_VALUE)
                    for (child in node.expand(problem)) {
                        val result = recursiveILS(child, problem, limit)
                        if (result.first is NodeCutOff) {
                            if (localMinDiscarded > result.second) {
                                nodeDiscarded = result.first
                                localMinDiscarded = result.second
                            }
                        } else if (result.first != null) return result
                    }
                    return Pair(nodeDiscarded, localMinDiscarded)
                }
            }
        }

        var threshold = 0
        while (true) {
            val node = recursiveILS(Node(problem.initial), problem, threshold)
            if (node.first != null && node.first !is NodeCutOff) return node.first
            threshold = node.second
            println(node)
        }
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


    /**
     * Bidirectional search using bfs
     */
    @ExperimentalStdlibApi
    fun simpleBidirectionalSearch(problemF: Problem, fF: (Node) -> Int, problemB: Problem, fB: (Node) -> Int): Node? {
        val nodeF = Node(problemF.initial)
        val nodeB = Node(problemB.initial)
        val frontierF = PriorityQueue(compareBy(fF))
        val frontierB = PriorityQueue(compareBy(fB))
        val reachedF = mutableMapOf(nodeF.state to nodeF)
        val reachedB = mutableMapOf(nodeB.state to nodeB)
        frontierF.add(nodeF)
        frontierB.add(nodeB)
        var solution: Node? = null

        fun proceed(
                isFdirect: Boolean,
                problem: Problem,
                frontier: PriorityQueue<Node>,
                reached: MutableMap<State, Node>,
                reached2: MutableMap<State, Node>,
                solution: Node?
        ): Node? {
            fun joinNodes(isFdirect: Boolean, nodeA: Node, nodeB: Node): Node? {
                fun join2Node(startNode: Node, endNode: Node): Node? {
                    var travelNode: Node? = endNode
                    var nodeResult = startNode
                    while (travelNode != null) {
                        if (travelNode != nodeResult) {
                            val temp = travelNode.parent
                            travelNode.parent = nodeResult
                            nodeResult = travelNode
                            travelNode = temp
                        }else{
                            travelNode = travelNode.parent
                        }
                    }
                    return nodeResult
                }
                return if (isFdirect) join2Node(nodeA, nodeB) else join2Node(nodeB, nodeA)
            }

            var refSolution = solution
            val node = frontier.poll()
            for (child in node.expand(problem)) {
                val s = child.state
                if (s !in reached || child.g() < reached[s]!!.g()) {
                    reached[s] = child
                    frontier.add(child)
                    if (s in reached2) {
                        val solution2 = joinNodes(isFdirect, child, reached2[s]!!)
                        if (solution2?.g() ?: 0 < solution?.g() ?: Int.MAX_VALUE) {
                            refSolution = solution2
                        }
                    }
                }
            }
            return refSolution
        }

        fun terminated(frontierF: PriorityQueue<Node>, frontierB: PriorityQueue<Node>): Boolean {
            return frontierF.isEmpty() || frontierB.isEmpty()
        }

        while (!terminated(frontierF, frontierB)) {
            if (fF(frontierF.peek()) < fB(frontierB.peek()))
                solution = proceed(
                        isFdirect = true,
                        problem = problemF,
                        frontier = frontierF,
                        reached = reachedF,
                        reached2 = reachedB,
                        solution = solution
                )
            else
                solution = proceed(
                        isFdirect = false,
                        problem = problemB,
                        frontier = frontierB,
                        reached = reachedB,
                        reached2 = reachedF,
                        solution = solution
                )
        }
        return solution
    }


    /*------------------------------------------------------------------------------------------*/
    /**
     * expand closet to the goal base on evaluation function
     * greedy bfs = how close a node to a solution
     */
    private fun greedyBestFirstSearch(problem: Problem, f: (Node) -> Int): Node? {
        val frontier = PriorityQueue(compareBy(f))
        frontier.add(Node(problem.initial, pathCost = 0))
        val explored = mutableSetOf<State>()
        val track = mutableMapOf<Node, Int>()
        while (frontier.isNotEmpty()) {
            val node = frontier.poll()
            //return at selected for expansion since node found can be in suboptimal path
            if (problem.goalTest(node.state)) {
                return node
            }
            explored.add(node.state)
            for (child in node.expand(problem)) {
                if (child.state !in explored && child !in frontier) {
                    frontier.add(child)
                    track[child] = f(child)
                } else if (child in frontier) {
                    if (f(child) < f(frontier.elementAt(frontier.indexOf(child)))) {
                        frontier.remove(child)
                        frontier.add(child)
                        track[child] = f(child)
                    }
                }
            }
        }
        return null
        //val nodeGoal = Agent.greedyBestFirstSearch(problem) { problem.h(it) }
    }


    /**
     * minimize the total estimated cost
     */
    fun aStarSearch(problem: Problem): Node? {
        return greedyBestFirstSearch(problem, (problem as GraphProblem)::f)
    }

    /**
     * RBFS
     */
    fun recursiveBestFirstSearch(problem: Problem, f: (Node) -> Int): Node? {

        fun rBFS(problem: Problem, node: Node, threshold: Int): Pair<Node?, Int> {
            if (problem.goalTest(node.state)) return Pair(node, 0)
            var children = node.expand(problem)
            if (children.isEmpty()) return Pair(null, Int.MAX_VALUE)
            for (s in children) {
                s.fValue = max(f(s), node.fValue)
            }
            while (true) {
                children = children.sortedBy { it.fValue }
                val best = children.first()
                if (best.fValue > threshold) return Pair(null, best.fValue)
                val alternative = if (children.size > 1) children[1].fValue else Int.MAX_VALUE
                val pairResult = rBFS(problem, best, min(threshold, alternative))
                val result = pairResult.first
                best.fValue = pairResult.second
                if (result != null) return Pair(result, best.fValue)
            }
        }

        val node = Node(problem.initial)
        node.fValue = f(node)
        return rBFS(problem, node, Int.MAX_VALUE).first
    }
}
/**
 *  uninformed search bfs, dfs, bidirectional bfs, IDS
 *  informed bestfirst - Dijkstra, A*, IDA*, SMA*
 **/