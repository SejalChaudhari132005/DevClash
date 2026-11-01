@echo off
title Stop DevClash
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0stop_all.ps1"
pause
