package me.wieku.circuits.render.utils

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

import java.lang.reflect.Field
import java.util.HashMap

enum class FontManager(var fontName: String, var genSize: Int, var widthScale: Float, var heightScale: Float) {

	ROBOTO("Roboto-Regular", 64, 35f, 39f);

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
			var data = fonts[type]!!.fontData
			var dataCopy = BitmapFontData()
			try {
				var h = data.glyphs
				var g = kotlin.Array<kotlin.Array<Glyph?>>(h.size){kotlin.Array<Glyph?>(h[0].size){null}}
				println("${h.size}  ${g.size}")
				println("${h[0].size}  ${g[0].size}")
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

		fun init() {

			println("Initializing FontManager...")

			var asset = if(Gdx.app.getType()== ApplicationType.Android) "" else "assets/"

			for (type in values()) {
				var file = Gdx.files.internal(asset+"font/" + type.fontName + ".ttf");

				try {
					var generator = FreeTypeFontGenerator(file)
					var pam = FreeTypeFontParameter()
					pam.size = type.genSize
					pam.genMipMaps = true
					pam.borderWidth = 1.5f
					pam.borderColor = Color(0f,0f,0f, 1f)
					pam.hinting = Hinting.Full
					pam.magFilter = TextureFilter.Linear
					pam.minFilter = TextureFilter.MipMapLinearLinear
					pam.characters = "ABCDEFGHIJKLMNOQPRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789èéêëìí,<.>/?;:'\"[{]}]|\\=+-_`~!@#$%^&*()"

					var d = generator.generateData(pam)

					var field = d.javaClass.getDeclaredField("regions")
					field.isAccessible = true

					var fontData = FontData(field.get(d) as Array<TextureRegion>, d)

					fonts.put(type, fontData)

					generator.dispose()

				} catch (e: Exception) {
					e.printStackTrace()
				}

			}
			println("FontManager initialized!")
		}
	}


	internal data class FontData(var regions:Array<TextureRegion>, var fontData: BitmapFontData)
}
