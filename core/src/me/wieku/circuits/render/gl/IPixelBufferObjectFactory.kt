package me.wieku.circuits.render.gl

interface IPixelBufferObjectFactory {
    fun createPixelBufferObject(width: Int, height: Int, format: Int, dataType: Int, pixelSize: Int, internalFormat: Int): IPixelBufferObject
}