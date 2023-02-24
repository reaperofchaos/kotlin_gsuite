package com.jomondb.gsuite.web.rest

import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.service.DriveService
import com.jomondb.gsuite.service.FileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.function.RequestPredicates.contentType


@RestController
class SampleController {
    val driveService = DriveService();
    val fileService = FileService();

    @GetMapping("/")
    fun home(): String{
        return "Home"
    }
    @GetMapping("/test")
    fun test(): String{
        return driveService.list10Files();
    }


    @GetMapping("/folders")
    fun getFolders(): List<DownloadFile>{
        val files: List<DownloadFile> = driveService.getFolders()
        return files
    }

    @GetMapping("/info/{name}")
    fun getInfoByName(@PathVariable name: String): List<DownloadFile>{
        val files: List<DownloadFile> = driveService.getFileInfo(name)
        return files
    }

    @GetMapping("/info/files/{folderId}")
    fun getFilesInFolder(@PathVariable folderId: String): List<DownloadFile>{
        val files: List<DownloadFile> = driveService.getFolderContents(folderId)
        return files
    }

    @GetMapping("/download/{id}")
    fun download(@PathVariable id: String): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_PDF);
        val filename = "output.pdf"
        headers.setContentDispositionFormData(filename, filename)
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        val contents = driveService.dnwnloadFile(id).toByteArray()
        return ResponseEntity(contents, headers, HttpStatus.OK)
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String>{
        var message: String = "";
        System.out.println("File name " + file.originalFilename)
        System.out.println("File type " + file.contentType)

        try{
            fileService.init();
            val path = fileService.save(file);
            driveService.upload(file.originalFilename, file.contentType, path)
            val id = fileService.deleteAll();
            return ResponseEntity.status(HttpStatus.OK).body("File: " + path.toString() + " was uploaded with a drive id of  " + id);
        }catch(e: Exception){
            message = "Could not upload the file: " + file.originalFilename + ". Error: " + e.message;
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

}