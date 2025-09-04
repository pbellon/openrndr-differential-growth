package differentialGrowth

import org.openrndr.shape.Rectangle

open class DGSettings(
    /** Minimum distance between nodes. Used in attraction, pruning, and injection */
    open val minDistance: Double? = null,

    /** Maximum distance between nodes before they are split */
    open val maxDistance: Double? = null,

    /** Radius to search for nearby nodes for repulsion force */
    open val repulsionRadius: Double? = null,

    /** Maximum velocity at which a node can move per frame */
    open val maxVelocity: Double? = null,

    /** Maximum attraction force between connected nodes */
    open val attractionForce: Double? = null,

    /** Maximum repulsion force between nearby nodes */
    open val repulsionForce: Double? = null,

    /** Maximum alignment force between connected nodes */
    open val alignmentForce: Double? = null,

    /** Interval (in ms) between call to node injection routine */
    open val nodeInjectionInterval: Double? = null,

    /** Number of splits on each edge when initializing contour */
    open val subSplitsNumber: Int? = null,

    /** Logs debug info */
    open val debugMode: Boolean? = null,

    open val bounds: Rectangle,
) {
    override fun toString(): String {
        return "DGSettings(" +
                "minDistance=$minDistance, " +
                "maxDistance=$maxDistance, " +
                "repulsionRadius=$repulsionRadius, " +
                "maxVelocity=$maxVelocity, " +
                "attractionForce=$attractionForce, " +
                "repulsionForce=$repulsionForce, " +
                "alignmentForce=$alignmentForce, " +
                "nodeInjectionInterval=$nodeInjectionInterval, " +
                "nbPointsPerContour=$subSplitsNumber)"
    }
}

class DGDefinedSettings(
    /** Minimum distance between nodes. Used in attraction, pruning, and injection */
    override var minDistance: Double,

    /** Maximum distance between nodes before they are split */
    override var maxDistance: Double,

    /** Radius to search for nearby nodes for repulsion force */
    override var repulsionRadius: Double,

    /** Maximum velocity at which a node can move per frame */
    override val maxVelocity: Double,

    /** Maximum attraction force between connected nodes */
    override var attractionForce: Double,

    /** Maximum repulsion force between nearby nodes */
    override var repulsionForce: Double,

    /** Maximum alignment force between connected nodes */
    override var alignmentForce: Double,

    /** Interval (in ms) between call to node injection routine */
    override val nodeInjectionInterval: Double,

    override val debugMode: Boolean,

    /** Number of splits on each edge when initializing contour */
    override val subSplitsNumber: Int,

    override  val bounds: Rectangle,
) : DGSettings(bounds = Rectangle.EMPTY)

var DGDefaultSettings = DGDefinedSettings(
    alignmentForce = .7,
    attractionForce = .15,
    maxDistance = 6.0,
    maxVelocity = .1,
    minDistance = 1.5,
    subSplitsNumber = 50,
    nodeInjectionInterval = .100,
    repulsionForce = .8,
    repulsionRadius = 17.0,
    debugMode = false,
    bounds = Rectangle.EMPTY,
)

fun DGDefinedSettings.merge(settings: DGSettings): DGDefinedSettings {
    return DGDefinedSettings(
        alignmentForce = settings.alignmentForce ?: alignmentForce,
        attractionForce = settings.attractionForce ?: attractionForce,
        maxDistance = settings.maxDistance ?: maxDistance,
        maxVelocity = settings.maxVelocity ?: maxVelocity,
        minDistance = settings.minDistance ?: minDistance,
        nodeInjectionInterval = settings.nodeInjectionInterval ?: nodeInjectionInterval,
        repulsionForce = settings.repulsionForce ?: repulsionForce,
        repulsionRadius = settings.repulsionRadius ?: repulsionRadius,
        subSplitsNumber = settings.subSplitsNumber ?: subSplitsNumber,
        debugMode = settings.debugMode ?: debugMode,
        bounds = settings.bounds,
    )
}

