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