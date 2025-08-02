package com.jomondb.gsuite.service


import com.jomondb.gsuite.mapper.GsuiteFileMapper
import com.jomondb.gsuite.utils.constants.SERVICE_TEMP_FOLDER
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

/** Service to handle temporary storage of file in service. */
@Service
class FileService() {
    private val root: Path = Paths.get(SERVICE_TEMP_FOLDER)

    /**
     * Creates an upload folder in service.
     */
    @Throws(IOException::class)
    fun init(){
        try{
            Files.createDirectories(root)
        }catch(e: Exception){
            throw IOException("Could not initialize folder for upload")
        }
    }

    /**
     * Saves the file to the service's upload folder.
     */
    fun save(file: MultipartFile): Path{
        try{
            file.originalFilename?.let { this.root.resolve(it) }?.let { Files.copy(file.inputStream, it) }
            return Paths.get(root.toString(), file.originalFilename)
        }catch(e: Exception){
           throw Exception(e.message)
        }
    }

    /**
     * Function to get the file from the service upload folder.
     *
     * @param fileName String
     * @return {@link Resource}
     */
    fun load(fileName: String): Resource{
        try{
            val file: Path = root.resolve(fileName)
            val resource: Resource = UrlResource(file.toUri())

            if(resource.exists() || resource.isReadable){
                return resource
            }else{
                throw Exception("Could not read the file!")
            }
        }catch(e: Exception){
            throw Exception(e.message)
        }
    }

    /**
     * Deletes all files in the temporary upload folder.
     */
    fun deleteAll(){
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    /**
     * Retrieves the paths of all files in the temporary upload folder.
     *
     * @return {@link Stream} of {@link Path}
     */
    fun loadAll(): Stream<Path> {
        try{
            return Files.walk(this.root, 1).filter { path: Path -> path != this.root }.map { other: Path? ->
                other?.let {
                    this.root.relativize(
                        it
                    )
                }
            }
        }catch(e: IOException){
            throw Exception(e.message)
        }
    }
}