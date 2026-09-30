# Pacman: Eternal Maze

A Pac-Man parody game where you have random gen maze and power up. but you aren't the only one that can use the power ups. Survive the chaos and try to gets a highscore
 

## Features

- Randomly generated maze layouts.
- Classic pellet collection plus big pellets that trigger power pellet mode.
- Powered ghost variants, including laser, bomb, fire, speed, clone, magnet, and bonus ghosts.
- Power-ups that can help Pacman or, if a ghost reaches them first, empower the ghost. These include but not exclusive to magnet, spike traps, speed boost, score multiplier, bomb, laser, clone, fire trail, etc...
- In-game options for speed, spawn timing, maze size, special ghost chance, custom ghost/power-up spawns, and audio volume.
- Almanac entries for Pacman and the ghost roster.

## How to Play

The goal is to collect pellets, avoid ghosts, and move through open exits to continue into the next maze. Clearing half of the board opens exits. Clearing every pellet awards a board-clear bonus before you leave. there're no winning condition so just aim for a high score

Ghosts spawn over time and can enter powered forms naturally or by collecting power-ups. Powered ghosts have special behaviors, so the maze becomes more chaotic as a run goes on.

## Controls

| Action | Keys |
| --- | --- |
| Move Pacman / navigate menus | `WASD` or arrow keys |
| Confirm menu selection | `Enter` or `Space` |
| Pause / resume | `P` |
| Open pause/quit prompt | `Esc` |
| Return to menu from a run | Press `Esc` while paused, then `Esc` again |
| Restart current run | `R` |
| Zoom camera | `Z` out, `C` in, `X` reset |
| Ability | `Q` to explode or emit electricity, `E` to dash as stone |
| Almanac navigation | `WASD` or arrow keys |

## Running the Game

This project has no external dependencies beyond a JDK. Assets are loaded from `res/...`, so run the game from the project root.

The runtime target is Java 21. A newer JDK may be used to compile, but keep `--release 21` so the generated classes remain compatible with Java 21.

### Command Line

```powershell
javac --release 21 -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin game.GameMain
```

### Eclipse

1. Import the project as an existing Java project.
2. Make sure `src` is the source folder and `bin` is the output folder.
3. Run `game.GameMain`.

## Project Layout

```text
src/game/        Java source code
src/game/ui/     Swing adapters
src/game/input/  Input adapters
src/game/rendering/ Rendering entry points
src/game/systems/ Simulation systems
src/game/resources/ Resource loading
res/sprite/      Sprite assets and JSON content definitions
res/sprite/ghost/ghost.json      Ghost behavior and sprite definitions
res/sprite/ghost/almanac.json    Almanac names and descriptions
res/sprite/player/pacman.json    Pacman sprite states and priority
res/sfx/         Sound effects
res/music/       Background music
playerscore.sav  Local high-score data
```

## Notes

- The main window title is `Pacman: Eternal Maze`.
- High scores are saved locally in `playerscore.sav`.
- The active music file used by the game is `res/music/Pac Terror.wav`.

## Runtime Architecture

The Swing surface is now kept in `game.ui.GamePanel`. It owns the window-facing lifecycle and delegates to three focused collaborators:

- `GameInput` converts Swing key events into game commands.
- `GameRenderer` is the paint entry point and owns the graphics context boundary.
- `Game` owns simulation state and the 60 FPS loop.

The simulation update order is coordinated by `game.systems.GameSystems`, with separate player, ghost, effect, collision, and transition systems. `GameState` replaces the old integer screen-state flags with named lifecycle states.

Resources are loaded through `game.resources.ResourceLoader`. It checks the classpath first and then the project-root `res/` directory, so the source tree works with the current layout without copying assets into `src` or `bin`.

## Credits

Music credited to: [https://www.youtube.com/watch?v=5XWQM08Ed9o](https://www.youtube.com/watch?v=5XWQM08Ed9o)
