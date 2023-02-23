package com.jomondb.gsuite.service

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

class FileService {
    private val root: Path = Paths.get("uploads")

    @Throws(IOException::class)
    fun init(){
        try{
            Files.createDirectories(root)
        }catch(e: Exception){
            throw IOException("Could not initialize folder for upload")
        }
    }

    fun save(file: MultipartFile): Path{
        try{
            Files.copy(file.inputStream, this.root.resolve(file.originalFilename))
            val filepath = Paths.get(root.toString(), file.originalFilename)

            return filepath;
        }catch(e: Exception){
           throw Exception(e.message)
        }
    }

    fun load(fileName: String): Resource{
        try{
            val file: Path = root.resolve(fileName);
            val resource: Resource = UrlResource(file.toUri())

            if(resource.exists() || resource.isReadable()){
                return resource
            }else{
                throw Exception("Could not read the file!")
            }
        }catch(e: Exception){
            throw Exception(e.message)
        }
    }

    fun deleteAll(){
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    fun loadAll(): Stream<Path> {
        try{
            return Files.walk(this.root, 1).filter { path: Path -> path != this.root }.map { other: Path? ->
                this.root.relativize(
                    other
                )
            }
        }catch(e: IOException){
            throw Exception(e.message)
        }
    }
}