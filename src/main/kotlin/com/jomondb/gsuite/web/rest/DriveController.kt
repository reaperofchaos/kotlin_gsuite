package com.jomondb.gsuite.web.rest

import com.jomondb.gsuite.dataobject.FolderDTO
import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.service.DriveService
import com.jomondb.gsuite.service.FileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
class SampleController {
    val driveService = DriveService()
    val fileService = FileService()

    @GetMapping("/")
    fun home(): String{
        return "Home"
    }

    @GetMapping("/test")
    fun test(): String{
        return driveService.list10Files()
    }

    @GetMapping("/folder/info")
    fun getFolderInfo(@RequestParam("folderName") folderName: String,
                      @RequestParam("parentId") parentId: String): List<DownloadFile>
    {
        return driveService.getFolderInfo(folderName, parentId)
    }

    @PostMapping("/folder/create")
    fun createFolder(
        @RequestParam("folderName") folderName: String,
        @RequestParam("parentId") parentId: String): ResponseEntity<String>
    {
        val folder: FolderDTO = driveService.createFolderIfNotFound(folderName, parentId)
        val wasCreated: String = if(folder.found) "was found" else "was created"
        return ResponseEntity.status(HttpStatus.OK).body("Folder: $folderName $wasCreated with a drive id of  ${folder.id}")
    }


    @GetMapping("/folders")
    fun getFolders(): List<DownloadFile>{
        return driveService.getFolders()
    }

    @GetMapping("/info/{name}")
    fun getInfoByName(@PathVariable name: String): List<DownloadFile>{
        return driveService.getFileInfo(name)
    }

    @GetMapping("/info/files/{folderId}")
    fun getFilesInFolder(@PathVariable folderId: String): List<DownloadFile>{
        return driveService.getFolderContents(folderId)
    }

    @GetMapping("/download/{id}")
    fun download(@PathVariable id: String): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        val file = driveService.downloadFile(id)
        val filename = file.name
        val mimeType = file.mimeType;
        headers.contentType = MediaType.parseMediaType(mimeType)
        headers.setContentDispositionFormData(filename, filename)
        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"
        val contents = file.content.toByteArray()
        return ResponseEntity(contents, headers, HttpStatus.OK)
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("folderId") folderId: String?): ResponseEntity<String>{

        return try{
            fileService.init()
            val path = fileService.save(file)
            val id = driveService.upload(file.originalFilename, file.contentType, path, folderId)
            fileService.deleteAll()
            return ResponseEntity.status(HttpStatus.OK).body("File: $path was uploaded with a drive id of  $id")
        }catch(e: Exception){
            val message = "Could not upload the file: " + file.originalFilename + ". Error: " + e.message
            ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message)
        }
    }

}