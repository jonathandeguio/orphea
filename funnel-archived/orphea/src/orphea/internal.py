# -*- coding: utf-8 -*-


import datetime
import os
import requests
from pyspark.sql import SparkSession
from typing import Final

spark_application_id = os.environ.get('SPARK_APPLICATION_ID')

ORPHEA_API = os.environ.get('ORPHEA_API')
BUILD_TOKEN = os.environ.get('BUILD_TOKEN')
BUILD_ID = os.environ.get("BUILD_ID")
TRANSACTION_ID = os.environ.get("TRANSACTION_ID")
BUILD_TYPE = os.environ.get("BUILD_TYPE")
REPOSITORY_ID = os.environ.get('REPOSITORY_ID')
SCRIPT_PATH = os.environ.get("SCRIPT_PATH")
LANGUAGE = "PYTHON"
SOURCE = os.environ.get("BASE_URL")


def spark_session():
    spark = SparkSession.builder.getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    return spark


class Orphea:
    def __init__(self, token=BUILD_TOKEN):
        self.token = token
        self.headers = {
            'content-type': 'application/json',
            'Source': SOURCE,
            'Environment': 'production',
            'Authorization': f'Bearer {self.token}'
        }
        self.host = ORPHEA_API

    def api(self, method, endpoint, payload=None, payload_type=None):
        try:
            call_url = f"{self.host}{endpoint}"

            if method == "GET":
                response = requests.get(call_url, headers=self.headers, verify=False)
            elif method == "POST":
                if payload_type == "JSON":
                    response = requests.post(call_url, headers=self.headers, json=payload, verify=False)
                else:
                    response = requests.post(call_url, headers=self.headers, data=payload, verify=False)
            else:
                raise ValueError("Invalid HTTP method")

            return response

        except Exception as error:
            Logging().error(message=f"Not able to call api on {endpoint}", debug=f"{error}")


class Kitab:

    def __init__(self):
        pass

    def start_transaction(self, dataset_id, branch):
        api_response = Orphea().api("GET", f"/api/kitab/transaction/{dataset_id}/{branch}/start?buildId={BUILD_ID}")
        if api_response.status_code == 200:
            Logging().info(message=f"Starting transaction")
            return True
        else:
            return True  # TODO : below needs fixing (Bhagesh)
            Logging().error(
                message=f"Error starting a transaction, there might be already an active transaction on {dataset_id}",
                debug=f"{api_response.text}")  # TODO: add automatically generated link to abort transaction

    def end_transaction(self, dataset_id, branch):

        api_response = Orphea().api("GET", f"/api/kitab/transaction/{dataset_id}/{branch}/end")

        if api_response.status_code == 200:
            Logging().info(message=f"Ending transaction")
            return True
        else:
            Logging().error(message=f"Unable to complete transaction",
                            debug=f"{api_response.text}")  # TODO: add automatically generated link to abort transaction


class Build:

    def __init__(self):
        pass

    def pre_transform(self, sources):
        # In pre transform, source will always be master branch
        payload = {
            "branch": os.environ.get('BRANCH'),
            "repositoryId": os.environ.get('REPOSITORY_ID'),
            "scriptPath": os.environ.get("SCRIPT_PATH"),
            "language": LANGUAGE,
            "branchId": os.environ.get("BRANCH_ID"),
            "commitId": os.environ.get("COMMIT_ID"),
            "buildId": BUILD_ID,
            "sparkApplicationId": spark_application_id,
            "sources": sources,
            "buildType": BUILD_TYPE
        }

        api_response = Orphea().api("POST", "/api/funnel/preTransform", payload, "JSON")

        if api_response.status_code == 200:
            return api_response.json()
        else:
            Logging().error(message=f"Unable to perform pre transform",
                            debug=f"{api_response.text}")

    def post_transform(self, dataset_id, sources, transactionId):
        payload = {
            "target": dataset_id,
            "sources": sources,
            "transactionId": transactionId,
            "branch": os.environ.get('BRANCH'),
            "repositoryId": os.environ.get('REPOSITORY_ID'),
            "scriptPath": os.environ.get("SCRIPT_PATH"),
            "buildId": BUILD_ID,
        }

        api_response = Orphea().api("POST", "/api/funnel/postTransform", payload, "JSON")
        if api_response.status_code == 200:
            return True
        else:
            Logging().error(message=f"Unable to perform post transform",
                            debug=f"{api_response.text}")

    def preview_result(self, result, schema):
        payload = {
            "schema": schema,
            "data": result,
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH
        }

        api_response = Orphea().api("POST", f"/api/funnel/previewResult", payload, "JSON")
        if api_response.status_code == 200:
            return True
        else:
            Logging().error(message=f"Unable to send preview results",
                            debug=f"{api_response.text}")


class Logging:
    def __init__(self):
        self.handlers = []  # To implement handlers for different output destinations

    def add_handler(self, handler):
        self.handlers.append(handler)

    def log(self, status, stage, message, debug=None):
        now = datetime.datetime.now()
        formatted_date = now.strftime("%y/%m/%d %H:%M:%S")
        log_entry = f"{formatted_date} {status.upper()} Running: {message} {debug}"

        for handler in self.handlers:
            handler.emit(log_entry)

        payload = {
            "status": status,
            "stage": stage,
            "message": message,
            "debug": f"{debug}",
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH
        }

        api_response = None

        if BUILD_TYPE == "PREVIEW" and status == Constants().ERROR:
            api_response = Orphea().api("POST", f"/api/funnel/log", payload, "JSON")
        elif BUILD_TYPE == "DEFAULT":
            api_response = Orphea().api("POST", f"/api/build/{BUILD_ID}/log", payload, "JSON")

        if api_response is not None and api_response.status_code != 200:
            error_message = f"{formatted_date} ERROR Running: {message} {debug}"
            for handler in self.handlers:
                handler.emit(error_message)
            raise Exception(error_message)

        if status == Constants().ERROR:
            error_exception = Exception(f"{formatted_date} [status]: {message} {debug}")
            for handler in self.handlers:
                handler.emit_error(error_exception)
            raise error_exception

    def info(self, message, debug=None):
        self.log(Constants().INFO, Constants().RUNNING, message, debug)

    def error(self, message, debug=None):
        self.log(Constants().ERROR, Constants().FINISHED, message, debug)


class Constants:
    # Stages
    RUNNING: Final = "RUNNING"
    FINISHED: Final = "FINISHED"

    # Status
    INFO: Final = "INFO"
    ERROR: Final = "ERROR"
