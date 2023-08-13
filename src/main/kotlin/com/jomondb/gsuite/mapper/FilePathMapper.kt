package com.jomondb.gsuite.mapper

import com.google.api.services.drive.model.File
import com.jomondb.gsuite.domain.DownloadFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

class FilePathMapper {
    companion object {
        fun createFilePath(
            srcId: String,
            type: String,
            language: String?,
            year: Int?,
            title: String?,
            journal: String?,
            volume: String?,
            issue: String?,
            chapterTitle: String?
        ): String {
            var filePath = "Pdfs/"
            println("type $type")
            if (type == "a") {
                filePath += "Articles/"

                if (journal !== null) {
                    println("journal $journal")

                    filePath += "$journal/"
                    if (year !== null) {
                        println("year $year")

                        filePath += "$year/"
                        if (volume !== null) {
                            println("volume $volume")

                            filePath += "$volume/"
                            if (issue !== null) {
                                println("issue $issue")

                                filePath += "$issue/"
                            }
                        }
                    }
                }
            } else if (type === "b" || type === "c") {
                filePath += "Books/"
                if (language !== null) {
                    println("language $language")

                    filePath += "$language/"
                } else {
                    filePath += "English/"
                }
                if (title != null) {
                    println("title $title")

                    filePath += "$title/"
                    if (type !== "b" && chapterTitle !== null) {
                        println("chapterTitle $chapterTitle")

                        filePath += "$chapterTitle/"
                    }
                }
            }

            filePath += "$srcId.pdf"

            return filePath;
        }
    }
}