package me.wieku.circuits.utils

import java.io.File

fun File.makeBackup(backupDir: Boolean = false) {
	if(exists()) {
		val newFile = if(backupDir) parentFile.resolve("backup"+File.separator+name) else File(absolutePath+".bak")
		copyTo(newFile, true)
	}
}

fun File.restoreBackup(backupDir: Boolean = false) {
	val newFile = if(backupDir) parentFile.resolve("backup"+File.separator+name) else File(absolutePath+".bak")
	if(newFile.exists())
		newFile.copyTo(this, true)
}

fun File.backupExists(backupDir: Boolean = false): Boolean {
	val newFile = if(backupDir) parentFile.resolve("backup"+File.separator+name) else File(absolutePath+".bak")
	return newFile.exists()
}

fun File.removeBackup(backupDir: Boolean = false) {
	val newFile = if(backupDir) parentFile.resolve("backup"+File.separator+name) else File(absolutePath+".bak")
	if(newFile.exists())
		newFile.delete()
}