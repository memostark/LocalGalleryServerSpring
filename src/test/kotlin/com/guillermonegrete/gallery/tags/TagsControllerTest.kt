package com.guillermonegrete.gallery.tags

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.data.files.dto.ImageFileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagEntity
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.InetAddress
import java.util.*

@WebMvcTest
class TagsControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val mapper: FileMapper,
    @Autowired val objectMapper: ObjectMapper,
) {

    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner

    @MockkBean private lateinit var tagsRepository: TagsRepository
    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository
    @MockkBean private lateinit var mediaFileRepository: MediaFileRepository

    private val ipAddress = InetAddress.getLocalHost().hostAddress

    @Test
    fun `Given tags, when get all endpoint called, then return them`(){

        val tags = listOf(TagEntity("my_tag"))
        every { tagsRepository.findAll() } returns tags

        val result = mockMvc.perform(get("/tags"))
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<List<TagEntity>>() {})

        assertThat(resultResponse).hasSize(1)
        val tagResult = resultResponse.first()
        val expectedTag = tags.first()
        assertTagEqual(expectedTag, tagResult)
    }

    @Test
    fun `Given no tags, when add endpoint called, then create new tag`(){

        val tag = TagEntity("my_tag")
        every { tagsRepository.save(any()) } returns tag

        val result = mockMvc.perform(post("/tags/add").param("name", "Cats"))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(tag, resultResponse)
    }

    @Test
    fun `Given valid file id, when add tag endpoint called, then add tag`(){

        every { mediaFileRepository.findById(0) } returns Optional.of(ImageEntity("saved_image.jpg"))
        val savedTag = TagEntity("my_tag")
        every { tagsRepository.findByName("my_tag") } returns savedTag

        every { tagsRepository.save(savedTag) } returns savedTag

        val result = mockMvc.perform(post("/files/{id}/tags", 0)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "my_tag"}"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(savedTag, resultResponse)
    }

    @Test
    fun `Given valid tag id, when get files by tag endpoint called, then files returned`(){

        every { tagsRepository.existsById(0) } returns true
        val file = MediaFile("my_file.jpg", folder = MediaFolder("my_folder"))
        every { mediaFileRepository.findFilesByTagsId(0, DEFAULT_PAGEABLE) } returns PageImpl(listOf(file), DEFAULT_PAGEABLE, 1)

        val result = mockMvc.perform(get("/tags/{id}/files", 0))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<ImageFileDTO>>() {})
        val expected = SimplePage(listOf(mapper.toDtoWithHost(file, ipAddress)), 1, 1)
        assertThat(resultResponse).isEqualTo(expected)
    }

    @Test
    fun `Given valid folder id, when get tags of folder endpoint called, then tags returned`(){
        val folderId = 0L
        val tag = TagEntity("my_tag")
        val files = listOf(MediaFile("image.jpg", tags = mutableSetOf(tag)))
        every { mediaFolderRepository.findByIdOrNull(folderId) } returns MediaFolder("my_folder", files, folderId)

        val result = mockMvc.perform(get("/folders/{id}/tags", 0))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<List<TagEntity>>() {})
        assertThat(resultResponse).hasSize(1)
        val tagResult = resultResponse.first()
        assertTagEqual(tag, tagResult)
    }

    @Test
    fun `Given valid folder and tag ids, when get files by folder and tag endpoint called, then files returned`(){
        val tagId = 3L
        every { tagsRepository.existsById(tagId) } returns true
        val folderId = 2L
        every { mediaFolderRepository.existsById(folderId) } returns true
        val files = listOf(MediaFile("image.jpg"))
        every { mediaFileRepository.findFilesByTagsIdAndFolderId(tagId, folderId, DEFAULT_PAGEABLE) } returns PageImpl(files, DEFAULT_PAGEABLE, files.size.toLong())

        val result = mockMvc.perform(get("/folders/{folderId}/tags/{tagId}", folderId, tagId))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<ImageFileDTO>>() {})
        val expected = SimplePage(files.map { mapper.toDtoWithHost(it, ipAddress) }, 1, 1)
        assertThat(resultResponse).isEqualTo(expected)
    }

    private fun assertTagEqual(expected: TagEntity, actual: TagEntity){
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.name).isEqualTo(expected.name)
        assertThat(actual.creationDate).isEqualTo(expected.creationDate)
    }

    @TestConfiguration
    internal class InnerConfig{
        @Bean
        fun fileMapper() = FileMapper()
    }

    companion object {
        val DEFAULT_PAGEABLE = PageRequest.of(0, 20)
    }
}
