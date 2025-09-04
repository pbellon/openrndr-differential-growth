import differentialGrowth.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.scatter
import org.openrndr.extra.videoprofiles.gif
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.shape.Circle
import kotlin.random.Random


fun main() = application {
    configure {
        width = 1080
        height = 1080
    }

    program {
        val record = ScreenRecorder().apply {
            outputToVideo = false
        }

        extend(record) {
            gif()
        }

        val palette = listOf(
            ColorRGBa.fromHex( "#0eb1d2").shade(0.76),
            ColorRGBa.fromHex("#bb4430"),
            ColorRGBa.fromHex("#26547C"),
            ColorRGBa.fromHex( "#5c0099"),
            ColorRGBa.fromHex("#f7c674").shade(0.85),
            ColorRGBa.fromHex( "#fb9649"),
        )
        val offset = 50.0
        val bounds = drawer.bounds.offsetEdges(-offset)
        val settings = DGSettings(
            alignmentForce = .7,
            attractionForce = .07,
            maxDistance = 12.0,
            minDistance = 1.5,
            maxVelocity = .2,
            nodeInjectionInterval = .01,
            repulsionForce = 0.6,
            repulsionRadius = 35.0,
            subSplitsNumber = 60,
            bounds = bounds,
        )

        val world = DGWorld(settings)
        val points = bounds.scatter(150.0, distanceToEdge = 90.0)

        val circles = points.map { pt ->
            val radius = Random.nextDouble(100.0, 150.0)
            Circle(pt, radius)
        }

        circles.forEachIndexed {i, circle ->
            world.addContour(circle.contour, palette[i % palette.size])
        }

        keyboard.keyDown.listen {
            when {
                it.name == "space" -> world.togglePause()
            }
        }

        extend {
            world.iterate()
            drawer.stroke = null
            with (drawer) {
                world.draw()
            }
        }
    }
}