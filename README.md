> Created from the [openrndr template](https://github.com/openrndr/openrndr-template)

# Differential growth experimentation using [OPENRNDR](https://openrndr.org/)

I wanted to share the code I used to generate [this short video](https://www.youtube.com/shorts/a4OGqRgODa8). It's based
on @jasonwebb's [work done using p5js](https://github.com/jasonwebb/2d-differential-growth-experiments/) and heavily 
adapted to OPENRNDR. Please note it's not a 1:1 adaptation.


The repository include the base code (aka the library) to transform OPENRNDR shapes using differential growth and
various examples.

## General principles
The way I implemented the differential growth for OPENRNDR was to make a matching between OPENRNDR's shapes / classes 
and my own classes so we can use them independently if needed.

- [openrndr.shape.Shape](https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape/index.html) -> [differentialGrowth.DGShape](src/main/kotlin/differentialGrowth/DGShape.kt)
- [openrndr.shape.ShapeContour](https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape-contour/index.html) -> [differentialGrowth.DGContour](src/main/kotlin/differentialGrowth/DGContour.kt)

And finally I created a [diffenrentialGrowth.DGWorld](src/main/kotlin/differentialGrowth/DGWorld.kt) to allow multiple
shapes interact with each other in the same "World". I advise only using this `DGWorld` class to register the various
shapes you want to deform, but you could use, in theory, directly `DGShape` or `DGContour`.   



## Transform basic shapes

![With basic shapes](video/DGWithBasicShapes.gif)

```kotlin
import differentialGrowth.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.scatter
import org.openrndr.shape.Circle
import kotlin.random.Random


fun main() = application {
    configure {
        width = 1080
        height = 1080
    }

    program {
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
```


## Transform complex shapes
The differential growth algorithm should also work with more complex shapes, like text for instance.

![DG with text](video/DGWithText.gif)

```kotlin
import differentialGrowth.DGSettings
import differentialGrowth.DGWorld
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.shapes.text.shapesFromText
import org.openrndr.math.Vector2

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }

    program {
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

            with (drawer) {
                world.draw()
            }
        }
    }
}
```