package com.auramirage.app
data class Note(val id:Long,val title:String,val content:String,val folderId:Long?,val updatedAt:Long,val deletedAt:Long?=null)
data class Folder(val id:Long,val name:String,val parentId:Long?)
data class Document(val id:Long,val name:String,val mimeType:String,val uri:String?,val folderId:Long?,val createdAt:Long)
data class Revision(val id:Long,val noteId:Long,val title:String,val content:String,val createdAt:Long)
object NoteTitlePolicy { fun normalize(title:String,content:String)=title.trim().ifEmpty{content.trim().lineSequence().firstOrNull().orEmpty().take(60).ifEmpty{"Sem título"}} }
