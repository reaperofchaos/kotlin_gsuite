package com.jomondb.gsuite.web.rest

import com.jomondb.gsuite.service.DriveService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DriveController(private val driveService: DriveService) {


    /**
     * Endpoint that returns a string advising
     * this is the Google Drive service.
     * @return String
     */
    @GetMapping("/")
    fun home(): String{
        return "This is the Google Drive Service"
    }

    /**
     * Randomly grabs 10 file names from Google Drive.
     * Used to verify drive service is connecting to the
     * drive.
     *
     * @return String
     */
    @GetMapping("/test")
    fun test(): String{
        return driveService.list10Files()
    }
}