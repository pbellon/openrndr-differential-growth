package differentialGrowth

import org.openrndr.extra.kdtree.KDTreeNode
import org.openrndr.math.Vector2

import utils.lerp

fun attractionForce(position:Vector2, attractor: Vector2, intensity: Double, minDistance: Double? = null): Vector2 {
    var force = Vector2.ZERO

    if (minDistance == null || position.distanceTo(attractor) > minDistance) {
        force = position.lerp(attractor, intensity) - position
    }

    return force
}

fun attractionForceToDirectNeighbors(vertex: DGVertex, minDistance: Double, intensity: Double): Vector2 {
    var force = Vector2.ZERO
    // Move towards next node, if there is one
    if (vertex.next != null) {
        force += attractionForce(vertex.position, vertex.next!!, minDistance, intensity)
    }
    // Move towards previous node, if there is one
    if (vertex.previous != null) {
        force += attractionForce(vertex.position, vertex.previous!!, minDistance, intensity)
    }

    return force
}

/**
 * TODO: Abstract closest neighbors querying to avoid depending on a specific algorithm to allow more easy switch
 *       between various algorithm (Quadtree, KDTree, KNN, ...)
 */
fun repulsionForceFromNeighbors(
    vertex: DGVertex,
    repulsionRadius: Double,
    intensity: Double,
    tree: KDTreeNode<Vector2>
): Vector2 {
    val position = vertex.position
    var force = Vector2.ZERO
    tree.findAllInRadius(position, repulsionRadius).forEach { node ->
        if (node != position) {
            val diff = position - node
            force += diff.normalized * intensity
        }
    }
    return force
}

fun alignmentForceToDirectNeighbors(vertex: DGVertex, intensity: Double): Vector2 {
    var force = Vector2.ZERO
    val prev = vertex.previous
    val next = vertex.next
    if (prev != null && next != null) {
        // Find the midpoint between the neighbors of this node
        val midpoint = (prev + next) / 2.0
        // and compute attraction force to it with intensity
        force = attractionForce(vertex.position, attractor =  midpoint, intensity)
    }
    return force
}