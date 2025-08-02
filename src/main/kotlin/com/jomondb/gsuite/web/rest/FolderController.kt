package com.jomondb.gsuite.web.rest

import com.jomondb.gsuite.dataobject.FolderDTO
import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.service.DriveService
import com.jomondb.gsuite.service.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/folder")
class FolderController(private val driveService: DriveService,
                       private val fileService: FileService) {

    /**
     * Lists the folders at the root of the drive id.
     *
     * @return list of {@link DownloadFile}
     */
    @GetMapping("/folders")
    fun getFolders(): List<DownloadFile>{
        return driveService.getFolders()
    }


    /**
     * Endpoint that returns folder name and id for a provided folder name.
     * @param folderName string
     *
     * @return list of {@link DownloadFile}
     */
    @GetMapping("/info")
    fun getFolderInfo(@RequestParam("folderName") folderName: String,
                      @RequestParam("parentId") parentId: String): List<DownloadFile>
    {
        return driveService.getFolderInfo(folderName, parentId)
    }

    /**
     * Endpoint to create a folder with the provided
     * folder name inside the parent folder.
     *
     * @param folderName string
     * @parm parentId string
     *
     * @return ResponseEntity<String> drive id
     */
    @PostMapping("/create")
    fun createFolder(
        @RequestParam("folderName") folderName: String,
        @RequestParam("parentId") parentId: String): ResponseEntity<String>
    {
        val folder: FolderDTO = driveService.createFolderIfNotFound(folderName, parentId)
        val wasCreated: String = if(folder.found) "was found" else "was created"
        return ResponseEntity.status(HttpStatus.OK).body("Folder: $folderName $wasCreated with a drive id of  ${folder.id}")
    }

    /**
     * Lists the files in the folder with the provided id.
     * @param folderId string
     *
     * @return list of {@link DownloadFile}
     */
    @GetMapping("/files/{folderId}")
    fun getFilesInFolder(@PathVariable folderId: String): List<DownloadFile>{
        return driveService.getFolderContents(folderId)
    }



}