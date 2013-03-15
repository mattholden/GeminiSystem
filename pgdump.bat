@echo off
SET PGPASSWORD=DevBoxOnly
pg_dump %2 -U postgres --file=%1%2.sql %1
set PGPASSWORD=UNSET
