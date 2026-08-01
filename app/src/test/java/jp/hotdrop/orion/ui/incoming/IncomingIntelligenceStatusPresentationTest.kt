package jp.hotdrop.orion.ui.incoming

import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class IncomingIntelligenceStatusPresentationTest {
    @Test
    fun driveNotConfigured_takesPriority() {
        val status = IncomingIntelligenceUiState(
            isSyncing = true,
            isOffline = true,
            errorMessage = "error",
        ).toStatusPresentation()

        assertEquals("UPLINK // STANDBY", status.code)
        assertEquals(IncomingIntelligenceStatusTone.Warning, status.tone)
    }

    @Test
    fun syncing_takesPriorityOverErrorAndOffline() {
        val status = IncomingIntelligenceUiState(
            isDriveConfigured = true,
            isSyncing = true,
            isOffline = true,
            errorMessage = "error",
        ).toStatusPresentation()

        assertEquals("UPLINK // RECEIVING", status.code)
        assertEquals(IncomingIntelligenceStatusTone.Normal, status.tone)
    }

    @Test
    fun error_takesPriorityOverOfflineAndKeepsMessage() {
        val status = IncomingIntelligenceUiState(
            isDriveConfigured = true,
            isOffline = true,
            errorMessage = "認証エラー",
        ).toStatusPresentation()

        assertEquals("UPLINK // ERROR", status.code)
        assertEquals("認証エラー", status.description)
        assertEquals(IncomingIntelligenceStatusTone.Error, status.tone)
    }

    @Test
    fun offline_usesCachedStatus() {
        val status = IncomingIntelligenceUiState(
            isDriveConfigured = true,
            isOffline = true,
        ).toStatusPresentation()

        assertEquals("UPLINK // OFFLINE CACHE", status.code)
        assertEquals(IncomingIntelligenceStatusTone.Warning, status.tone)
    }

    @Test
    fun connectedIdle_isReady() {
        val status = IncomingIntelligenceUiState(
            isDriveConfigured = true,
        ).toStatusPresentation()

        assertEquals("UPLINK // READY", status.code)
        assertEquals(IncomingIntelligenceStatusTone.Normal, status.tone)
    }
}
