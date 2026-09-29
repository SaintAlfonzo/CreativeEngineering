@echo off
REM Saves the server's always-on performance recording to perf\recordings\ - see perf\README.md.
REM   perf\dump.bat               everything kept, up to the last 30 minutes
REM   perf\dump.bat 10m           only the last 10 minutes: s, m or h
REM   perf\dump.bat 10m lagspike  adds a word to the file name
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0dump.ps1" -Age "%~1" -Tag "%~2"
