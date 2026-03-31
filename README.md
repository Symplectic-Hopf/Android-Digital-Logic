# Android Digital Logic - Karnaugh Map Simplification

An Android learning application for **Digital Logic & Karnaugh Map Simplification**, built with Kotlin + Jetpack Compose (MVI architecture).

## Features

### 1. Quine-McCluskey Algorithm (Domain Layer)
- Supports **2 to 6 variables**
- Accepts input via truth table (click cells) or minterm/don't-care lists
- Outputs both **minimal SOP** (Sum of Products) and **minimal POS** (Product of Sums) expressions
- Full prime implicant and essential prime implicant computation

### 2. Dynamic Karnaugh Map Grid (UI Layer)
- Automatically renders the correct K-map size:
  - 2 variables → 2×2 grid
  - 3 variables → 2×4 grid
  - 4 variables → 4×4 grid
  - 5 variables → 4×8 grid
  - 6 variables → 8×8 grid
- Gray-code ordering on both axes
- **Click cells** to cycle through states: **0 → 1 → X** (don't care)
- **Color-coded group overlays** showing each implicant's coverage

### 3. Step-by-Step Learning Mode
- Animated step-by-step breakdown of the simplification process:
  1. Initial Minterms (初始最小项)
  2. Prime Implicants (本原蕴涵项)
  3. Essential Prime Implicants (必要本原蕴涵项)
  4. Cover Remaining Minterms (覆盖剩余最小项)
  5. Final Result (最终结果)
- Progress indicator with previous/next navigation
- K-map highlights update in sync with each step

## Project Structure

```
app/src/main/java/com/digitallogic/karnaughmap/
├── MainActivity.kt
├── domain/
│   ├── algorithm/
│   │   ├── KarnaughMapLayout.kt   # Gray-code grid positioning helper
│   │   └── QuineMcCluskey.kt      # Core Q-M simplification algorithm
│   └── model/
│       ├── Implicant.kt           # Implicant data model + SOP/POS string generation
│       ├── KarnaughMapData.kt     # K-map state (cells with 0/1/X values)
│       └── SimplificationResult.kt # Result + step-by-step data
└── ui/
    ├── components/
    │   ├── KarnaughMapGrid.kt     # Dynamic K-map grid Composable
    │   ├── ResultPanel.kt         # SOP/POS result display
    │   └── StepByStepPanel.kt     # Step navigation panel
    ├── mvi/
    │   └── KMapState.kt           # MVI State + Events (sealed class)
    ├── screens/
    │   └── MainScreen.kt          # Main screen Composable
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── viewmodel/
        └── KMapViewModel.kt       # MVI ViewModel
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVI (Model-View-Intent)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35
- **Build**: Gradle 8.9 + AGP 8.5.2

## Building

```bash
./gradlew assembleDebug
```

## Running Tests

```bash
./gradlew test
```

The domain layer unit tests (`QuineMcCluskeyTest`) verify the Q-M algorithm across all variable counts (2–6) and edge cases.
