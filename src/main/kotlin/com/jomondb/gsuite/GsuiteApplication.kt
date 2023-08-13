package com.jomondb.gsuite

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class GsuiteApplication

fun main(args: Array<String>) {
	runApplication<GsuiteApplication>(*args)
}
