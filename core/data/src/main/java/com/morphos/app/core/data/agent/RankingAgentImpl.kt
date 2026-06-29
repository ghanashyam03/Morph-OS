package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.RankingAgent
import com.morphos.app.core.domain.model.ContentCandidate
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.RankedContent
import javax.inject.Inject

class RankingAgentImpl @Inject constructor() : RankingAgent {
    override fun rankContent(
        candidates: List<ContentCandidate>,
        context: ContextSnapshot,
        memoryProfile: MemoryProfile
    ): List<RankedContent> {
        TODO("Not yet implemented")
    }
}
