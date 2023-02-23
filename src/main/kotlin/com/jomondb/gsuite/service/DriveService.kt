package com.jomondb.gsuite.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.io.*
import java.security.GeneralSecurityException
import java.sql.Blob
import java.util.*

@Service
@RequiredArgsConstructor
class DriveService {
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
            .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8093).build()
        val credential: Credential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        //returns an authorized Credential object.
        return credential
    }
    @Throws(IOException::class)
    fun dnwnloadFile(id: String): ByteArrayOutputStream {
        val transport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        val service: Drive = Drive.Builder(transport, JSON_FACTORY, getCredentials(transport))
            .setApplicationName(APPLICATION_NAME)
            .build()
        try{
           val outputStream: OutputStream =  ByteArrayOutputStream()
            service.files().get(id)
                .executeMediaAndDownloadTo(outputStream)

            return outputStream as ByteArrayOutputStream
        }catch(e: GoogleJsonResponseException){
            System.err.println("Unable to move file: " + e.getDetails())
            throw e;
        }
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun list10Files(): String{
        try {
            val transport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            val service: Drive = Drive.Builder(transport, JSON_FACTORY, getCredentials(transport))
                .setApplicationName(APPLICATION_NAME)
                .build()
            val result: FileList = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute()
            val files: MutableList<File>? = result.getFiles();
            if (files == null || files.isEmpty()) {
                return "No files found"
            } else {
                val found: MutableList<String> = mutableListOf<String>()
                for (file in files) {
                    found.add("""${file.name} - ${file.id}""")
                }
                return found.joinToString("<br />")
            }
        }catch(e: Exception){
            return "Unable to authenticate"
        }
    }

}