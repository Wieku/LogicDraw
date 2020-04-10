package me.wieku.circuits.render.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import me.wieku.circuits.render.gl.IPixelBufferObject
import org.lwjgl.opengl.GL11

class WorldRenderer(val width: Int, val height: Int) {

    private var statePBO: IPixelBufferObject = IPixelBufferObject.createInstance(width, height, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, 1, GL30.GL_R8)
    private var elementStatePBO: IPixelBufferObject = IPixelBufferObject.createInstance(width, height, GL20.GL_RGBA, GL20.GL_FLOAT, 16, GL30.GL_RGBA32F)

    private var idlePBO: IPixelBufferObject = IPixelBufferObject.createInstance(width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, 3)
    private var activePBO: IPixelBufferObject = IPixelBufferObject.createInstance(width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, 3)

    private var mapShader: ShaderProgram = ShaderProgram(Gdx.files.internal("assets/shaders/map.vsh").readString(), Gdx.files.internal("assets/shaders/map.fsh").readString())
    private var quad: Mesh

    init {
        if (!mapShader.isCompiled) {
            println(mapShader.log)
        }

        quad = Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0))
        quad.setVertices(floatArrayOf(
                0f, 0f, 0f, 0f, 0f,
                width.toFloat(), 0f, 0f, 1f, 0f,
                width.toFloat(), height.toFloat(), 0f, 1f, 1f,
                0f, height.toFloat(), 0f, 0f, 1f))
        quad.setIndices(shortArrayOf(0, 1, 2, 2, 3, 0))

    }

    fun setElementData(x: Int, y: Int, colorIdle: Int, colorActive: Int) {
        idlePBO.setPixel3(x, y, colorIdle)
        activePBO.setPixel3(x, y, colorActive)
    }

    fun setElementStateData(x: Int, y: Int, idHorizontal: Int, idVertical: Int) {
        setElementStateDataHorizontal(x, y, idHorizontal)
        setElementStateDataVertical(x, y, idVertical)
    }

    fun setElementStateDataHorizontal(x: Int, y: Int, idHorizontal: Int) {
        elementStatePBO.setFloat( (y * width + x) * 16, (idHorizontal % width).toFloat() / width)
        elementStatePBO.setFloat( (y * width + x) * 16 + 4, (idHorizontal / width).toFloat() / height)
    }

    fun setElementStateDataVertical(x: Int, y: Int, idVertical: Int) {
        elementStatePBO.setFloat( (y * width + x) * 16 + 8, (idVertical % width).toFloat() / width)
        elementStatePBO.setFloat( (y * width + x) * 16 + 12, (idVertical / width).toFloat() / height)
    }

    fun setStateData(id: Int, value: Boolean) {
        statePBO.setByte(id, if(value) 0xff.toByte() else 0)
    }

    fun render(camera: Matrix4) {
        mapShader.begin()

        mapShader.setUniformMatrix("u_projTrans", camera)

        statePBO.updateTexture(2, false)
        mapShader.setUniformi("texture_states", 2)

        elementStatePBO.updateTexture(3, false)
        mapShader.setUniformi("texture_element_states", 3)

        idlePBO.updateTexture(4, false)
        mapShader.setUniformi("texture_idle", 4)

        activePBO.updateTexture(5, false)
        mapShader.setUniformi("texture_active", 5)

        mapShader.setUniformi("size", width, height)

        quad.render(mapShader, GL20.GL_TRIANGLES)

        mapShader.end()

        Gdx.gl.glActiveTexture(GL_TEXTURE0)
    }

}