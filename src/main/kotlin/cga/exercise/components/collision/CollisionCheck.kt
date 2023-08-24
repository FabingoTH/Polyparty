package cga.exercise.components.collision

import org.joml.Vector3f

// Korrektur für Garten
enum class OverlappedAxis(val difference: Vector3f) {
    MIN_X(Vector3f(-0.1f, 0f, 0f)),
    MAX_X(Vector3f(0.1f, 0f, 0f)),
    MIN_Z(Vector3f(0f, 0f, -0.1f)),
    MAX_Z(Vector3f(0f, 0f, -0.1f))
}

/**
 * AABB (Axis Aligned Bounding Box) repräsentiert die "Hit Box" eines jeden Objekts.
 */
class AABB(val min: Vector3f, val max: Vector3f) {

    /**
     * Überprüft Kollision zwischen den Bounding Boxes zweier Objekte.
     * @param otherBox Bounding Box des anderen Objektes.
     * @return true, wenn die Objekte kollidieren.
     */
    fun collidesWith(otherBox: AABB): Boolean {
        return !(max.x < otherBox.min.x || min.x > otherBox.max.x ||
                max.y < otherBox.min.y || min.y > otherBox.max.y ||
                max.z < otherBox.min.z || min.z > otherBox.max.z)
    }

    /**
     * Überprüft Kollision zwischen Objekten.
     * @param otherBox Bounding Box des anderen Objektes.
     * @return Vector3f (mit xyz-Koordinaten (dabei nur eine definiert, die anderen 0f)), um den für das Object translatiert werden soll.
     */
    fun getAxisToCorrect(otherBox: AABB): OverlappedAxis? {

        return if (max.x > otherBox.min.x && min.x < otherBox.max.x) {
            // overlap on x-axis
            if (max.x - otherBox.min.x < otherBox.max.x - min.x) {
                OverlappedAxis.MIN_X
            } else {
                OverlappedAxis.MAX_X
            }
        } else if (max.z > otherBox.min.z && min.z < otherBox.max.z) {
            // overlap on z-axis
            if (max.z - otherBox.min.z < otherBox.max.z - min.z) {
                OverlappedAxis.MIN_Z
            } else {
                OverlappedAxis.MAX_Z
            }
        } else null

    }
}

