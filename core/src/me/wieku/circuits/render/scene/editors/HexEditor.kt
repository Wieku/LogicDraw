package me.wieku.circuits.render.scene.editors

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import me.wieku.circuits.render.scene.onClickS
import java.io.File
import java.util.*

class HexEditor : VisTable(true) {
	private val mainTable = VisTable(true)
	private val programDir = File("programs")
	private var byteArea: ByteArea = ByteArea(8)
	init {
		programDir.mkdir()
		//region tool table
		val toolTable = VisTable()
		//region wordLengthSpinner
		val wordLengthSpinnerModel = IntSpinnerModel(8, 2, 32, 2)
		val wordLengthSpinner = Spinner("Word length: ", wordLengthSpinnerModel)
		wordLengthSpinner.textField.setFocusTraversal(false)
		wordLengthSpinner.addListener {
			when (it) {
				is ChangeListener.ChangeEvent -> {
					onWordLengthChanged(wordLengthSpinnerModel.value)
					true
				}
				else -> false
			}
		}
		toolTable.add(wordLengthSpinner)
		//endregion

		//region select file
		val programSelectorTable = VisTable(true)
		val selectBox = generateProgramList()
		programSelectorTable.add(selectBox)

		val loadButton = VisTextButton("Load")
		loadButton.onClickS {
			if(selectBox.selected != null) {
				onProgramLoad(programDir.resolve(selectBox.selected))
			}
		}
		toolTable.add(VisLabel(" Select Program: "))
		toolTable.add(programSelectorTable)
		toolTable.add(loadButton).padLeft(2f)

		//endregion
//        val okButton = VisTextButton("Save & Apply")
//        okButton.onClickS { close() }
 //       toolTable.add(okButton)
		//endregion
		mainTable.align(Align.topLeft)
		mainTable.add(toolTable).align(Align.top)
		mainTable.row()
		val scrollPane = VisScrollPane(byteArea)
		scrollPane.setFlickScroll(false)
		scrollPane.setFadeScrollBars(false)
		mainTable.add(scrollPane).align(Align.topLeft).growX().fill()
		add(mainTable).expand().fill()
	}

	override fun getPrefHeight(): Float {
		return 600f
	}

	override fun getPrefWidth(): Float {
		return 800f
	}

	public fun loadFromBytes(bytes: ByteArray){
		byteArea.loadFromBytes(bytes)
	}

	public fun saveToBytes() = byteArea.saveToBytes()

	private fun onProgramLoad(file: File) {
		byteArea.loadFromBytes(file.readBytes())
	}

	private fun onWordLengthChanged(newValue: Int) {
		val saved = byteArea.saveToBytes()
		byteArea.wordLength = newValue

		if(saved.isEmpty()){
			byteArea.loadFromBytes(ByteArray(1))
		}else{
			byteArea.loadFromBytes(saved)
		}
	}

	private fun generateProgramList(): VisSelectBox<String> {
		val array = Array<String>()
		if (programDir.exists()) {
			programDir.listFiles { f -> f.isFile }.forEach { array.add(it.name) }
		}
		return VisSelectBox<String>().apply {
			setItems(array)
		}
	}
}

class ByteArea(var wordLength: Int) : VisTable(true) {
	private var currColumn = 0
	private var rowIndex = 0

	init {
		align(Align.topLeft)
	}


	fun loadFromBytes(bytes: ByteArray) {
		clear()
		currColumn = 0
		rowIndex = 0
		for (byte in bytes) {
			addByteField(toHex2(byte))
		}
	}

	fun saveToBytes(): ByteArray {
		return children.filter { it is ByteField }
				.map { it as ByteField }
				.filter { it.text.isNotEmpty() }
				.map { fromHex2(it.text) }.toByteArray()
	}

	private fun addByteField(value: String) {
		if (!hasChildren()) {
			add(VisLabel(toHex4(rowIndex++)))
		}
		val field = ByteField()
		field.text = value
		field.addListener(ByteEditorFocusListener())
		add(field)
		currColumn++
		if (currColumn == wordLength) {
			row()
			add(VisLabel(toHex4(rowIndex++)))
			currColumn = 0
		}
	}

	private fun toHex2(byte: Byte): String = String.format("%02X", byte)
	private fun fromHex2(str: String): Byte =  java.lang.Integer.parseInt(str, 16).toByte()
	private fun toHex4(byte: Int): String = String.format("%04X", byte)

	inner class ByteEditorFocusListener : FocusListener() {
		override fun keyboardFocusChanged(event: FocusEvent?, actor: Actor?, focused: Boolean) {
			super.keyboardFocusChanged(event, actor, focused)
			try {
				if (children.last { it is ByteField } == actor) {
					addByteField("")
				}
			}catch (e: NoSuchElementException) {
				//ignore
			}

		}
	}

}

open class ByteField : VisTextField("00") {
	init {
		maxLength = 2
		textFieldFilter = HexFilter()
	}

	override fun setText(str: String?) {
		super.setText(str?.toUpperCase())
	}

	override fun createInputListener(): InputListener {
		return object : VisTextField.TextFieldClickListener() {
			override fun keyTyped(event: InputEvent, character: Char): Boolean {

				val b = super.keyTyped(event, Character.toUpperCase(character))

				if (cursorPosition == text.length && (text.length == maxLength || (text.isEmpty() && character.toInt() == 0x8 ))) {// backspace == 0x8
						next(character.toInt() == 0x8)
				}
				return b
			}
		}
	}

	override fun getPrefWidth(): Float {
		return ByteFiledWidth.prefWidth
	}

	//Used for calculate max width for ByteField
	private object ByteFiledWidth : VisTextField("DD") {

		override fun getPrefWidth(): Float {
			return layout.width + style.font.spaceWidth
		}
	}
}

class HexFilter : VisTextField.TextFieldFilter {
	override fun acceptChar(textField: VisTextField?, c: Char): Boolean {
		return when (c.toUpperCase()) {
			in CharRange('0', '9') -> true
			'A', 'B', 'C', 'D', 'F', ' ' -> true
			else -> false
		}
	}
}
