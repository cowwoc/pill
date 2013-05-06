@echo off
REM ----------------------------------------------------------------------------
REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.
REM ----------------------------------------------------------------------------

REM ----------------------------------------------------------------------------
REM Pill Start Up Batch script
REM
REM Required ENV vars:
REM JAVA_HOME - location of a JDK home dir
REM ----------------------------------------------------------------------------

REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set ERROR_CODE=0
setlocal

REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto JavaHomeValidated

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME environment variable to the JDK installation path.
echo.
goto error

:JavaHomeValidated
if exist "%JAVA_HOME%\bin\java.exe" goto checkPillHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME environment variable to the JDK installation path.
echo.
goto error

:checkPillHome
if not "%PILL_HOME%"=="" goto validatePillHome

if "%OS%"=="Windows_NT" SET "PILL_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "PILL_HOME=%~dp0.."
if not "%PILL_HOME%"=="" goto validatePillHome

echo.
echo ERROR: PILL_HOME not found in your environment.
echo Please set the PILL_HOME variable in your environment to match the
echo location of the Pill installation
echo.
goto error

:validatePillHome

:stripPillHome
REM If %PILL_HOME% ends with slash, strip it
if not "_%PILL_HOME:~-1%"=="_\" goto checkPillBat
set "PILL_HOME=%PILL_HOME:~0,-1%"
goto stripPillHome

:checkPillBat
if exist "%PILL_HOME%\bin\pill.bat" goto init

echo.
echo ERROR: PILL_HOME is set to an invalid directory.
echo PILL_HOME = "%PILL_HOME%"
echo Please set the PILL_HOME variable in your environment to match the
echo location of the Pill installation
echo.
goto error
REM ==== END VALIDATION ====

:init
REM Decide how to startup depending on the version of windows

REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

:WinNTNovell
REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

REM -- Regular WinNT shell
set PILL_CMD_LINE_ARGS=%*
goto endInit

REM The 4NT Shell from jp software
:4NTArgs
set PILL_CMD_LINE_ARGS=%$
goto endInit

REM Reaching here means variables are defined and arguments have been captured
:endInit
SET Pill_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

REM Start Pill
:runPill
%PILL_JAVA_EXE% "-Dpill.home=%PILL_HOME%" -jar "%PILL_HOME%\dist\pill.core.jar" %PILL_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT
goto postExec

:endNT
endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec
cmd /C exit /B %ERROR_CODE%
