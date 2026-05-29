@echo off
cd /d "%~dp0"
git add .
git commit -m "Fix: Runtime permissions, service lifecycle, and dependency injection"
git push origin main
pause