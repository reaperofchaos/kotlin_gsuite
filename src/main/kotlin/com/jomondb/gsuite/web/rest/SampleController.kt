package com.jomondb.gsuite.web.rest
import com.jomondb.gsuite.service.DriveService
import org.springframework.web.bind.annotation.GetMapping
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

}