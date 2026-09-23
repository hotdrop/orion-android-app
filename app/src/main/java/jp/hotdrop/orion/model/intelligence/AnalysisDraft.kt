package jp.hotdrop.orion.model.intelligence

/**
 * 再開可能な分析下書き。プロンプト、対象範囲、開始時刻は作成時点で固定する。
 * stageは0がプロンプト表示、1が結果入力。本文・メモ・Keywordとともに保存する。
 */
data class AnalysisDraft(
    val token: String,
    val startedAt: Long,
    val upperSignalId: Long,
    val signalCount: Int,
    val prompt: String,
    val analysis: String = "",
    val note: String = "",
    val keywords: String = "",
    val stage: Int = 0,
)
