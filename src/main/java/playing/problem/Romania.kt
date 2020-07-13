package playing.problem

import GraphProblem
import Node
import State
import UndirectedGraph
import playing.agent.Agent
import java.awt.Point

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

    romaniaMap.location = mapOf(
            State("Arad") to Point(91, 492), State("Bucharest") to Point(400, 327), State("Craiova") to Point(253, 288),
            State("Drobeta") to Point(165, 299), State("Eforie") to Point(562, 293), State("Fagaras") to Point(305, 449),
            State("Giurgiu") to Point(375, 270), State("Hirsova") to Point(534, 350), State("Iasi") to Point(473, 506),
            State("Lugoj") to Point(165, 379), State("Mehadia") to Point(168, 339), State("Neamt") to Point(406, 537),
            State("Oradea") to Point(131, 571), State("Pitesti") to Point(320, 368), State("Rimnicu") to Point(233, 410),
            State("Sibiu") to Point(207, 457), State("Timisoara") to Point(94, 410), State("Urziceni") to Point(456, 350),
            State("Vaslui") to Point(509, 444), State("Zerind") to Point(108, 531)
    )

    val problemF = GraphProblem(State("Arad"), listOf(State("Bucharest")), romaniaMap)
    val problemB = GraphProblem(State("Bucharest"), listOf(State("Arad")), romaniaMap)
    val nodeGoal = Agent.simpleBidirectionalSearch(problemF, Node::g, problemB, Node::g)
    println(nodeGoal?.solution())
}