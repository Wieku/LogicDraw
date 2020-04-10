package me.wieku.circuits.render.gl

import com.badlogic.gdx.utils.Disposable
import java.lang.IllegalStateException

interface IPixelBufferObject : Disposable {

    val width: Int

    val height: Int

    val textureId: Int

    fun setPixel3(x: Int, y: Int, color: Int)

    fun setInt(index: Int, data: Int)

    fun setFloat(index: Int, data: Float)

    fun setByte(index: Int, data: Byte)

    fun updateTexture(unit: Int, force: Boolean)

    fun bind(unit: Int)

    companion object {
        var currentFactory: IPixelBufferObjectFactory? = null

        fun createInstance(width: Int, height: Int, format: Int, dataType: Int, pixelSize: Int, internalFormat: Int = format): IPixelBufferObject {
            return currentFactory?.createPixelBufferObject(width, height, format, dataType, pixelSize, internalFormat)
                    ?: throw IllegalStateException("Factory can not be null.")
        }
    }
}