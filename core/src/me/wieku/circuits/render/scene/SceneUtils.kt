package me.wieku.circuits.render.scene

import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip
import me.wieku.circuits.world.ClassicWorld
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import me.wieku.circuits.render.utils.FontManager
import javafx.scene.Cursor.cursor
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle






fun OrthographicCamera.fit(world: ClassicWorld, stage: Stage) {
	if (stage.width * zoom > world.width) {
		position.x = Math.max(-stage.width * zoom / 2 + world.width, Math.min(stage.width * zoom / 2, position.x))
	} else {
		position.x = Math.max(stage.width * zoom / 4, Math.min(world.width - stage.width * zoom / 4, position.x))
	}

	if (stage.height * zoom > world.height) {
		position.y = Math.max(-stage.height * zoom / 2 + world.height, Math.min(stage.height * zoom / 2, position.y))
	} else {
		position.y = Math.max(stage.height * zoom / 4, Math.min(world.width - stage.height * zoom / 4, position.y))
	}
}


fun Table(color: Color): Table {
	var table = Table()
	table.background = getTxRegion(color)
	return table
}

fun Label(text: String, color: Color, size: Int) = Label(text, getLabelStyle(color, size))

fun StripeButton(background: Color, color: Color, size: Int): ImageButton {
	var stl = ImageButton.ImageButtonStyle()
	stl.up = getStripeImg(background, color, size)
	stl.over = getStripeImg(background.lerp(Color.WHITE, 0.05f), color, size)
	stl.down = getStripeImg(background.lerp(Color.LIGHT_GRAY, 0.05f), color, size)
	return ImageButton(stl)
}

fun ColorButton(color:Color): ImageButton {
	var stl = ImageButton.ImageButtonStyle()
	stl.up = getTxRegion(color)
	stl.over = getTxRegion(color.lerp(Color.WHITE, 0.05f))
	stl.down = getTxRegion(color.lerp(Color.LIGHT_GRAY, 0.05f))
	return ImageButton(stl)
}

fun TextTooltip(text: String): TextTooltip {
	var stl = TextTooltip.TextTooltipStyle()
	stl.label = getLabelStyle(Color.WHITE, 10)
	stl.wrapWidth = 100f
	stl.background = getTxRegion(Color.BLACK)
	return TextTooltip(text, stl)
}

fun getLabelStyle(color: Color, size: Int): LabelStyle {
	val stl = LabelStyle()
	stl.font = if(size < 12) {
		 FontManager.getFont(FontManager.ROBOTO_16, size)
	} else FontManager.getFont(FontManager.ROBOTO, size)

	stl.font.data.markupEnabled = true
	stl.fontColor = color.cpy()
	return stl
}

fun getTextButtonStyle(color: Color, size: Int): TextButton.TextButtonStyle {
	val stl = TextButton.TextButtonStyle()
	stl.font = FontManager.getFont(FontManager.ROBOTO, size)
	stl.fontColor = color
	return stl
}

fun getTextButtonStyle(background: Color, color: Color, size: Int): TextButton.TextButtonStyle {
	var d = getTxRegion(background)
	val stl = TextButton.TextButtonStyle(d, d, d, FontManager.getFont(FontManager.ROBOTO, size))
	stl.fontColor = color
	return stl
}

fun getTextFieldStyle(bg: Color, textColor: Color, size: Int): TextFieldStyle {
	val stl = TextFieldStyle()
	stl.background = getTxRegion(bg)
	stl.background.leftWidth = 5f
	stl.background.rightWidth = 5f
	stl.background.topHeight = 5f
	stl.background.bottomHeight = 5f
	stl.font = FontManager.getFont(FontManager.ROBOTO, size)
	stl.selection = getTxRegion(Color(0.8f, 0.8f, 0.8f, 0.5f))
	stl.cursor = getTxRegion(Color.LIGHT_GRAY)
	stl.fontColor = textColor.cpy()
	return stl
}

fun getScrollPaneStyle(bg: Color, knob: Color): ScrollPaneStyle {
	val style = ScrollPaneStyle()

	style.hScroll = getTxHRegion(bg, 10)
	style.vScroll = getTxWRegion(bg, 10)
	style.hScrollKnob = getTxHRegion(knob, 10)
	style.vScrollKnob = getTxWRegion(knob, 10)

	return style
}

private fun getStripeImg(background: Color, color: Color, size: Int): TextureRegionDrawable {
	var pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
	pixmap.setColor(background)
	pixmap.fillRectangle(0, 0, size, size)
	pixmap.setColor(color)
	var xa = size*0.1f
	var hei = (size-2*xa)/5f
	pixmap.fillRectangle(xa, xa, size-2*xa, hei)
	pixmap.fillRectangle(xa, xa+2*hei, size-2*xa, hei)
	pixmap.fillRectangle(xa, xa+4*hei, size-2*xa, hei)
	return getTxRegion(pixmap)
}

private fun Pixmap.fillRectangle(x: Float, y: Float, width: Float, height: Float) = fillRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())

private fun getTxRegion(color: Color): TextureRegionDrawable {
	return getTxRegion(color, 1, 1)
}

fun getTxWRegion(color: Color, width: Int): TextureRegionDrawable {
	return getTxRegion(color, width, 1)
}

fun getTxHRegion(color: Color, height: Int): TextureRegionDrawable {
	return getTxRegion(color, 1, height)
}

private fun getTxHRegionUB(color: Color, uB: Color, height: Int): TextureRegionDrawable {
	val pixMap = Pixmap(1, height, Pixmap.Format.RGBA8888)
	pixMap.setColor(color)
	pixMap.fillRectangle(0, 0, 1, height)
	pixMap.setColor(uB)
	pixMap.fillRectangle(0, 0, 1, height / 8)
	return getTxRegion(pixMap)
}

private fun getTxRegion(color: Color, width: Int, height: Int): TextureRegionDrawable {

	val pixMap = Pixmap(width, height, Pixmap.Format.RGBA8888)
	pixMap.setColor(color)
	pixMap.fillRectangle(0, 0, width, height)
	return getTxRegion(pixMap)
}

private fun getTxRegion(texture: Texture): TextureRegionDrawable {
	return TextureRegionDrawable(TextureRegion(texture))
}

private fun getTxRegion(pixMap: Pixmap): TextureRegionDrawable {
	val tex = Texture(pixMap)
	tex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)
	return TextureRegionDrawable(TextureRegion(tex))
}