package com.jomondb.gsuite.mapper

import com.google.api.services.drive.model.File
import com.jomondb.gsuite.domain.DownloadFile

class GsuiteFileMapper {

    fun fileToDownloadFile(file: File): DownloadFile{
        return DownloadFile(file.id, file.name)
    }

}