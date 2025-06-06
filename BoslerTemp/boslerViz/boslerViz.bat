@echo off
setlocal

:: Constants
set "SCRIPT_DIR=%~dp0"
set "REPOS_DIR=repos"
set "IMAGES_DIR=images"

:: Function

:start_containers
    docker compose up -d || exit /b 1
    goto :eof

:stop_containers
    docker compose down || exit /b 1
    goto :eof

:restart_containers
    call :stop_containers
    call :start_containers
    goto :eof

:check_container_status
    docker compose ps || exit /b 1
    goto :eof

:import_docker_images
    if exist "%IMAGES_DIR%" (
        echo %date% %time%: Importing Docker images from %IMAGES_DIR%...
        for %%i in ("%IMAGES_DIR%\*.tar") do (
            if exist "%%i" (
                echo Loading image: %%i
                docker load -i %%i || exit /b 1
            ) else (
                echo %date% %time%: No Docker image tar files found in %IMAGES_DIR%
            )
        )
    ) else (
        echo Directory %IMAGES_DIR% does not exist!
        exit /b 1
    )
    goto :eof

:import_database
    echo Waiting for PostgreSQL to start...
    :wait_postgres
    docker exec boson-db pg_isready -U postgres >nul 2>nul
    if errorlevel 1 (
        timeout /t 1 >nul
        goto :wait_postgres
    )

    echo PostgreSQL is up. Creating database...
    docker exec -it boson-db sh -c "su - postgres -c 'psql -c \"CREATE DATABASE boson;\"'" || exit /b 1

    echo Database created successfully.
    goto :eof

:install
    call :import_docker_images
    call :start_containers
    call :import_database
    call :restart_containers
    goto :eof

:: Main function
cd /d "%SCRIPT_DIR%"
echo Current working directory is now: %SCRIPT_DIR%

if "%1"=="start" (
    call :start_containers
) else if "%1"=="stop" (
    call :stop_containers
) else if "%1"=="restart" (
    call :restart_containers
) else if "%1"=="status" (
    call :build_platform
) else if "%1"=="install" (
    call :install
) else (
    echo Usage: %0 {start|stop|restart|status|install}
    exit /b 1
)

:: Optional: Clean up the repos folder after running
rd /s /q "%REPOS_DIR%"

endlocal
