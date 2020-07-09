class Quadruple(
        private val w: Int,
        private val x: MutableList<Node>,
        private val y: MutableList<Node>,
        private val z: MutableMap<Node, Int>) {
    operator fun component1(): Int {
        return w
    }

    operator fun component2(): MutableList<Node> {
        return x
    }

    operator fun component3(): MutableList<Node> {
        return y
    }

    operator fun component4(): MutableMap<Node, Int> {
        return z
    }
}
