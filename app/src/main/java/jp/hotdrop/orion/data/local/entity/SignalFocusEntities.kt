package jp.hotdrop.orion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** ユーザーが気になった内容を記録したSignal。作成時刻は編集後も維持する。 */
@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)

/** 分析結果とユーザーのメモ。生成時刻には分析の開始時刻を使用する。 */
@Entity(tableName = "focuses")
data class FocusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val analysis: String,
    val note: String,
    val generatedAt: Long,
    val updatedAt: Long,
)

/** SignalのKeyword。正規化した値で重複を防ぎ、表示名と入力順を別に保持する。 */
@Entity(
    tableName = "signal_keywords",
    primaryKeys = ["signalId", "normalized"],
    foreignKeys = [
        ForeignKey(
            entity = SignalEntity::class,
            parentColumns = ["id"],
            childColumns = ["signalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("normalized")],
)
data class SignalKeywordEntity(
    val signalId: Long,
    val normalized: String,
    val display: String,
    val position: Int,
)

/** FocusのKeyword。正規化した値をSignalとの関連判定にも使用する。 */
@Entity(
    tableName = "focus_keywords",
    primaryKeys = ["focusId", "normalized"],
    foreignKeys = [
        ForeignKey(
            entity = FocusEntity::class,
            parentColumns = ["id"],
            childColumns = ["focusId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("normalized")],
)
data class FocusKeywordEntity(
    val focusId: Long,
    val normalized: String,
    val display: String,
    val position: Int,
)

/**
 * 再開可能な分析下書き。id = 1の一件だけを保存する。
 * プロンプトに開始時点のSignalを保持し、後から元の記録が編集されても変更しない。
 */
@Entity(tableName = "focus_draft")
data class FocusDraftEntity(
    @PrimaryKey val id: Int = 1,
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

/** 最後に確定した分析の対象上限。Focusが削除されても次回の分析範囲を維持する。 */
@Entity(tableName = "focus_checkpoint")
data class FocusCheckpointEntity(
    @PrimaryKey val id: Int = 1,
    val upperSignalId: Long,
    val startedAt: Long,
)
