package differentialGrowth

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.kdtree.KDTreeNode
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import utils.closestPointOfBound
import utils.lerp
import kotlin.random.Random

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

/**
 * Equivalent of a OPENRNDR's [ShapeContour] in the differential growth system. Central class in the system, [DGShape]
 * depends on it for its inner contours. It can be used as a standalone if `iterate` is called with a [KDTreeNode] tree
 * managed outside of it. Simpler use is to directly rely on [DGWorld] via [DGWorld.addContour] or [DGWorld.addContours]
 */
class DGContour(
    private var baseContour: ShapeContour,
    private val settings: DGDefinedSettings,
    /**
     * Must use `draw()` to work
     */
    private val fillColor: ColorRGBa? = null
) {
    var lastNodeInjectTime = 0.0
    var closed = baseContour.closed
    var empty = baseContour.empty
//    var winding = baseContour.winding
    var coords: List<Vector2> = listOf()

    init {
        val perimeter = baseContour.length

        coords = List(settings.subSplitsNumber) {
            val p = it / settings.subSplitsNumber.toDouble()
            baseContour.pointAtLength(p * perimeter, 0.1)
        }

        if (settings.debugMode) println("[DGContour.init] Created ${coords.size} coords: $coords")
    }

    fun getForcesOnVertex(vertice: DGVertex, tree: KDTreeNode<Vector2>): Vector2 {
        var force = Vector2.ZERO

        // skip OOB vertice
        if (!settings.bounds.contains(vertice.position)) {
            return force
        }

        force += attractionForceToDirectNeighbors(
            vertice,
            minDistance = settings.minDistance,
            intensity = settings.attractionForce
        )

        force += repulsionForceFromNeighbors(
            vertice,
            repulsionRadius = settings.repulsionRadius,
            intensity = settings.repulsionForce,
            tree = tree
        )

        force += alignmentForceToDirectNeighbors(
            vertice,
            intensity = settings.alignmentForce,
        )

        // repulsion force from edge
        val (distance, closest) = closestPointOfBound(settings.bounds, vertice.position)
        if (distance > 0 && distance < settings.repulsionRadius) {
            val repulsion = -(closest + vertice.position).normalized * 1.5
            force += repulsion
        }

        return force
    }


    /**  Splits too long vertices based on [DGSettings.maxDistance] */
    private fun splitVerticesByDistance(vertices: List<DGVertex>): List<DGVertex> {
        val res = mutableListOf<Vector2>()

        vertices.forEachIndexed { i, vertex ->
            if (vertex.next != null && vertex.position.distanceTo(vertex.next!!) > settings.maxDistance) {
                val midPoint = (vertex.position + vertex.next!!) / 2.0
                res += vertex.position
                res += midPoint
            } else {
                res += vertex.position
            }
        }

        // not optimized but avoid for the moment to worry about next and previous updates
        return res.asVertices(closed)
    }

    private fun removeTooCloseVertices(vertices: List<DGVertex>): List<DGVertex> {
        val nextPositions: MutableList<Vector2> = mutableListOf()
        vertices.forEachIndexed { i, vertex ->
            if (vertex.next == null) {
                nextPositions += vertex.position
            } else {
                val distance = vertex.position.distanceTo(vertex.next!!)
                if (distance > settings.minDistance) {
                    nextPositions += vertex.position
                } else if (settings.debugMode) {
                    println("[DGContour.removeTooCloseVertices] skipped vertex #$i - vertex: $vertex")
                }
            }
        }
        return nextPositions.asVertices(closed)
    }

    fun constructContour(coords: List<Vector2>): ShapeContour {
        // was on a previously closed path and it "exploded" or no coords given
        if (empty || coords.isEmpty() || coords.size <= 2 && closed) {
            empty = true
            if (settings.debugMode) println("[DGContour.constructContour($coords)] Empty contour detected")
            return ShapeContour.EMPTY
        }

        if (settings.debugMode) println("[DGContour.constructContour([...${coords.size} coords]))] Going to construct contour")

        val c = contour {
            moveTo(coords[0])
            for (i in 1 until coords.size) {
                lineTo(coords[i])
            }
            if (closed) {
                if (settings.debugMode) println("[DGContour.constructContour] Close contour on ${coords[0]}")
                lineTo(coords[0])
                close()
            }
        }
        return c
    }

    fun injectNodeRandom(vertices: List<DGVertex>): List<DGVertex> {
        // Choose two connected nodes at random
        val index = Random.nextInt(1, vertices.size)
        val mutablePositions = vertices.map{ it.position }.toMutableList()
        val vertex = vertices[index]

        if (
            vertex.previous != null &&
            vertex.next != null &&
            vertex.position.distanceTo(vertex.previous!!) > settings.minDistance
        ) {
            // Create a new node in the middle
            val midpointNode = (vertex.position + vertex.previous!!) / 2.0
            if (settings.debugMode) println("[DGContour.injectNodeRandom] Injected random vertice at $midpointNode from $vertex and ${vertex.previous}")
            mutablePositions.add(index, midpointNode)
        }

        return mutablePositions.asVertices(closed)
    }


    context(Program)
    fun iterate(tree: KDTreeNode<Vector2>) {
        if (empty) {
            return
        }

        if (settings.debugMode) println("[DGContour.iterate] Before: ${coords.size} coords")
        var nextVertices = coords.asVertices(closed).toMutableList()

        // first we apply forces on all vertices
        nextVertices.forEach { v ->
            val force = getForcesOnVertex(v, tree)
            val nextPosition = v.position + force
            v.position = v.position.lerp(nextPosition, settings.maxVelocity)
        }

        // then split too long edges
        nextVertices = splitVerticesByDistance(nextVertices).toMutableList()

        // remove too close edges
        nextVertices = removeTooCloseVertices(nextVertices).toMutableList()

        // periodically inject random node
        if (nextVertices.size > 3 && (clock() - lastNodeInjectTime) >= settings.nodeInjectionInterval) {
            nextVertices = injectNodeRandom(nextVertices).toMutableList()
            lastNodeInjectTime = clock()
        }

        // filter OoB vertices & write as resulting coords
        coords = nextVertices.map{ it.position }.filter { settings.bounds.contains(it) }

        // if previously closed and have only 2 coords we totally empty the list
        if (closed && coords.size <= 2 || coords.isEmpty()) {
            coords = listOf()
            empty = true
        }

        if (settings.debugMode) println("[DGContour.iterate] After: ${coords.size} coords")
    }

    fun points(): Set<Vector2> {
        return coords.toSet()
    }

    context(Drawer)
    fun draw() {
        if (fillColor != null) {
            fill = fillColor
        }
        contour(contour)
    }

    /** Drawable `ShapeContour` */
    val contour: ShapeContour
        get() {
            return constructContour(coords)
        }
}