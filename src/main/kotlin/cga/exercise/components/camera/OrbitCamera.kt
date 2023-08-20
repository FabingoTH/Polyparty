package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable
import org.joml.Matrix4f
import org.joml.Vector3f

class OrbitCamera(private val target: Transformable) : TronCamera() {
    var distance: Float = 5.0f // Anfangsabstand zur Zielposition
        set(value) {
            field = value.coerceIn(minDistance, maxDistance) // Setzt den gültigen Wertebereich
        }
    var elevation: Float = 40.0f // Anfangswinkel der Höhe
        set(value) {
            field = value.coerceIn(minElevation, maxElevation) // Setzt den gültigen Wertebereich
        }
    var azimuth: Float = 0.0f // Anfangswinkel der Horizontalen
        set(value) {
            field = value
            if (field > 360.0f) field -= 360.0f
            if (field < 0.0f) field += 360.0f
        }

    private val minDistance = 2.0f
    private val maxDistance = 20.0f
    private val minElevation = 2.0f
    private val maxElevation = 60.0f

    init {
        updateCameraPosition()
    }

    fun updateCameraPosition() {
        val horizontalDistance = distance * Math.cos(Math.toRadians(elevation.toDouble()))
        val verticalDistance = distance * Math.sin(Math.toRadians(elevation.toDouble()))

        val xOffset = horizontalDistance * Math.sin(Math.toRadians(azimuth.toDouble()))
        val zOffset = horizontalDistance * Math.cos(Math.toRadians(azimuth.toDouble()))

        val targetPosition = target.getWorldPosition()

        val newPosition = Vector3f(targetPosition).add(xOffset.toFloat(), verticalDistance.toFloat(), zOffset.toFloat())
        setPosition(newPosition)

        // Verwende die JOML lookAt-Funktion
        val viewMatrix = Matrix4f().lookAt(
            newPosition, // Aktualisierte Kameraposition
            targetPosition, // Zielposition
            Vector3f(0.0f, 1.0f, 0.0f) // Oben-Vektor
        )
        setWorldMatrix(viewMatrix.invertAffine())
    }

    fun update(dt: Float) {
        updateCameraPosition()
    }
}