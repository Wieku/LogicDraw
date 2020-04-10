package me.wieku.circuits.desktop

import me.wieku.circuits.render.gl.IPixelBufferObject
import me.wieku.circuits.render.gl.IPixelBufferObjectFactory

class PixelBufferObjectFactory : IPixelBufferObjectFactory {
    override fun createPixelBufferObject(width: Int, height: Int, format: Int, dataType: Int, pixelSize: Int, internalFormat: Int): IPixelBufferObject {
        return PixelBufferObject(width, height, format, dataType, pixelSize, internalFormat)
    }
}