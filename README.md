# Pacman: Eternal Maze

A Java/Swing take on Pac-Man where every board is a newly generated maze and the ghosts can become much more dangerous than the classic four. Eat pellets, survive the powered ghosts, collect bonus items, and escape through the maze exits to push your score as far as possible.

## Features

- Randomly generated maze layouts with portals/exits.
- Classic pellet collection plus big pellets that trigger power pellet mode.
- Powered ghost variants, including laser, bomb, fire, speed, clone, magnet, and bonus ghosts.
- Power-ups that can help Pacman or, if a ghost reaches them first, empower the ghost.
- Pacman abilities such as magnet collection, spike traps, speed boost, score multiplier, bomb, laser, clone, and fire trail.
- Fruit bonuses, board-clear bonuses, level scaling, and a local Hall of Fame (up to 3 players).
- In-game options for speed, spawn timing, maze size, special ghost chance, no-power mode, and audio volume.
- Almanac entries for Pacman and the ghost cast.

## How to Play

The goal is to collect pellets, avoid or defeat ghosts, and move through open exits to continue into the next maze. Clearing half of the board opens exits. Clearing every pellet awards a board-clear bonus before you leave.

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
| Almanac navigation | `WASD` or arrow keys |
| Name entry | Arrow keys or `WASD`, then `Enter`/`Space` |

## Running the Game

This project has no external dependencies beyond a JDK. Assets are loaded from `res/...`, so run the game from the project root.

### Command Line

```powershell
javac -d bin src\game\*.java
java -cp bin game.GameMain
```

### Eclipse

1. Import the project as an existing Java project.
2. Make sure `src` is the source folder and `bin` is the output folder.
3. Run `game.GameMain`.

## Project Layout

```text
src/game/        Java source code
res/sprite/      Sprite assets
res/sfx/         Sound effects
res/music/       Background music
res/almanac/     Almanac text entries
playerscore.sav  Local high-score data
```

## Notes

- The main window title is `Pacman: Eternal Maze`.
- High scores are saved locally in `playerscore.sav`.
- The active music file used by the game is `res/music/Pac Terror.wav`.
- A matching `Pac Terror.mp3` is also included in `res/music/`.

## Credits

Music credited to: [https://www.youtube.com/watch?v=5XWQM08Ed9o](https://www.youtube.com/watch?v=5XWQM08Ed9o)
