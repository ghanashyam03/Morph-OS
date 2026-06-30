# MorphOS — Engineering Architecture Documentation

MorphOS is an AI-powered, adaptive Android widget platform designed to dynamically construct, schedule, and personalize Android AppWidgets using local and cloud AI models.

## Module Structure

The project follows a modular, feature-based Clean Architecture structure:

```
                  ┌──────────────┐
                  │    :app      │ (Application entry-point, Theme)
                  └──────┬───────┘
                         │
         ┌───────────────┼───────────────┬────────────────┐
         ▼               ▼               ▼                ▼
┌────────────────┐┌──────────────┐┌──────────────┐┌──────────────┐
│ :feature:dash..││ :feature:cre..││ :feature:set..││:feature:onb..│ (Feature modules)
└────────┬───────┘└──────┬───────┘└──────┬───────┘└──────┬───────┘
         │               │               │               │
         └───────────────┼───────────────┴───────────────┘
                         ▼
                ┌────────────────┐
                │  :core:widget  │ (Glance rendering, layouts & state)
                └────────┬───────┘
                         ▼
                ┌────────────────┐
                │    :core:ai    │ (LlamaCppEngine, OnnxEmbedding, Download)
                └────────┬───────┘
                         ▼
                ┌────────────────┐
                │   :core:data   │ (Room, DataStore, Repositories, Workers)
                └────────┬───────┘
                         ▼
                ┌────────────────┐
                │  :core:domain  │ (Entities, Use Cases, Agents, Interfaces)
                └────────┬───────┘
                         ▼
                ┌────────────────┐
                │  :core:common  │ (Dispatchers, Results, Utilities)
                └────────────────┘
```

---

## Architecture Flowcharts

### 1. Natural Language Widget Creation Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as User
    participant Creator as WidgetCreatorScreen / ViewModel
    participant Orchestrator as AgentOrchestrator
    participant IntentAgent as IntentAgentImpl
    participant PlanningAgent as PlanningAgentImpl
    participant AI as AIEngineManager
    participant Repos as WidgetRepository & DB

    User->>Creator: Input description: "build steps and clock tracker"
    Creator->>Orchestrator: handleUserInput(description)
    Orchestrator->>IntentAgent: parseIntent(description)
    IntentAgent->>AI: Classify intent (Local Tier 0 Model / Fallbacks)
    AI-->>IntentAgent: Parsed IntentType (e.g., CREATE_FITNESS_WIDGET)
    IntentAgent-->>Orchestrator: WidgetIntent
    Orchestrator->>PlanningAgent: generatePlan(intent, context, memory)
    PlanningAgent->>AI: Plan configuration (Local Tier 1 Model / Fallbacks)
    AI-->>PlanningAgent: JSON representation of WidgetPlan
    PlanningAgent-->>Orchestrator: WidgetPlan
    Orchestrator-->>Creator: WidgetPlan (confidence)
    
    alt high confidence (> 0.75)
        Creator->>User: Show Widget Preview Screen
    else low confidence
        Creator->>User: Go to Template Selection Screen
    end
    
    User->>Creator: Confirm & Create
    Creator->>Repos: createWidget(plan)
    Repos-->>User: Widget placed on home screen!
```

---

### 2. Adaptive Widget Update Loop (Context-Aware)

```mermaid
graph TD
    A[WorkManager / ContextRefreshWorker] --> B[ContextAgent.refresh]
    B --> C[Query Sensors, Battery, Network, PowerSaver]
    C --> D[Update Local ContextSnapshot]
    D --> E[Check Battery Status]
    
    E -- Battery < 10% --> F[Skip all background updates]
    E -- Battery < 20% --> G[Skip network plugins, update clock widgets only]
    E -- Battery >= 20% --> H[Run full update pipeline]
    
    G --> I[WidgetUpdatePipeline.updateAllWidgets]
    H --> I
    I --> J[Fetch cached / fresh plugin data]
    J --> K[Update Glance state using updateAppWidgetState]
    K --> L[Notify Widget Provider to draw on launcher]
```

---

## Local AI Fallback System

In the event of network disconnection, model corruption, or engine load issues, MorphOS leverages a tiered fallback design:

1. **Tier 2 (Cloud AI)**: Used for complex intents/plans requiring remote LLM processing (if enabled in user settings).
   - *Fallback on failure / offload*: Queries Tier 1 local model.
2. **Tier 1 (Local Gemma-3 1B)**: Used for local widget planning.
   - *Fallback on failure / offload*: Uses pre-compiled deterministic rule-based planning mapping `IntentType` directly to standard templates.
3. **Tier 0 (Local SmolLM-135M)**: Used for lightning-fast local intent classification.
   - *Fallback on failure / offload*: Reverts to Regex/Keyword classification matching patterns like `"study"`, `"gym"`, `"weather"`.

---

## Security Hardening Details

- **Database Encryption**: MorphOS utilizes SQLCipher for encrypting the Room database. The passphrase is automatically generated as a secure 256-bit key, encrypted via Android KeyStore, and stored inside private SharedPreferences.
- **Network Security**: TLS-only traffic configuration (`cleartextTrafficPermitted="false"`) prevents MITM attacks.
- **Data Sanitization**: All user strings are scrubbed of HTML tags, JavaScript declarations, and formatting comment tokens (`' " # ; -- *`) to protect against prompt injections.
