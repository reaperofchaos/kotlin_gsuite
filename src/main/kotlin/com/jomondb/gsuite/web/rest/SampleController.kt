package com.jomondb.gsuite.web.rest
import com.jomondb.gsuite.service.DriveService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    val driveService = DriveService();
    @GetMapping("/")
    fun home(): String{
        return "Home"
    }
    @GetMapping("/test")
    fun test(): String{
        return driveService.list10Files();
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
}