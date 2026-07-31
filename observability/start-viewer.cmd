@echo off
REM Double-click to start the local log viewer (Loki + Alloy + Grafana),
REM then open Grafana in the browser. See docs/logging.md.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-local.ps1" -Command start
start "" http://localhost:3000
echo.
pause
