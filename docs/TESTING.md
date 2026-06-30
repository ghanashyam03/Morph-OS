# MorphOS — Testing Guidelines

MorphOS contains a robust test suite spanning unit testing, integration testing, and end-to-end (E2E) testing across all layers of the application.

## Test Directory Mapping

- **Unit Tests**:
  - Domain layer: `core/domain/src/test/` (UseCases, entities)
  - Data layer: `core/data/src/test/` (Repositories, Agent implementations, database helpers)
  - AI engine layer: `core/ai/src/test/` (AIEngineManager, CloudAIEngine, PromptBuilder)
  - ViewModels: `feature/<feature_name>/src/test/` (DashboardViewModel, WidgetCreatorViewModel, SettingsViewModel)
- **Integration & Instrumented Tests**:
  - WorkManager: `core/data/src/androidTest/` (Workers testing using TestListenableWorkerBuilder)
  - Glance pipeline: `core/widget/src/test/` (WidgetUpdatePipeline integration test)
  - ContextAgent: `core/data/src/test/` (Robolectric sensor/broadcast tests)
- **End-to-End Tests**:
  - `app/src/androidTest/` (MainActivity, onboarding, and creation flows using ComposeTestRule, Hilt, and Espesso)

---

## Test Execution Commands

To execute the tests locally or in a CI environment, run the following Gradle commands in the terminal:

### 1. Run All Local Unit Tests
```bash
./gradlew testDebugUnitTest
```

### 2. Run Module-Specific Unit Tests
- **Core Domain**:
  ```bash
  ./gradlew :core:domain:testDebugUnitTest
  ```
- **Core Data**:
  ```bash
  ./gradlew :core:data:testDebugUnitTest
  ```
- **Core AI**:
  ```bash
  ./gradlew :core:ai:testDebugUnitTest
  ```
- **Dashboard Feature**:
  ```bash
  ./gradlew :feature:dashboard:testDebugUnitTest
  ```

### 3. Run Static Code Analysis & Linting
```bash
./gradlew lintDebug
```

---

## Testing Mocks and Setup

We utilize:
- **JUnit 5** for modern assertion styles in unit tests.
- **MockK** for stubbing dependencies and verifying method calls.
- **Turbine** for testing Kotlin Flows reactively.
- **Robolectric** for Android runtime tests executing on JVM.
- **TestListenableWorkerBuilder** for testing background workers.
