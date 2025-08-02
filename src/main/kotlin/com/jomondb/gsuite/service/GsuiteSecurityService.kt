package com.jomondb.gsuite.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections

/* Handles authentication to Google Drive API*/
@Service
class GsuiteSecurityService {
    /**
     * Application name
     */
    val APPLICATION_NAME = "Google Drive Service"

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
}