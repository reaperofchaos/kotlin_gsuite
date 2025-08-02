package com.jomondb.gsuite.service

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.jomondb.gsuite.dataobject.FileDTO
import com.jomondb.gsuite.dataobject.FolderDTO
import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.mapper.GsuiteFileMapper
import com.jomondb.gsuite.utils.constants.ARTICLE_FOLDER
import com.jomondb.gsuite.utils.constants.BOOK_FOLDER
import com.jomondb.gsuite.utils.constants.FOLDER_MIME_TYPE
import com.jomondb.gsuite.web.rest.DriveController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.*
import java.nio.file.Path
import java.security.GeneralSecurityException
import java.util.*


/* Handles folder navigation of google drive. */
@Service
class DriveService(private val gSuiteSecurityService: GsuiteSecurityService) {
    val mapper: GsuiteFileMapper = GsuiteFileMapper()
    private val securityService by lazy { gSuiteSecurityService.initializeService() }

    private
    val logger: Logger? = LoggerFactory.getLogger(DriveService::class.java)

    /**
     * Lists all files and folders inside a folder based on its id.
     *
     * @param id folder id
     * @return list of {@link DownloadFile}
     */
    @Throws(IOException::class)
    fun getFolderContents(id: String): List<DownloadFile>{
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
            var pageToken: String? = null
            do {
                val result: FileList = securityService.files().list()
                    .setQ("'$id' in parents")
                    .setPageToken(pageToken)
                    .execute()
                for (file: File in result.files) {
                    files.add(mapper.fileToDownloadFile(file))
                }
                pageToken = result.nextPageToken
            }while(pageToken !== null)
            return files
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to move file: " + e.details)
            throw e
        }
    }

    /**
     * Lists up to the first 100 top level folders.
     *
     * @return list of {@link DownloadFile}
     */
    fun getFolders(): List<DownloadFile>{
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
                val result: FileList = securityService.files().list()
                    .setQ("mimeType = '$FOLDER_MIME_TYPE' and parents in 'root' ")
                    .setSpaces("drive")
                    .setPageSize(100)
                    .execute()
                for (file: File in result.files) {
                    files.add(mapper.fileToDownloadFile(file))
                }

            return files
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to move file: " + e.details)
            throw e
        }
    }

    /**
     * Lists file info for files with a given name.
     *
     * @param name file name
     * @return a list of {@link DownloadFile}
     */
    @Throws(IOException::class)
    fun getFileInfo(name: String): List<DownloadFile>{
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
            var pageToken: String? = null
            do {
                val result: FileList = securityService.files().list()
                    .setQ("""name='${name}'""")
                    .setSpaces("drive")
                    .setPageToken(pageToken)
                    .setPageSize(10)
                    .execute()
                for (file: File in result.files) {
                    files.add(mapper.fileToDownloadFile(file))
                }
                pageToken = result.nextPageToken
            }while(pageToken != null)

            return files
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to find file: " + e.details)
            throw e
        }
    }

    /**
     * Gets a folder info based on the name and parent folder id.
     *
     * @param name string
     * @param parentFolderId string
     * @return List of {@Link DownloadFile}
     */
    @Throws(IOException::class)
    fun getFolderInfo(name: String, parentFolderId: String): List<DownloadFile>{
        val folderMimeType = FOLDER_MIME_TYPE
        println("name " + name)
        println("folder id " + parentFolderId)
        val files: MutableList<DownloadFile> = mutableListOf()
        try{
            var pageToken: String? = null
            do {
                val result: FileList = securityService.files().list()
                    .setQ("""name='${name}' and mimeType='${folderMimeType}' and parents in '${parentFolderId}'""")
                    .setSpaces("drive")
                    .setPageToken(pageToken)
                    .setPageSize(10)
                    .execute()
                for (file: File in result.files) {
                    files.add(mapper.fileToDownloadFile(file))
                }
                pageToken = result.nextPageToken
            }while(pageToken != null)

        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to find file: " + e.details)
            return files
        }catch(e: Exception){
            System.err.println("error " + e.message)
            return files
        }
        return files

    }

    /**
     * Function to check if a folder with a given name exists in another folder.
     * The folder id is returned if present.
     *
     * @param folderName string
     * @param parentFolderId string
     * @return Boolean
     */
    @Throws(IOException::class)
    fun checkIfFolderExists(folderName: String, parentFolderId: String): String?{
        val found: List<DownloadFile> = getFolderInfo(folderName, parentFolderId)
        if(found.isNotEmpty()) {
            println("folder exists" + found[0].id)
        }else{
            println("folder not found")
        }
        return if(found.isNotEmpty()) found[0].id  else null
    }

    /**
     * Method to create a folder in a provided drive id.
     *
     * @param folderName string
     * @param parentFolderId string
     * @return folder's drive id
     */
    @Throws(IOException::class)
    fun createFolder(folderName: String, parentFolderId: String): String{
        val fileMetadata = File()
        fileMetadata.name = folderName
        fileMetadata.mimeType = FOLDER_MIME_TYPE
        fileMetadata.parents = Collections.singletonList(parentFolderId)

        try {
            val file = securityService.files().create(fileMetadata)
                .setFields("id, parents")
                .execute()
            println("Folder ID: " + file.id)
            return file.id
        } catch (e: GoogleJsonResponseException) {
            System.err.println("Unable to create folder: " + e.details)
            throw e
        }
    }

    @Throws(IOException::class)
    fun createFolderIfNotFound(folderName: String, parentFolderId: String): FolderDTO {
        val found = checkIfFolderExists(folderName, parentFolderId)
        println("F")
        println("folder name " + folderName)
        println("parent folder id " + parentFolderId)
        return if(found !== null){
            FolderDTO(found = true, id = found)
        }else{
            val id = createFolder(folderName, parentFolderId)
            FolderDTO(false, id)
        }
    }



    /**
     * Method to upload a file.
     *
     * @param name file name
     * @param mimeType file mime type
     * @param path file path
     * @param folderId target folder drive id
     * @return drive id of saved file
     */
    @Throws(IOException::class)
    fun upload(name: String?, mimeType: String?, path: Path, folderId: String?): String{
        logger?.info("name $name")
        logger?.info("mimeType ${mimeType}")
        logger?.info("path ${path}")
        logger?.info("folderId ${folderId}")

        val fileMetadata = File()
        fileMetadata.name = name
        if(folderId != null){
            fileMetadata.parents = Collections.singletonList(folderId)
        }

        val filePath = java.io.File(path.toString())
        if (!filePath.exists()) {
            throw IOException("File does not exist: ${path}")
        }

        val mediaContent = FileContent(mimeType, filePath)
        try {
//            val file: File = securityService.files().create(
//                fileMetadata,
//                mediaContent
//            )
//                .setFields("id")
//                .execute()
//            println("The file was successfully uploaded with an id of " + file.id)
            return "test"
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to upload file: " + e.details)
            throw e
        }
    }


    /**
     * Function to download a file based on a provided drive id.
     *
     * @param id string
     * @return {@link FileDTO}
     */
    @Throws(IOException::class)
    fun downloadFile(id: String): FileDTO {
        try{
            val outputStream: OutputStream =  ByteArrayOutputStream()
            val request = securityService.files().get(id)
            request.executeMediaAndDownloadTo(outputStream)
            val file = request.execute()
            return mapper.buildFileDTO(outputStream as ByteArrayOutputStream, file)
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to move file: " + e.details)
            throw e
        }
    }

    @Throws(IOException::class)
    fun uploadSource(fileName: String, mimeType: String, path: Path,
                     type: String, language: String?, title: String?, year: Int?,
                     journal: String?, volume: String?, issue: String?, chapterTitle: String?): String? {
        println("type " + type);

        if(type == "a"){
            return uploadArticle(fileName, mimeType, path, year, journal, volume, issue)
        }
        if(type == "b"){
            return uploadBook(fileName, mimeType, path, language, title)
        }
        if(type == "c"){
            return uploadChapter(fileName, mimeType, path, language, title, chapterTitle)
        }
        return null
    }

    /**
     * Uploads a file to the appropriate folder building them if needed
     */
    @Throws(IOException::class)
    fun uploadArticle(fileName: String, mimeType: String, path: Path, year: Int?,
                     journal: String?, volume: String?, issue: String?): String? {
        println("article folder " + ARTICLE_FOLDER);
        println("Path " + path.toString())
        println("journal " +  journal)
        if(journal !== null)
        {
          val foundJournal = createFolderIfNotFound(journal, ARTICLE_FOLDER)
            val journalId = foundJournal.id
            println("journal id " + journalId)
            if(year !== null && journalId !== null){
                val yearId = createFolderIfNotFound(year.toString(), journalId).id
                println("year id " + yearId)

                if(volume !== null){
                    val volumeId = createFolderIfNotFound(volume, yearId).id
                    println("volume id " + volumeId)

                    if(issue !== null){
                        val issueId = createFolderIfNotFound(issue, volumeId).id
                        println("issueId id " + issueId)

                        return upload(fileName, mimeType, path, issueId)
                    }else{
                        return upload(fileName, mimeType, path, volumeId)
                    }
                }
            }
        }

        return null
    }

    @Throws(IOException::class)
    fun uploadBook(fileName: String, mimeType: String, path: Path,
                   language: String?, title: String?): String? {

        //check if title exists in books
        if(title !== null)
        {
            val languageId: String = if(language !== null) {
                createFolderIfNotFound(language, BOOK_FOLDER).id
            }else{
                createFolderIfNotFound("English", BOOK_FOLDER).id
            }

            val titleId = createFolderIfNotFound(title, languageId).id
            return upload(fileName, mimeType, path, titleId)
        }

        return null
    }

    @Throws(IOException::class)
    fun uploadChapter(fileName: String, mimeType: String, path: Path,
                   language: String?, title: String?, chapterTitle: String?): String? {

        //check if title exists in books
        if(title !== null && chapterTitle !== null)
        {
            val languageId: String = if(language !== null) {
                createFolderIfNotFound(language, BOOK_FOLDER).id
            }else{
                createFolderIfNotFound("English", BOOK_FOLDER).id
            }

            val titleId = createFolderIfNotFound(title, languageId).id
            val chapterTitleId = createFolderIfNotFound(chapterTitle, titleId).id
            return upload(fileName, mimeType, path, chapterTitleId)
        }

        return null
    }


    /**
     * Method that lists the names of 10 files in the drive id root folder.
     *
     * @return String.
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun list10Files(): String{
        try {
            val result: FileList = securityService.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute()
            val files: MutableList<File>? = result.files
            return if (files == null || files.isEmpty()) {
                "No files found"
            } else {
                val found: MutableList<String> = mutableListOf()
                for (file in files) {
                    found.add("""${file.name} - ${file.id}""")
                }
                found.joinToString("<br />")
            }
        }catch(e: Exception){
            return "Unable to authenticate"
        }
    }

}