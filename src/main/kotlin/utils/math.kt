package utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

fun lerp(a: Double, b: Double, t: Double): Double {
    return a + (b - a) * t
}

fun Vector2.lerp(b: Vector2, t: Double): Vector2 {
    return Vector2(
        lerp(this.x, b.x, t),
        lerp(this.y, b.y, t),
    )
}


fun closestPointOnSegment(segment: Pair<Vector2, Vector2>, point: Vector2): Vector2 {
    val y1 = segment.first.y
    val x1 = segment.first.x

    val x2 = segment.second.x
    val y2 = segment.second.y


    val m1 = (y2 - y1) / (x2 - x1)
    val m2 = -1.0 / m1

    val a = point.x
    val b = point.y

    var x = ((m1 * x1) - m2*a - y1 + b) / (m1 - m2)
    var y = m2*(x-a)+b

    if (x1 == x2) {
        return Vector2(
            x1, b
        )
    }

    if (y1 == y2) {
        return Vector2(
            a, y1
        )
    }

    return Vector2(x, y)
}

fun closestPointOfBound(b: Rectangle, p: Vector2): Pair<Double, Vector2> {
    val topLeft = Vector2(b.x, b.y)
    val topRight = Vector2(b.x + b.width, b.y)
    val bottomRight = Vector2(b.x + b.width, b.y + b.height)
    val bottomLeft = Vector2(b.x, b.y - b.height)

    var edges = listOf<Pair<Vector2, Vector2>>(
        Pair(
            topLeft, // TOP-LEFT
            topRight // TOP-RIGHT
        ),
        Pair(
            topRight,
            bottomRight,
        ),
        Pair(
            bottomRight,
            bottomLeft
        ),
        Pair(bottomLeft, topLeft)
    )

    var distance = Double.POSITIVE_INFINITY
    var closestPoint = topLeft

    for (edge in edges) {
        val closestOnEdge = closestPointOnSegment(edge, p)
        val distanceToClosest = closestOnEdge.distanceTo(p)
        if (distanceToClosest < distance) {
            distance = distanceToClosest
            closestPoint = closestOnEdge
        }
    }

    return Pair(distance, closestPoint)
}
