package com.guillermonegrete.gallery.data

import javax.persistence.*


@Entity
data class MediaFile(
    @Column(unique=true)
    val filename: String = "",
    val width: Int = 0,
    val height: Int = 0,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id")
    var folder: MediaFolder = MediaFolder(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0
)