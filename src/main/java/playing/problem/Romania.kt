package playing.problem

import playing.*

@ExperimentalStdlibApi
fun main() {
    val romaniaMap = UndirectedGraph(mutableMapOf(
            State("Arad") to mutableMapOf(
                    State("Zerind") to 75,
                    State("Sibiu") to 140,
                    State("Timisoara") to 118
            ),
            State("Bucharest") to mutableMapOf(
                    State("Urziceni") to 85,
                    State("Pitesti") to 101,
                    State("Giurgiu") to 90,
                    State("Fagaras") to 211
            ),
            State("Craiova") to mutableMapOf(
                    State("Drobeta") to 120,
                    State("Rimnicu") to 146,
                    State("Pitesti") to 138
            ),
            State("Drobeta") to mutableMapOf(
                    State("Mehadia") to 75
            ),
            State("Eforie") to mutableMapOf(
                    State("Hirsova") to 86
            ),
            State("Fagaras") to mutableMapOf(
                    State("Sibiu") to 99
            ),
            State("Hirsova") to mutableMapOf(
                    State("Urziceni") to 98
            ),
            State("Iasi") to mutableMapOf(
                    State("Vaslui") to 92,
                    State("Neamt") to 87
            ),
            State("Lugoj") to mutableMapOf(
                    State("Timisoara") to 111,
                    State("Mehadia") to 70
            ),
            State("Oradea") to mutableMapOf(
                    State("Zerind") to 71,
                    State("Sibiu") to 151
            ),
            State("Pitesti") to mutableMapOf(State("Rimnicu") to 97),
            State("Rimnicu") to mutableMapOf(State("Sibiu") to 80),
            State("Urziceni") to mutableMapOf(State("Vaslui") to 142))
    )

    val problem = GraphProblem(State("Arad"), listOf(State("Bucharest")), romaniaMap)
    val nodeGoal = bfs(problem)
    println(nodeGoal?.solution())
    println(nodeGoal?.path())
}

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