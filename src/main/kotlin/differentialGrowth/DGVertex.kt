package differentialGrowth

import org.openrndr.math.Vector2

/**
 * Utility class to ease process
 */
data class DGVertex(
    var position: Vector2,
    var next: Vector2? = null,
    var previous: Vector2? = null,
)

fun List<Vector2>.asVertices(closed: Boolean): List<DGVertex> {
    val max = this.size - 1
    return this.mapIndexed { i , position ->
        DGVertex(
            position = position,
            previous = if (i > 0) {
                this[i-1]
            } else if (closed) {
                this[this.size - 1]
            } else null,
            next = if (i < max) {
                this[i+1]
            } else if (closed) {
                this[0]
            } else null,
        )
    }
}