package jp.hotdrop.orion.model

data class KnowledgeArchiveEntry(
    val id: Long,
    val title: String,
    val url: String,
    val memo: String,
    val createdAt: Long,
    val updatedAt: Long,
)