@echo off
REM Double-click to stop the local log viewer.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-local.ps1" -Command stop
echo.
pause
