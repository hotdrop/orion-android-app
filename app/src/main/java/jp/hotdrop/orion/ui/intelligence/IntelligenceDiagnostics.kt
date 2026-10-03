package jp.hotdrop.orion.ui.intelligence

import java.util.logging.Level
import java.util.logging.Logger

/**
 * ユーザー向けのエラー表示とは分けて、調査用の例外を記録する。
 */
internal fun reportIntelligenceFailure(error: Exception) {
    Logger.getLogger("PersonalIntelligence")
        .log(Level.WARNING, "Personal Intelligence operation failed", error)
}
