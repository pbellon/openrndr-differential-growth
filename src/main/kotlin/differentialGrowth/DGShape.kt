package differentialGrowth

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.kdtree.KDTreeNode
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape

class DGShape(
    baseShape: Shape,
    private val settings: DGDefinedSettings,
    /** Must use `draw()` to work */
    private val fillColor:ColorRGBa? = null
) {
    private var dgContours = baseShape.contours.map { DGContour(it, settings) }

    context(Program)
    fun iterate(tree: KDTreeNode<Vector2>) {
        with (program) {
            dgContours.forEach { it.iterate(tree) }
        }

        // remove empty contours
        dgContours = dgContours.filter { !it.empty }
    }

    context(Drawer)
    fun draw() {
        if (fillColor != null) {
            fill = fillColor
        }
        shape(shape)
    }

    fun points(): Set<Vector2> {
        val results = mutableSetOf<Vector2>()
        dgContours.forEach {
            results += it.points()
        }
        return results
    }

    val shape: Shape
        get(){
            return Shape(dgContours.map{ it.contour })
        }
}