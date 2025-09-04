package differentialGrowth

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.kdtree.kdTree
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour

/**
 * Helper to manage a Differential Growth figures evolving together.
 * It will manage each registered [DGContour] or [DGShape]
 */
class DGWorld(
    settings: DGSettings,
    private var paused: Boolean = false
) {
    private val settings = DGDefaultSettings.merge(settings)
    private var dgShapes = listOf<DGShape>()
    private var dgContours = listOf<DGContour>()
    private var tree = listOf<Vector2>().kdTree()

    context(Program)
    fun iterate() {
        if (paused) {
            return
        }
        val points = mutableSetOf<Vector2>()

        dgShapes.forEach { points += it.points() }
        dgContours.forEach { points += it.points() }

        tree = points.kdTree()

        if (settings.debugMode) println("[DGWorld.iterate()] Registered ${points.size} points")

        with (program) {
            dgShapes.forEach { it.iterate(tree) }
            dgContours.forEach { it.iterate(tree) }
        }

    }

    @Suppress("MemberVisibilityCanBePrivate")
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
        contours.forEach { addContour(it, fillColorRGBa) }
    }

    context(Drawer)
    fun draw() {
        dgContours.forEach {
            it.draw()
        }
        dgShapes.forEach {
            it.draw()
        }
    }

    fun togglePause() {
        paused = !paused
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun pause() {
        paused = true
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun play(){
        paused = false
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    val contours: List<ShapeContour>
        get() {
            return contours.map { it.contour }
        }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    val shapes: List<Shape>
        get() {
            return dgShapes.map { it.shape }
        }
}