# Requirements

## Required software

This game needs:

- Windows with Command Prompt (`cmd.exe`)
- A Java Development Kit (JDK) version 21 or newer

The JDK must provide both `java` and `javac`. There are no third-party Java libraries to install.

You can install a JDK from [Eclipse Adoptium](https://adoptium.net/temurin/releases/) or another trusted Java distribution. During installation, enable the option that adds Java to `PATH` if it is offered.

## Check the installation

Open a new Command Prompt and run:

```cmd
java --version
javac --version
```

Both commands must work and report version 21 or newer. If Windows says that either command is not recognized, reopen Command Prompt after installing the JDK or add the JDK's `bin` directory to `PATH`.

## Build and run

1. Download or clone the project.
2. Open the project folder in File Explorer.
3. Click the folder address bar, type `cmd`, and press Enter.
4. Run:

```cmd
run.cmd --run
```

The launcher compiles the Java source files into `bin` and starts the game. It does not create an `.exe` file. The game's assets and save files are read from the project folder, so always launch it from this folder.

You can also use the shorter command in a normal Command Prompt opened in this folder:

```cmd
run --run
```

## Other commands

```cmd
run.cmd --build
run.cmd --clean
run.cmd --help
```

`--build` compiles without starting the game. `--clean` removes generated class files from `bin`; it does not remove source code, assets, or save files.
