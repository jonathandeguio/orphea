@echo off
set NGINX_PATH=C:\Bosler\softwares\nginx-1.25.5
cd %NGINX_PATH%
nginx -g "daemon off;"
