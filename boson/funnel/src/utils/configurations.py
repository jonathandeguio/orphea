import os

SPARK_APPLICATION_ID = os.environ.get('SPARK_APPLICATION_ID')
DEFAULT_BRANCH = os.environ.get('DEFAULT_BRANCH') if os.environ.get('DEFAULT_BRANCH') else "master"
MOVETODATA_API = os.environ.get('MOVETODATA_API') if os.environ.get('MOVETODATA_API') else "http://localhost:8080"
BUILD_TOKEN = os.environ.get('BUILD_TOKEN') if os.environ.get(
    'BUILD_TOKEN') else "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzZDNiMTM3Ni00NWM1LTRiNmYtYWJiYy1kYTIxNWYyZTAxZGMiLCJpYXQiOjE3MTE0NjM3MjAsImV4cCI6MTcxMTUwNjkyMH0.KnH3W1YGOMjTGN4hAzXqf5xnJRfpBS1_Ha8MJT9qot2QNhrQ58b4BeN5VDJTxjZFkk-gn3q-xfo9DveKXTHnzQ"
BUILD_ID = os.environ.get("BUILD_ID") if os.environ.get("BUILD_ID") else "a22817bb-11d8-46c8-91ea-735f8e48526d"
TRANSACTION_ID = os.environ.get("TRANSACTION_ID") if os.environ.get("TRANSACTION_ID") else ""
BUILD_TYPE = os.environ.get("BUILD_TYPE") if os.environ.get("BUILD_TYPE") else "PREVIEW"
REPOSITORY_ID = os.environ.get('REPOSITORY_ID') if os.environ.get(
    'REPOSITORY_ID') else "a22817bb-11d8-46c8-91ea-735f8e48526d"
SCRIPT_PATH = os.environ.get("SCRIPT_PATH") if os.environ.get("SCRIPT_PATH") else ""
LANGUAGE = "PYTHON"
PREVIEW_ID = os.environ.get("BUILD_ID") if os.environ.get("BUILD_ID") else ""
# THis is repo branch not dataset branch
REPO_BRANCH = os.getenv('BRANCH') if os.environ.get("BRANCH") else "master"
PHYSICAL_ENDPOINT = os.environ.get("PHYSICAL_ENDPOINT") if os.environ.get(
    "PHYSICAL_ENDPOINT") else None
BRANCH_ID = os.environ.get("BRANCH_ID") if os.environ.get("BRANCH_ID") else ""
COMMIT_ID = os.environ.get("COMMIT_ID") if os.environ.get("COMMIT_ID") else ""
FILE_NAME = os.environ.get("FILE_NAME")
LINE_NO = os.environ.get("LINE_NO")
ROW_LIMIT = os.environ.get("ROW_LIMIT")
SOURCE = os.environ.get("SOURCE")
BUILD_TRIGGER = os.environ.get("BUILD_TRIGGER")
BACKING_FS = os.environ.get("BACKING_FS")

