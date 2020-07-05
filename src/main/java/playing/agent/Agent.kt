package playing.agent

import Node
import Problem
import State

object Agent {

    @ExperimentalStdlibApi
    fun bfs(problem: Problem): Node? {
        var node = Node(problem.initial)
        if (problem.goalTest(node.state)) return node
        val frontier = ArrayDeque<Node>(listOf(node))
        val explored = mutableSetOf<State>()

        while (frontier.size != 0) {
            node = frontier.removeFirst()
            explored.add(node.state)
            for (child in node.expand(problem)) {
                if (child.state !in explored || child !in frontier) {
                    if (problem.goalTest(child.state)) return child
                    frontier.addLast(child)
                }
            }
        }
        return null
    }
}