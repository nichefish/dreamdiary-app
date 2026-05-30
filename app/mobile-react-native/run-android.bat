@echo off
REM DreamDiary Mobile — Android emulator (IntelliJ / Explorer double-click)
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-android.ps1"
exit /b %ERRORLEVEL%
