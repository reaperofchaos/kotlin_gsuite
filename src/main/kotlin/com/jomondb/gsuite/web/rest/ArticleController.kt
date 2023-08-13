package com.jomondb.gsuite.web.rest

import com.jomondb.gsuite.dataobject.ReesponseDTO
import com.jomondb.gsuite.mapper.FilePathMapper
import com.jomondb.gsuite.service.DriveService
import com.jomondb.gsuite.service.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/source")
@RestController
class ArticleController {
    val driveService = DriveService()
    val fileService = FileService()

    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("srcId") srcId: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") type: String,
        @RequestParam("language") language: String?,
        @RequestParam("year") year: Int?,
        @RequestParam("title") title: String?,
        @RequestParam("journal") journal: String?,
        @RequestParam("volume") volume: String?,
        @RequestParam("issue") issue: String?,
        @RequestParam("chapterTitle") chapterTitle: String?,
        ): ReesponseDTO {
        return try{
            fileService.init()
            val path = fileService.save(file)
            println(path.toString())
            val contentType: String = if(file.contentType !== null) file.contentType!! else ""
            val fileName = "$srcId.pdf"
            val id = driveService.uploadSource(fileName, contentType,
                path, type, language, title, year, journal, volume, issue, chapterTitle )
            val location = FilePathMapper.createFilePath(srcId, type, language, year, title, journal, volume, issue, chapterTitle)

            fileService.deleteAll()
            return ReesponseDTO(driveId = id, location = location, message = "File: $path was uploaded with a drive id of  $id" )

        }catch(e: Exception){
            val message = "Could not upload the file: " + file.originalFilename + ". Error: " + e.message
            fileService.deleteAll()
            return ReesponseDTO(driveId = "", location = "", message = message )
        }
    }
}