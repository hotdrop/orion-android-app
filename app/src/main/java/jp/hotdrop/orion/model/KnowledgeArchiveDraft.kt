package jp.hotdrop.orion.model

data class KnowledgeArchiveDraft(
    val title: String,
    val url: String,
    val memo: String,
) {
    fun normalized(): KnowledgeArchiveDraft = copy(
        title = title.trim(),
        url = url.trim(),
        memo = memo.trim(),
    )
}

