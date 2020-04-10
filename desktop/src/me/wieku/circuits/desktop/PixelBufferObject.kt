package me.wieku.circuits.desktop

import me.wieku.circuits.render.gl.IPixelBufferObject
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER
import org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE

import java.nio.ByteBuffer

class PixelBufferObject(override val width: Int, override val height: Int, val format: Int, val dataType: Int, val pixelSize: Int, val iformat: Int = format) : IPixelBufferObject {

    override val textureId: Int = glGenTextures()
    var pbo: Int
    var buffer: ByteBuffer

    private var dirty = true

    init {
        bind(0)
        glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage2D(GL_TEXTURE_RECTANGLE, 0, iformat, width, height, 0, format, dataType, null as ByteBuffer?)

        pbo = glGenBuffers()

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pbo)
        glBufferData(GL_PIXEL_UNPACK_BUFFER, height * width * pixelSize.toLong(), GL_DYNAMIC_DRAW)

        buffer = glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY, null)
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0)
    }

    override fun setPixel3(x: Int, y: Int, color: Int) {
        buffer.put((y * width + x) * pixelSize, ((color ushr 16) and 0xff).toByte())
        buffer.put((y * width + x) * pixelSize + 1, ((color ushr 8) and 0xff).toByte())
        buffer.put((y * width + x) * pixelSize + 2, (color and 0xff).toByte())
        dirty = true
    }

    override fun setInt(index: Int, data: Int) {
        buffer.putInt(index, data)
        dirty = true
    }

    override fun setFloat(index: Int, data: Float) {
        buffer.putFloat(index, data)
        dirty = true
    }

    override fun setByte(index: Int, data: Byte) {
        buffer.put(index, data)
        dirty = true
    }

    override fun updateTexture(unit: Int, force: Boolean) {
        bind(unit)

        if (dirty || force) {
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pbo)
            glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER)
            glTexSubImage2D(GL_TEXTURE_RECTANGLE, 0, 0, 0, width, height, format, dataType, 0L)
            buffer = glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY, width * height * pixelSize.toLong(), buffer)
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0)

            dirty = false
        }

    }

    override fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_RECTANGLE, textureId)
    }

    override fun dispose() {
        glDeleteTextures(textureId)
        glDeleteBuffers(pbo)
    }

}