package playing.problem

import Action
import Problem
import State

class NQueensProblem(
        override val initial: State,
        private val boardSize: Int = 8
) : Problem(initial) {
    override fun actions(state: State): List<Action> {
        val ar = (state.state as IntArray)
        if (!ar.contains(-1)) return listOf()
        else {

        }
    }

    override fun pathCost(costSoFar: Int, state1: State, action: Action?, state2: State): Int {
        TODO("Not yet implemented")
    }

    override fun result(state: State, action: Action): State {
        TODO("Not yet implemented")
    }

    private fun conflict(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        return row1 == row2
                || col1 == col2
                || row1 - col1 == row2 - col2
                || row1 + col1 == row2 + col2
    }

    private fun conflicted(state: State, row: Int, col: Int): Boolean {

    }
}