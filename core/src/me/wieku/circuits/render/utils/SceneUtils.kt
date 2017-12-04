package me.wieku.circuits.render.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip


fun Table(color: Color): Table {
	var table = Table()
	table.background = getTxRegion(color)
	return table
}

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

/*fun TextTooltip(text: String): TextTooltip {
	var stl = TextTooltip.TextTooltipStyle()
	stl.background
}*/

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

private fun getTxWRegion(color: Color, width: Int): TextureRegionDrawable {
	return getTxRegion(color, width, 1)
}

private fun getTxHRegion(color: Color, height: Int): TextureRegionDrawable {
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