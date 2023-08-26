package cga.exercise.components.geometry

import cga.exercise.components.collision.AABB
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f


/**
 * Bounding Box als Eigenschaft hinzugefügt, um Collision mit Objekt zu checken.
 * Eigenschaft ist eine Liste, um komplexere Bounding Boxes für ein Objekt erschaffen zu können.
 *
 * Beispiel: Der Garten ist ein einzelnes Objekt. Wir wollen z.B. Kollision mit den Wänden/Bögen haben.
 *          Hierfür brauchen wir drei schmale Bounding Boxes anstatt einen großen Würfel, um noch in
 *          den Garten "rein" zu können.
 *
 * Die erste Bounding Box ist initiiert und per default auf 0 gesetzt, weil wir sie vielleicht nicht bei jedem Objekt brauchen/haben wollen.
 * Außerdem können Objekte so erstmal in die Szene geladen werden, ohne direkt die Koordinaten für die Boxen parat haben zu müssen.
 */
open class Transformable(
    private var modelMatrix: Matrix4f = Matrix4f(),
    var parent: Transformable? = null,
    val boundingBoxList: MutableList<AABB> = mutableListOf(
        (AABB(
            Vector3f(0f), Vector3f(0f)
        ))
    )
) {
    /**
     * Returns copy of object model matrix
     * @return modelMatrix
     */
    fun getModelMatrix(): Matrix4f {
        return Matrix4f(modelMatrix)
    }

    /**
     * Returns multiplication of world and object model matrices.
     * Multiplication has to be recursive for all parents.
     * Hint: scene graph
     * @return world modelMatrix
     */
    fun getWorldModelMatrix(): Matrix4f {
        return parent?.getWorldModelMatrix()?.mul(getModelMatrix()) ?: getModelMatrix()
    }

    /**
     * Rotates object around its own origin.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     */
    fun rotate(pitch: Float, yaw: Float, roll: Float) {
        modelMatrix.rotateXYZ(pitch, yaw, roll)
    }

    /**
     * Rotates object around given rotation center.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     * @param altMidpoint rotation center
     */
    fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        val tmp = Matrix4f()
        tmp.translate(altMidpoint)
        tmp.rotateXYZ(pitch, yaw, roll)
        tmp.translate(Vector3f(altMidpoint).negate())
        modelMatrix = tmp.mul(modelMatrix)
    }

    /**
     * Translates object based on its own coordinate system.
     * @param deltaPos delta positions
     */
    fun translate(deltaPos: Vector3f) {
        modelMatrix.translate(deltaPos)
    }

    /**
     * Translates object based on its parent coordinate system.
     * Hint: this operation has to be left-multiplied
     * @param deltaPos delta positions (x, y, z)
     */
    fun preTranslate(deltaPos: Vector3f) {
        modelMatrix = Matrix4f().translate(deltaPos).mul(modelMatrix)
    }

    /**
     * Scales object related to its own origin
     * @param scale scale factor (x, y, z)
     */
    fun scale(scale: Vector3f) {
        modelMatrix.scale(scale)
    }

    /**
     * Returns position based on aggregated translations.
     * Hint: last column of model matrix
     * @return position
     */
    fun getPosition(): Vector3f {
        return Vector3f(modelMatrix.m30(), modelMatrix.m31(), modelMatrix.m32())
    }

    /**
     * Returns position based on aggregated translations incl. parents.
     * Hint: last column of world model matrix
     * @return position
     */
    fun getWorldPosition(): Vector3f {
        val wmat = getWorldModelMatrix()
        return Vector3f(wmat.m30(), wmat.m31(), wmat.m32())
    }

    /**
     * Returns x-axis of object coordinate system
     * Hint: first normalized column of model matrix
     * @return x-axis
     */
    fun getXAxis(): Vector3f {
        return Vector3f(
            modelMatrix.m00(), modelMatrix.m01(), modelMatrix.m02()
        ).normalize()
    }

    /**
     * Returns y-axis of object coordinate system
     * Hint: second normalized column of model matrix
     * @return y-axis
     */
    fun getYAxis(): Vector3f {
        return Vector3f(
            modelMatrix.m10(), modelMatrix.m11(), modelMatrix.m12()
        ).normalize()
    }

    /**
     * Returns z-axis of object coordinate system
     * Hint: third normalized column of model matrix
     * @return z-axis
     */
    fun getZAxis(): Vector3f {
        return Vector3f(
            modelMatrix.m20(), modelMatrix.m21(), modelMatrix.m22()
        ).normalize()
    }

    /**
     * Returns x-axis of world coordinate system
     * Hint: first normalized column of world model matrix
     * @return x-axis
     */
    fun getWorldXAxis(): Vector3f {
        val wmat = getWorldModelMatrix()
        return Vector3f(
            wmat.m00(), wmat.m01(), wmat.m02()
        ).normalize()
    }

    /**
     * Returns y-axis of world coordinate system
     * Hint: second normalized column of world model matrix
     * @return y-axis
     */
    fun getWorldYAxis(): Vector3f {
        val wmat = getWorldModelMatrix()
        return Vector3f(
            wmat.m10(), wmat.m11(), wmat.m12()
        ).normalize()
    }

    /**
     * Returns z-axis of world coordinate system
     * Hint: third normalized column of world model matrix
     * @return z-axis
     */
    fun getWorldZAxis(): Vector3f {
        val wmat = getWorldModelMatrix()
        return Vector3f(
            wmat.m20(), wmat.m21(), wmat.m22()
        ).normalize()
    }

    fun setPosition(newPosition: Vector3f) {
        val translationMatrix = Matrix4f().translate(newPosition)
        modelMatrix = translationMatrix.mul(Matrix4f(getModelMatrix()))
    }

    fun setWorldPosition(newPosition: Vector3f) {
        // Berechne Verschiebung vom Weltursprung zur neuen Position
        val worldTranslation = Matrix4f().translate(newPosition)

        // Setze das Objekt an gewünschte Weltposition
        modelMatrix = worldTranslation
    }

    fun setWorldMatrix(matrix: Matrix4f) {
        modelMatrix = Matrix4f(matrix)
        parent?.let {
            modelMatrix = it.getWorldModelMatrix().mul(modelMatrix)
        }
    }

    fun lookAt(target: Vector3f) {

        val forward = getWorldZAxis().negate().normalize()

        val directionToTarget = Vector3f(target).sub(getWorldPosition()).normalize()

        // calculate the rotation quaternion to align the forward direction with the direction to the target.
        val rotation = Quaternionf().rotationTo(forward, directionToTarget)

        // convert rotation quaternion to a rotation matrix.
        val rotationMatrix = Matrix4f().rotation(rotation)

        modelMatrix = rotationMatrix.mul(getModelMatrix())
    }
}