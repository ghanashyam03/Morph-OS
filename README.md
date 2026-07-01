# MorphOS — AI-Powered Adaptive Android Widget Platform

MorphOS is an agentic, local-first adaptive home screen widget launcher assistant for Android. It uses a three-tier AI architecture to rank content, analyze intent, and summarize on-device context using local Large Language Models (LLMs) like SmolLM2 and Gemma-3 alongside ONNX-based embedding models.

## Module Dependency Graph

```
                   ┌──────────────┐
                   │     :app     │
                   └──────┬───────┘
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
┌────────────────┐┌──────────────┐┌────────────────┐
│:feature:onb... ││:feature:dash ││:feature:sett...│
└────────┬───────┘└───────┬──────┘└────────┬───────┘
         │                │                │
         └────────────────┼────────────────┘
                          ▼
              ┌───────────────────────┐
              │:feature:widget-creator│
              └───────────┬───────────┘
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
┌────────────────┐┌──────────────┐┌────────────────┐
│  :core:widget  ││  :core:data  ││   :core:ai     │
└────────┬───────┘└───────┬──────┘└────────┬───────┘
         │                │                │
         └────────────────┼────────────────┘
                          ▼
                   ┌──────────────┐
                   │ :core:domain │
                   └──────┬───────┘
                          │
                          ▼
                   ┌──────────────┐
                   │ :core:common │
                   └──────────────┘
```

All features and data layers also use `:core:testing` for unit and integration testing.

## Build Requirements

- JDK 17
- Android SDK (API level 35)
- Git

## How to Build

Run the following command to download Gradle wrapper and build the debug APK:
```bash
./gradlew assembleDebug
```

## Running Tests

To run the unit tests across all modules:
```bash
./gradlew test
```

To run instrumented integration tests:
```bash
./gradlew connectedCheck
```

## Environment Variables / Secrets
The CI workflow and cloud inference engine expect these environment variables (configured via GitHub Secrets or `local.properties`):
- `OPENROUTER_API_KEY`: API key for Gemma-3-27B cloud backup inference.
- `CLOUDFLARE_AI_KEY`: Backup endpoint API key.

## Architecture Documentation
For details on database schemas, data flows, AI routing policies, and agents structure, please refer to [ARCHITECTURE.md](./ARCHITECTURE.md).

## Local AI Model Download Instructions
On first execution, `ModelDownloadManager` will download the following GGUF models:
1. **Intent Classifier**: `smollm2-135m-q4.gguf` (~90 MB)
2. **Intent Parser (NLU)**: `smollm2-360m-q4.gguf` (~230 MB)
3. **Widget Planner**: `gemma3-1b-q4_k_m.gguf` (~900 MB)
4. **Embedding Model**: `all-minilm-l6-v2.onnx` (~23 MB)
