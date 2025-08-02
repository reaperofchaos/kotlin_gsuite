package com.jomondb.gsuite.web.rest

import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.service.DriveService
import com.jomondb.gsuite.service.FileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/file")
class FileController (private val driveService: DriveService,
                       private val fileService: FileService) {

    /**
     * Downloads a file with a provided drive id.
     *
     * @param id string
     * @return {@link ByteArray}
     */
    @GetMapping("/{id}")
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

    /**
     * Returns info about a file with a provided name.
     * TODO: FIX THIS
     * @param name string
     *
     * @return list of {@link DownloadFile}
     */
    @GetMapping("/info/{name}")
    fun getInfoByName(@PathVariable name: String): List<DownloadFile>{
        return driveService.getFileInfo(name)
    }

    /**
     * Uploads a file to folder based on its drive id.
     * @param file {@link MultipartFile}
     * @param folderId string
     *
     * @return drive id of saved file
     */
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("folderId") folderId: String?): ResponseEntity<String>{
        println("Upload request - File: ${file.originalFilename}, FolderId: ${folderId}")
        return try{
            if (file.isEmpty) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty")
            }

            fileService.init()
            val path = fileService.save(file)
            println("File saved locally at: $path")

            val id = driveService.upload(file.originalFilename, file.contentType, path, folderId)
            fileService.deleteAll()
            ResponseEntity.status(HttpStatus.OK).body("File: ${file.originalFilename} was uploaded with a drive id of $id")
        }catch(e: Exception){
            println("Upload error: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            val message = "Could not upload the file: ${file.originalFilename}. Error: ${e.message}"
            ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message)
        }
    }
}