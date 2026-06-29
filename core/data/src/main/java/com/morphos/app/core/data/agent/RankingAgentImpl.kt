package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.RankingAgent
import com.morphos.app.core.domain.model.*
import javax.inject.Inject
import kotlin.math.min

class RankingAgentImpl @Inject constructor() : RankingAgent {

    override fun rankContent(
        candidates: List<ContentCandidate>,
        context: ContextSnapshot,
        profile: MemoryProfile
    ): List<RankedContent> {
        val now = System.currentTimeMillis()
        val weights = PriorityWeights() // Use defaults or customize based on profile later

        return candidates
            .map { candidate ->
                // Recency score decays over hours
                val diffHours = (now - candidate.recency).toFloat() / 3600000f
                val recencyScore = 1.0f / (1.0f + diffHours)

                // Engagement normalized to 0-1
                val engagementScore = min(1.0f, candidate.engagementCount / 10f)

                // Sum components
                val score = (recencyScore * weights.recencyWeight) +
                        (engagementScore * weights.engagementWeight) +
                        (candidate.contextMatchScore * weights.contextRelevanceWeight)

                RankedContent(
                    contentId = candidate.contentId,
                    score = score,
                    metadata = candidate.metadata
                )
            }
            .sortedByDescending { it.score }
    }
}
