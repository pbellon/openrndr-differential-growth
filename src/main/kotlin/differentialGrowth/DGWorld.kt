package differentialGrowth

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.kdtree.KDTreeNode
import org.openrndr.extra.kdtree.kdTree
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour

/**
 * Helper to manage a Differential Growth figures evolving together.
 * It will manage each registered [DGContour]
 */
class DGWorld(
    settings: DGSettings,
    private var paused: Boolean = false
) {
    val settings = DGDefaultSettings.merge(settings)
    private var dgShapes = listOf<DGShape>()
    private var dgContours = listOf<DGContour>()
    private var tree = listOf<Vector2>().kdTree()

    context(Program)
    fun iterate() {
        if (paused) {
            return
        }
        val points = mutableSetOf<Vector2>()
        with(tree) {
            dgShapes.forEach { points += it.points() }
            dgContours.forEach { points += it.points() }
        }
        tree = points.kdTree()

        if (settings.debugMode) println("[DGWorld.iterate()] Registered ${points.size} points")

        with (program) {
            dgShapes.forEach { it.iterate(tree) }
            dgContours.forEach { it.iterate(tree) }
        }

    }

    fun addShape(shape: Shape) {
        val mutableShapes = dgShapes.toMutableList()
        mutableShapes.add(DGShape(shape, settings))
        dgShapes = mutableShapes.toList()
    }

    fun addShapes(shapes: List<Shape>) {
        shapes.forEach { addShape(it) }
    }

    fun addContour(contour: ShapeContour, fillColorRGBa: ColorRGBa? = null) {
        val mutableContours = dgContours.toMutableList()
        mutableContours.add(DGContour(contour, settings, fillColorRGBa))
        dgContours = mutableContours.toList()
    }

    fun addContours(contours: List<ShapeContour>, fillColorRGBa: ColorRGBa? = null) {
        contours.forEach { addContour(it) }
    }

    context(Drawer)
    fun draw() {
        dgContours.forEach() {
            it.draw()
        }
        dgShapes.forEach() {
            it.draw()
        }
    }

    fun togglePause() {
        paused = !paused
    }

    fun pause() {
        paused = true
    }

    fun play(){
        paused = false
    }


    val contours: List<ShapeContour>
        get() {
            return contours.map { it.contour }
        }

    val shapes: List<Shape>
        get() {
            return dgShapes.map { it.shape }
        }
}