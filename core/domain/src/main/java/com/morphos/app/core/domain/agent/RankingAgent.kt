package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.ContentCandidate
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.RankedContent

interface RankingAgent {
    fun rankContent(
        candidates: List<ContentCandidate>,
        context: ContextSnapshot,
        memoryProfile: MemoryProfile
    ): List<RankedContent>
}
