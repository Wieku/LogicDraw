package me.wieku.circuits.render.utils

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Pool

import java.util.HashMap

enum class FontManager(var fontName: String, var genSize: Int, var widthScale: Float, var heightScale: Float) {

	ROBOTO("Roboto-Regular", 64, 35f, 39f),
	ROBOTO_16("Roboto-Regular", 16, 8.75f, 9.75f);

	internal data class FontData(var regions:Array<TextureRegion>, var fontData: BitmapFontData)

	private var cache = object: Pool<BitmapFont>() {
		override fun newObject(): BitmapFont = createFont(this@FontManager)
	}

	companion object {
		private var fonts = HashMap<FontManager, FontData>()

		fun getFont(type: FontManager, size: Int):BitmapFont {
			val font = type.cache.obtain()
			font.data.setScale(size / type.widthScale, size / type.heightScale)
			return font
		}

		fun createFont(type: FontManager): BitmapFont {
			val data = fonts[type]!!.fontData
			val dataCopy = BitmapFontData()
			try {
				val h = data.glyphs
				val g = kotlin.Array(h.size){kotlin.Array<Glyph?>(h[0].size){null}}
				for(i in 0 until h.size) {
					if (h[i] != null) {
						for (j in 0 until h[i].size) {
							if (h[i][j] != null) {
								g[i][j] = Glyph()
								for (field in g[i][j]!!.javaClass.fields) {
									field.isAccessible = true
									field.set(g[i][j], h[i][j]!!.javaClass.getField(field.name).get(h[i][j]))
								}
							}
						}
					}
				}

				for (field in dataCopy.javaClass.fields) {
					field.isAccessible = true
					if(field.name == "glyphs")
						field.set(dataCopy, g)
					else field.set(dataCopy, data.javaClass.getField(field.name).get(data))
				}

			} catch (e: Exception) {
				throw GdxRuntimeException("Failed to create font", e)
			}
			return BitmapFont(dataCopy, fonts[type]!!.regions, false)
		}

		fun putToCache(type: FontManager, font: BitmapFont) = type.cache.free(font)

		fun dispose() {
			fonts.clear()
		}

		@Suppress("UNCHECKED_CAST")
		fun init() {

			println("Initializing FontManager...")

			val asset = if(Gdx.app.type == ApplicationType.Android) "" else "assets/"

			for (type in values()) {
				val file = Gdx.files.internal(asset+"font/" + type.fontName + ".ttf")

				try {
					val generator = FreeTypeFontGenerator(file)
					val pam = FreeTypeFontParameter()
					pam.size = type.genSize
					pam.genMipMaps = true
					pam.hinting = Hinting.Full
					pam.magFilter = TextureFilter.Linear
					pam.minFilter = TextureFilter.MipMapLinearLinear
					pam.characters = "ABCDEFGHIJKLMNOQPRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789èéêëìí,<.>/?;:'\"[{]}]|\\=+-_`~!@#$%^&*()"

					val data = generator.generateData(pam)

					val field = data.javaClass.getDeclaredField("regions")
					field.isAccessible = true

					fonts.put(type, FontData(field.get(data) as Array<TextureRegion>, data))

					generator.dispose()

				} catch (e: Exception) {
					e.printStackTrace()
				}

			}
			println("FontManager initialized!")
		}
	}
}
