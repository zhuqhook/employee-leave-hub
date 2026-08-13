@echo off
cd /d %~dp0
title Employee Leave Hub

if not exist node_modules (
    call npm install
    echo.
)

start "" /min cmd /c "timeout /t 25 /nobreak >nul && start http://localhost:4200"
echo.

call npm run dev
