@echo off
setlocal
cd /d %~dp0

set "CODEX_PY=%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
set "PYTHON_CMD=python"
if exist "%CODEX_PY%" (
    set "PYTHON_CMD=%CODEX_PY%"
)

if not exist .venv (
    "%PYTHON_CMD%" -m venv .venv
)
call .venv\Scripts\activate.bat
python -m pip install --upgrade pip
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
