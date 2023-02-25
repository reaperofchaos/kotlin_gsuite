package com.jomondb.gsuite.mapper

import com.google.api.services.drive.model.File
import com.jomondb.gsuite.dataobject.FileDTO
import com.jomondb.gsuite.domain.DownloadFile
import java.io.ByteArrayOutputStream

class GsuiteFileMapper {

    fun fileToDownloadFile(file: File): DownloadFile{
        return DownloadFile(id =file.id, name =file.name)
    }

    fun buildFileDTO(content: ByteArrayOutputStream, file: File): FileDTO{
        return FileDTO(content = content, id = file.id, name= file.name, mimeType = file.mimeType )
    }
}