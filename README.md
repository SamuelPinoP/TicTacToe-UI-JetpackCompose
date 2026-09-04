# TicTacToe UI Jetpack Compose

Native Android Tic Tac Toe built with Kotlin, Jetpack Compose, and Material 3.

## Interview-ready highlights

- Immutable game state with a pure `TicTacToeEngine` for move validation, win detection, draw detection, scoring, undo, and timeout forfeits.
- Three AI levels: practice move ordering, tactical win/block play, and expert minimax with alpha-beta pruning.
- Polished Compose UI with responsive board sizing, match score, turn timer, mode controls, difficulty controls, and highlighted winning lines.
- Canvas-rendered X/O pieces that scale with their cells instead of fixed button text.
- JVM unit tests for engine rules, scoring, undo behavior, and AI move selection.

## Technical talking points

- Separating the engine from Compose makes the game rules deterministic and testable without Android instrumentation.
- Minimax models the game tree, scores terminal states, and uses alpha-beta pruning to skip branches that cannot improve the result.
- The UI is state-driven: user actions produce a new `GameState`, and Compose recomposes from that state.
- Accessibility hooks are included through board cell content descriptions.

## Useful commands

```bash
./gradlew test
./gradlew assembleDebug
```
