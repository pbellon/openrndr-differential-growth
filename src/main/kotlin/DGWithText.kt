import differentialGrowth.DGSettings
import differentialGrowth.DGWorld
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BufferMultisample
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.font.loadFace
import org.openrndr.draw.renderTarget
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.shapes.bounds.bounds
import org.openrndr.extra.shapes.text.shapesFromText
import org.openrndr.extra.videoprofiles.H265Profile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }

    program {
        extend(ScreenRecorder()) {
            frameClock = true
            outputToVideo = false
            frameRate = 30
            multisample = BufferMultisample.SampleCount(4)
            profile = H265Profile().apply {
                constantRateFactor = 18
            }
        }

        val face =
            loadFace("https://github.com/IBM/plex/raw/master/packages/plex-mono/fonts/complete/otf/IBMPlexMono-Bold.otf")
        val shapes = shapesFromText(face, "Differential\nGrowth", 155.0, position = drawer.bounds.center - Vector2(450.0, 100.0), )
        val bounds = drawer.bounds.offsetEdges(-50.0)
        val settings = DGSettings(
            alignmentForce = .3,
            attractionForce = .1,
            maxDistance = 8.8,
            maxVelocity = .2,
            minDistance = .5,
            nodeInjectionInterval = .100,
            repulsionForce = .6,
            repulsionRadius = 12.0,
            debugMode = false,
            subSplitsNumber = 120,
            bounds = bounds,
        )

        val world = DGWorld(settings)
        world.addShapes(shapes)

        extend {
            with (program) {
                world.iterate()
            }

            drawer.stroke = null
            drawer.fill = ColorRGBa.fromHex("#bb4430")

            world.shapes.forEachIndexed(){ _index, shape ->
                drawer.shape(shape)
            }
        }
    }
}