package com.guillermonegrete.gallery.data.files.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.tags.data.TagEntity
import java.time.Instant

sealed class FileDTO(
    @get:JsonProperty("file_type")
    val type: FileType
)

data class ImageFileDTO(
    val url: String,
    val width: Int,
    val height: Int,
    val creationDate: Instant,
    val lastModified: Instant,
    val tags: Set<TagEntity>,
    val id: Long,
): FileDTO(FileType.Image)

data class VideoFileDTO(
    val url: String,
    val width: Int,
    val height: Int,
    val creationDate: Instant,
    val lastModified: Instant,
    val duration: Int,
    val tags: Set<TagEntity>,
    val id: Long,
): FileDTO(FileType.Video)

data class SingleImageFile(
    val folder: Folder,
    @JsonUnwrapped
    val dto: ImageFileDTO,
): FileDTO(FileType.Image)

data class SingleVideoFile(
    val folder: Folder,
    @JsonUnwrapped
    val dto: VideoFileDTO,
): FileDTO(FileType.Video)

enum class FileType{
    Image,
    Video
}
