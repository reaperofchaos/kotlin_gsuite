package com.jomondb.gsuite.dataobject

data class SourceDTO (
    val id: Integer,
    val authors: ArrayList<Integer>,
    val sourceType: String,
    val year: Integer,
    val title: String,
    val journal: String,
    val volume: Integer,
    val issue: Integer,
    val citation: String,
    val srcLocation: String,
    val webSite: String,
    val doi : String,
)