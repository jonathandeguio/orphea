import os

spark_application_id = os.environ.get('SPARK_APPLICATION_ID')

MOVETODATA_API = os.environ.get('MOVETODATA_API') if os.environ.get('MOVETODATA_API') else "http://localhost:8080"
BUILD_TOKEN = os.environ.get('BUILD_TOKEN') if os.environ.get('BUILD_TOKEN') else "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhYmNlMzBmOC0xY2FkLTRkOWQtOTIwYS0zYWYzYzA5Y2ExZjIiLCJpYXQiOjE3MTEzNjE5NzIsImV4cCI6MTcxNzcxODQwMH0.4hyIdVrMApmTO6TdcrWDqlHvEIMRhT75fZBhjtZY1auQa84XnAC72HxBgQPNPBRyYvmIl6GFr5dnRTd5ps9c7Q"
BUILD_ID = os.environ.get("BUILD_ID") if os.environ.get("BUILD_ID") else "a22817bb-11d8-46c8-91ea-735f8e48526d"
TRANSACTION_ID = os.environ.get("TRANSACTION_ID") if os.environ.get("TRANSACTION_ID") else ""
BUILD_TYPE = os.environ.get("BUILD_TYPE") if os.environ.get("BUILD_TYPE") else "PREVIEW"
REPOSITORY_ID = os.environ.get('REPOSITORY_ID') if os.environ.get('REPOSITORY_ID') else "a22817bb-11d8-46c8-91ea-735f8e48526d"
SCRIPT_PATH = os.environ.get("SCRIPT_PATH") if os.environ.get("SCRIPT_PATH") else ""
LANGUAGE = "PYTHON"
PREVIEW_ID = os.environ.get("BUILD_ID") if os.environ.get("BUILD_ID") else ""
REPO_BRANCH = os.getenv('BRANCH') if os.environ.get("BRANCH") else "master"
PHYSICAL_ENDPOINT = os.environ.get("PHYSICAL_ENDPOINT") if os.environ.get("PHYSICAL_ENDPOINT") else "/Users/commonaccount/movetodata/localFS/dataset"