package com.jomondb.gsuite.dataobject

import java.io.ByteArrayOutputStream

data class FileDTO (
    val content: ByteArrayOutputStream,
    val id: String,
    val name: String,
    val mimeType: String,
)