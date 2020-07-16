package playing.problem

import Action
import Problem
import State

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
            possibleMove.foldRight(mutableListOf()) { row, initList ->
                ar[col] = row
                initList.add(Action(State(ar), 0))
                initList
            }
        }
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action?, state2: State): Int {
        TODO("Not yet implemented")
    }

    override fun result(state: State, action: Action): State {
        return action.destState
    }

    private fun conflict(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        return row1 == row2
                || col1 == col2
                || row1 - col1 == row2 - col2
                || row1 + col1 == row2 + col2
    }

    private fun conflicted(state: State, row: Int, col: Int): Boolean {
        //only check from leftmost to col - 1
        val ar = state.state as IntArray
        for (c in 0..col) {
            if (conflict(row, col, ar[c], c)) {
                return false
            }
        }
        return true
    }

    override fun goalTest(state: State): Boolean {
        val ar = state.state as IntArray
        if (ar.contains(-1)) return false
        for (col in 0..boardSize) {
            if (conflicted(state, ar[col], col)) {
                return false
            }
        }
        return true
    }
}


fun main() {
    val initList = IntArray(8)
    initList.fill(-1, 0, 8)
    val initQueenState = State(initList)
    val p = NQueensProblem(initQueenState, 8)
    println(p.actions(initQueenState))

}