package com.guillermonegrete.gallery.data

data class Folder(
        val name:String,
        val coverUrl: String,
        val count: Int
)

data class GetFolderResponse(
        val name: String,
        val folders: List<Folder>
)