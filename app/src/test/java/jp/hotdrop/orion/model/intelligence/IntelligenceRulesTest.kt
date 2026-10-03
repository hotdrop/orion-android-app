package jp.hotdrop.orion.model.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligenceRulesTest {
    @Test
    fun normalizationPreservesPhraseAndDeduplicatesEquivalentForms() {
        val keywords = parseKeywords("  Ｓｍａｌｌ　ＬＬＭ  \nsmall   llm\nRLCD\n\n")
        assertEquals(listOf("small llm", "rlcd"), keywords.map { it.normalized })
        assertEquals("Ｓｍａｌｌ　ＬＬＭ", keywords.first().display)
        assertEquals("rlhf / rlcd", normalizeKeyword(" RLHF / RLCD "))
    }

    @Test
    fun blankBodyRejected() {
        assertThrows(IllegalArgumentException::class.java) { requireRecordBody(" \n\t") }
        requireRecordBody("興味のあること")
    }

    @Test
    fun promptContainsEverySourceAndExploratoryInstructions() {
        val signals = listOf(
            SignalRecord(
                id = 7,
                body = "判定器としてのLLM",
                createdAt = 1,
                updatedAt = 1,
                keywords = listOf("Small LLM", "Jev"),
            ),
            SignalRecord(
                id = 8,
                body = "関連が気になる",
                createdAt = 2,
                updatedAt = 2,
                keywords = emptyList(),
            ),
        )
        val prompt = buildFocusPrompt(signals)
        val expectedFragments = listOf(
            "Signal 7",
            "判定器としてのLLM",
            "Small LLM / Jev",
            "Signal 8",
            "Keywords: なし",
            "すべてのSignalをFocusにする必要はありません",
            "まだ分かっていないこと",
            "TODOではありません",
        )
        expectedFragments.forEach { fragment ->
            assertTrue(fragment, prompt.contains(fragment))
        }
    }

    @Test
    fun extractsKnownHeadingsEmphasisAndEnglishTermsWithoutDuplicates() {
        val candidates = extractKeywordCandidates(
            analysis = "RLCDへの関心\n## 選択型推論\n**Jev** と Small LLM\nJev",
            knownKeywords = listOf("RLCD", "rlcd"),
        )
        assertTrue(candidates.contains("選択型推論"))
        assertTrue(candidates.contains("Jev"))
        assertTrue(candidates.contains("Small LLM"))
        assertEquals(candidates.size, candidates.map(::normalizeKeyword).distinct().size)
    }

    @Test
    fun candidatesAreBoundedAndEmptyInputIsSafe() {
        assertTrue(extractKeywordCandidates("", listOf("RLCD")).isEmpty())
        val analysis = (1..40).joinToString("\n") { "## Theme$it" }
        val candidates = extractKeywordCandidates(analysis, emptyList())
        assertEquals(20, candidates.size)
    }

    @Test
    fun englishCandidatesExcludeConnectingProse() {
        val candidates = extractKeywordCandidates(
            "RLCD and Small LLM connect evaluation with selection.", listOf("RLCD"),
        )
        assertEquals(listOf("RLCD", "Small LLM"), candidates)
    }

    @Test
    fun keywordSelectionDoesNotAddLeadingBlankOrDuplicates() {
        assertEquals("RLCD", appendKeyword("", "RLCD"))
        assertEquals("RLCD\nSmall LLM", appendKeyword("RLCD\n", "Small LLM"))
        assertEquals("RLCD", appendKeyword("RLCD", "ｒｌｃｄ"))
    }
}
