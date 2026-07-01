package com.morphos.app.core.data.di

import com.morphos.app.core.data.agent.*
import com.morphos.app.core.domain.agent.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentModule {

    @Binds
    @Singleton
    abstract fun bindContextAgent(impl: ContextAgentImpl): ContextAgent

    @Binds
    @Singleton
    abstract fun bindIntentAgent(impl: IntentAgentImpl): IntentAgent

    @Binds
    @Singleton
    abstract fun bindPlanningAgent(impl: PlanningAgentImpl): PlanningAgent

    @Binds
    @Singleton
    abstract fun bindMemoryAgent(impl: MemoryAgentImpl): MemoryAgent

    @Binds
    @Singleton
    abstract fun bindRankingAgent(impl: RankingAgentImpl): RankingAgent

    @Binds
    @Singleton
    abstract fun bindNotificationAgent(impl: NotificationAgentImpl): NotificationAgent

    @Binds
    @Singleton
    abstract fun bindAgentOrchestrator(impl: AgentOrchestratorImpl): AgentOrchestrator
}
