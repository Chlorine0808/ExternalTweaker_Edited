@echo off
if not exist standalone_bin mkdir standalone_bin

echo Compiling...
javac --release 8 -d standalone_bin -sourcepath src/main/java src/main/java/com/bartz24/externaltweaker/app/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Running...
java -cp standalone_bin com.bartz24.externaltweaker.app.Main
pause
