package playing.problem

import Action
import Problem
import State
import playing.agent.Agent

/**
 * state is represented as N-element array
 * with index = col. if any value at index = -1 -> that col does not contains a queen
 */
class NQueensProblem(
        override val initial: State,
        private val boardSize: Int = 8
) : Problem(initial, listOf()) {
    override fun actions(state: State): List<Action> {
        //"""In the leftmost empty column, try all non-conflicting rows."""
        val ar = state.state as IntArray
        return if (!ar.contains(-1)) listOf()
        else {
            val col = ar.indexOf(-1)
            val possibleMove = IntArray(boardSize) { it }
                    .filter { row -> !conflicted(state, row, col) }
            return possibleMove.foldRight(mutableListOf()) { row, initList ->
                val newAr = ar.copyOf()
                newAr[col] = row
                initList.add(Action(State(newAr), 0))
                initList
            }
        }
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action?, state2: State): Int = 0

    override fun result(state: State, action: Action): State {
        return action.destState
    }

    private fun conflict(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (row1 == -1 || row2 == -1) return false
        return row1 == row2
                || col1 == col2
                || row1 - col1 == row2 - col2
                || row1 + col1 == row2 + col2
    }

    private fun conflicted(state: State, row: Int, col: Int): Boolean {
        //only check from leftmost to col - 1
        val ar = state.state as IntArray
        for (c in 0 until col) {
            if (conflict(row, col, ar[c], c)) {
                return true
            }
        }
        return false
    }

    override fun goalTest(state: State): Boolean {
        val ar = state.state as IntArray
        if (ar.contains(-1)) return false
        for (col in 0 until boardSize) {
            if (conflicted(state, ar[col], col)) {
                return false
            }
        }
        return true
    }

    fun h(state: State): Int {
        //"""Return number of conflicting queens for a given node"""
        var numConflicts = 0
        val ar = state.state as IntArray
        for (i in 0 until boardSize) {
            if (ar[i] == -1) numConflicts += 1
            else {
                if (i == boardSize - 1) break
                for (j in i + 1 until boardSize) {
                    if (conflict(ar[i], i, ar[j], j)) {
                        numConflicts += 1
                    }
                }
            }
        }
        return numConflicts
    }


    override fun value(state: State): Int = -h(state)
}


@ExperimentalStdlibApi
fun main() {
    val initList = IntArray(8)
    initList.fill(-1, 0, 8)
    val initQueenState = State(initList)
    val problem = NQueensProblem(initQueenState, 8)
    val sol = Agent.hillClimbing(problem)
    println(sol?.solution()?.map { (it.destState.state as IntArray).toList() })
}