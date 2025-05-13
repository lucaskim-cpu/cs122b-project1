@echo off
echo Running stored procedure SQL script...
echo.

REM Update these MySQL connection details with your actual credentials
set MYSQL_USER=mytestuser
set MYSQL_PASSWORD=My6$Password
set MYSQL_HOST=localhost
set MYSQL_DB=moviedb

echo Using database credentials:
echo User: %MYSQL_USER%
echo Database: %MYSQL_DB%
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% %MYSQL_DB% < stored-procedure.sql

if %ERRORLEVEL% EQU 0 (
  echo.
  echo ✅ Stored procedure creation successful!
  echo You can now use the "Add Movie" feature on the dashboard.
) else (
  echo.
  echo ❌ Error creating stored procedure.
  echo Please check your MySQL credentials and connection.
)

pause
