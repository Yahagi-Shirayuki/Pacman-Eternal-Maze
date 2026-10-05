@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem Keep all paths relative to this script so it works from any command prompt.
set "ROOT=%~dp0"
set "BIN=%ROOT%bin"
set "MAIN_CLASS=game.GameMain"
set "COMMAND=%~1"

if not defined COMMAND set "COMMAND=--run"

if /I "%COMMAND%"=="--help" goto :help
if /I "%COMMAND%"=="-h" goto :help
if /I "%COMMAND%"=="--build" goto :build
if /I "%COMMAND%"=="--run" goto :run
if /I "%COMMAND%"=="--clean" goto :clean

echo Unknown command: %COMMAND%
echo.
goto :help_error

:run
call :build
if errorlevel 1 exit /b 1

echo Starting %MAIN_CLASS%...
pushd "%ROOT%"
java -cp "%BIN%" %MAIN_CLASS%
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:build
where javac >nul 2>&1
if errorlevel 1 (
    echo ERROR: javac was not found.
    echo Install a JDK 21 or newer and make sure its bin folder is on PATH.
    echo See REQUIREMENT.MD for setup instructions.
    exit /b 1
)

if not exist "%ROOT%src\*.java" if not exist "%ROOT%src\game\*.java" (
    echo ERROR: The src folder was not found. Run this command from the downloaded project folder.
    exit /b 1
)

if not exist "%BIN%" mkdir "%BIN%"

set "SOURCE_LIST=%TEMP%\pacman-sources-%RANDOM%-%RANDOM%.txt"
for /r "%ROOT%src" %%F in (*.java) do (
    set "SOURCE_FILE=%%~fF"
    set "SOURCE_FILE=!SOURCE_FILE:\=/!"
    echo "!SOURCE_FILE!">>"%SOURCE_LIST%"
)

echo Compiling Java sources...
javac --release 21 -encoding UTF-8 -d "%BIN%" @"%SOURCE_LIST%"
set "BUILD_CODE=%ERRORLEVEL%"
del /q "%SOURCE_LIST%" >nul 2>&1

if not "%BUILD_CODE%"=="0" (
    echo.
    echo ERROR: Compilation failed. Check that a JDK 21 or newer is installed.
    exit /b %BUILD_CODE%
)

echo Build complete.
exit /b 0

:clean
if exist "%BIN%" (
    rmdir /s /q "%BIN%"
    echo Removed compiled files from bin.
) else (
    echo Nothing to clean.
)
exit /b 0

:help
echo Pacman: Eternal Maze command launcher
echo.
echo Usage:
echo   run.cmd --run      Compile the game and launch it. (Default)
echo   run.cmd --build    Compile the game only.
echo   run.cmd --clean    Remove compiled class files from bin.
echo   run.cmd --help     Show this help.
echo.
echo Run this command from the project root. See REQUIREMENT.MD for setup.
exit /b 0

:help_error
call :help
exit /b 2
