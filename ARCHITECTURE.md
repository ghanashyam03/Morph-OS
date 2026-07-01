# MorphOS — Core Engineering Architecture

This document specifies the technical design, data flows, and architectural constraints of MorphOS.

## 1. System Topology & Data Flow

```
+--------------------+      +--------------------+      +--------------------+
|  ContextRefresh    |      |  User Input (NL)   |      |  System Event      |
|  (Time/Loc/Batt)   |      |  (Widget Creator)  |      |  (Notification)    |
+---------+----------+      +---------+----------+      +---------+----------+
          |                           |                           |
          v                           v                           v
+------------------+        +------------------+        +------------------+
|   ContextAgent   |        |   IntentAgent    |        |NotificationAgent |
+---------+----------+        +---------+----------+        +---------+----------+
          |                           |                           |
          v                           v                           v
   [ContextSnapshot]           [WidgetIntent]           [PrioritizedNotification]
          |                           |                           |
          +-------------+-------------+---------------------------+
                        |
                        v
              +-------------------+
              |  AgentOrchestrator| <=====> [MemoryAgent] (nightly summarization)
              +---------+---------+
                        |
                        v (requires WidgetPlan creation)
              +-------------------+
              |   PlanningAgent   | <=====> [AIEngineManager] (local/cloud models)
              +---------+---------+
                        |
                        v [WidgetConfig / SlotConfig]
              +-------------------+
              |   Widget Engine   | <=====> [Data Plugins] (Calendar, Weather, etc.)
              +---------+---------+
                        |
                        v [Content Candidates]
              +-------------------+
              |   RankingAgent    | (deterministic prioritization)
              +---------+---------+
                        |
                        v [Ranked Content]
              +-------------------+
              | Glance Renderer   | ====> Launcher (AppWidgetProvider update)
              +-------------------+
```

## 2. Three-Tier AI Inference System

Inference is dispatched by `AIEngineManager` based on network state, battery levels, and task size:

- **Tier 0 (SmolLM2-135M / 360M)**: Runs always on-device. Used for instant intent parsing and entity classification.
- **Tier 1 (Gemma-3 1B)**: Runs locally on-device when connected to Wi-Fi and charging. Used for widget planning and memory consolidations.
- **Tier 2 (Cloud Llama-3.3 / Gemma-3)**: Uses OpenRouter API. Optional user opt-in fallback for complex creation reasoning.

## 3. Agent Coordination

All agents are structured using Kotlin Coroutines and run independently. They communicate only via the `AgentEventBus` (`SharedFlow<AgentEvent>`), decoupling agents from each other.

## 4. Memory Architecture

- **Short-Term Memory**: SQL database storing the last 50 actions/taps (evicted after 72 hours).
- **Long-Term Memory**: DataStore Preference schemas and plain-text summaries processed nightly.
- **Semantic / Vector Memory**: SQLite table (`embedding_store`) storing 384-dimension float vectors generated locally via ONNX `all-MiniLM-L6-v2`. Cosine similarity is computed directly in Kotlin.

## 5. Security & Permission Lifecycle

1. **Lazy Requests**: Permissions (Location, Calendar, usage statistics) are requested only when a specific plugin requires it.
2. **Graceful Failures**: If permission is denied, the plugin returns a placeholder result rather than crashing.
3. **Local Encryption**: relational tables are encrypted via SQLCipher (AES-256) when a lock screen is configured on the host device.
