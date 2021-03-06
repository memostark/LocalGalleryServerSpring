package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile

interface FoldersRepository {

    fun getFolders(path: String): List<String>

    fun getImages(folder: String): List<ImageFile>
}