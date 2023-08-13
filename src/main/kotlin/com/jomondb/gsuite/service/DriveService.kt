package com.jomondb.gsuite.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.jomondb.gsuite.dataobject.FileDTO
import com.jomondb.gsuite.dataobject.FolderDTO
import com.jomondb.gsuite.domain.DownloadFile
import com.jomondb.gsuite.mapper.GsuiteFileMapper
import com.jomondb.gsuite.utils.constants.ARTICLE_FOLDER
import com.jomondb.gsuite.utils.constants.BOOK_FOLDER
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.io.*
import java.nio.file.Path
import java.security.GeneralSecurityException
import java.util.*


@Service
@RequiredArgsConstructor
class DriveService {
    val mapper: GsuiteFileMapper = GsuiteFileMapper()
    /**
     * Application name
     */
    val APPLICATION_NAME = "Google Drive API Java Quickstart"

    /**
     * JSON Factory
     */
    val JSON_FACTORY: GsonFactory? = GsonFactory.getDefaultInstance()
    /**
     * Directory to store authorization tokens for this application.
     */
    val TOKENS_DIRECTORY_PATH = "tokens"
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    val SCOPES: List<String> = Collections.singletonList(DriveScopes.DRIVE)
    val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val input: InputStream = DriveService::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(input))
        // Build flow and trigger user authorization request.
        // Build flow and trigger user authorization request.
        val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8093).build()
        //returns an authorized Credential object.
        return  AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Function to initialize drive service
     */
    fun initializeService(): Drive {
        val transport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        return Drive.Builder(transport, JSON_FACTORY, getCredentials(transport))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Lists all files and folders inside a folder
     */
    @Throws(IOException::class)
    fun getFolderContents(id: String): List<DownloadFile>{
        val service = initializeService()
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
            var pageToken: String? = null
            do {
                val result: FileList = service.files().list()
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
     * Lists up to the first 100 top level folders
     */
    fun getFolders(): List<DownloadFile>{
        val service: Drive = initializeService()
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
                val result: FileList = service.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and parents in 'root' ")
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
     * Lists file info for files with a given name
     */
    @Throws(IOException::class)
    fun getFileInfo(name: String): List<DownloadFile>{
        val service: Drive = initializeService()
        try{
            val files: MutableList<DownloadFile> = mutableListOf()
            var pageToken: String? = null
            do {
                val result: FileList = service.files().list()
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
            System.err.println("Unable to move file: " + e.details)
            throw e
        }
    }

    @Throws(IOException::class)
    fun getFolderInfo(name: String, parentFolderId: String): List<DownloadFile>{
        val service: Drive = initializeService()
        val folderMimeType = "application/vnd.google-apps.folder"
        println("name " + name)
        println("folder id " + parentFolderId)
        val files: MutableList<DownloadFile> = mutableListOf()
        try{
            var pageToken: String? = null
            do {
                val result: FileList = service.files().list()
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

    @Throws(IOException::class)
    fun createFolder(folderName: String, parentFolderId: String): String{
        val service: Drive = initializeService()
        val fileMetadata = File()
        fileMetadata.name = folderName
        fileMetadata.mimeType = "application/vnd.google-apps.folder"
        fileMetadata.parents = Collections.singletonList(parentFolderId)

        try {
            val file = service.files().create(fileMetadata)
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

    @Throws(IOException::class)
    fun downloadFile(id: String): FileDTO {
        val service: Drive = initializeService()
        try{
           val outputStream: OutputStream =  ByteArrayOutputStream()
            val request = service.files().get(id)
            request.executeMediaAndDownloadTo(outputStream)
            val file = request.execute()
            return mapper.buildFileDTO(outputStream as ByteArrayOutputStream, file)
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to move file: " + e.details)
            throw e
        }
    }

    @Throws(IOException::class)
    fun upload(name: String?, mimeType: String?, path: Path, folderId: String?): String{
        val service: Drive = initializeService()
        val fileMetadata = File()
        println("Uploading $name type: $mimeType path: $path")
        fileMetadata.name = name
        if(folderId !== null){
            fileMetadata.parents = Collections.singletonList(folderId)
        }
        val filePath: java.io.File = File(path.toString())
        val mediaContent = FileContent(mimeType, filePath)
        try {

            val file: File = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            println("The file was successfully uploaded with an id of " + file.id)
            return file.id
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to upload file: " + e.details)
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


    @Throws(IOException::class, GeneralSecurityException::class)
    fun list10Files(): String{
        try {
            val service: Drive = initializeService()
            val result: FileList = service.files().list()
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