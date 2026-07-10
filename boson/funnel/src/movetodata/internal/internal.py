# -*- coding: utf-8 -*-

import datetime
import requests
import sys
import os

from pyspark.sql import SparkSession
from typing import Final

from utils.configurations import BUILD_TOKEN, MOVETODATA_API, LANGUAGE, BUILD_ID, SPARK_APPLICATION_ID, BUILD_TYPE, \
    REPOSITORY_ID, \
    SCRIPT_PATH, REPO_BRANCH, BRANCH_ID, COMMIT_ID, SOURCE, BUILD_TRIGGER


def spark_session():
    spark = SparkSession.builder.getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    return spark


class MoveToData:
    def __init__(self, token=BUILD_TOKEN):
        self.token = token
        self.headers = {
            'content-type': 'application/json',
            'Source': SOURCE,
            'Accept-Language': "en-US,en;q=0.5",
            'Environment': 'production',
            'Authorization': f'Bearer {self.token}'
        }
        self.host = MOVETODATA_API

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
            Logging().error(message=f"Not able to call functions on {endpoint}", debug=f"{error}")


class Kitab:

    def __init__(self):
        pass

    def start_transaction(self, dataset_id, branch, build_id = None):
        if build_id is None:
            build_id = BUILD_ID
        api_response = MoveToData().api("GET",
                                    f"/api/kitab/transaction/{dataset_id}/{branch}/start?buildId={build_id}")
        if api_response.status_code == 200:
            Logging().info(message=f"Starting transaction")
            return True
        else:
            return True  # TODO : below needs fixing (Bhagesh)
            Logging().error(
                message=f"Error starting a transaction, there might be already an active transaction on {dataset_id}",
                debug=f"{api_response.text}", build_id=build_id)  # TODO: add automatically generated link to abort transaction

    def end_transaction(self, dataset_id, branch, build_id = None):

        api_response = MoveToData().api("GET", f"/api/kitab/transaction/{dataset_id}/{branch}/end")

        if api_response.status_code == 200:
            Logging().info(message=f"Completed transaction on {dataset_id}", build_id=build_id)
            return True
        else:
            Logging().error(message=f"Unable to complete transaction",
                            debug=f"{api_response.text}", build_id=build_id)  # TODO: add automatically generated link to abort transaction


class Build:

    def __init__(self):
        pass

    def initiate_notebook_build(self, build_id, user_id, branch):
        payload = {
            "buildId": build_id,
            "userId": user_id,
            "branch": branch
        }

        api_response = MoveToData().api("POST", "/api/build/buildNotebook", payload, "JSON")

        if api_response.status_code == 200:
            return api_response.json()
        else:
            Logging().error(message=f"Unable to initiate the notebook build",
                            debug=f"{api_response.text}", build_id=build_id)

    def pre_transform(self, sources):
        # In pre transform, source will always be master branch
        SPARK_APPLICATION_ID = os.environ.get('SPARK_APPLICATION_ID')
        payload = {
            "branch": REPO_BRANCH,
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH,
            "language": LANGUAGE,
            "branchId": BRANCH_ID,
            "commitId": COMMIT_ID,
            "buildId": BUILD_ID,
            "sparkApplicationId": SPARK_APPLICATION_ID,
            "sources": sources,
            "buildType": BUILD_TYPE
        }

        api_response = MoveToData().api("POST", "/api/funnel/preTransform", payload, "JSON")

        if api_response.status_code == 200:
            return api_response.json()
        else:
            Logging().error(message=f"Unable to perform pre transform",
                            debug=f"{api_response.text}")

    def resolve_target(self, target, sources, filename, lineno):
        # In pre transform, source will always be master branch
        SPARK_APPLICATION_ID = os.environ.get('SPARK_APPLICATION_ID')
        payload = {
            "sources": sources,
            "target": target,
            "branch": REPO_BRANCH,
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH,
            "language": LANGUAGE,
            "branchId": BRANCH_ID,
            "commitId": COMMIT_ID,
            "buildId": BUILD_ID,
            "sparkApplicationId": SPARK_APPLICATION_ID,
            "buildTrigger": BUILD_TRIGGER,
            "fileName": filename,
            "lineNo": lineno
        }

        api_response = MoveToData().api("POST", "/api/funnel/resolveTarget", payload, "JSON")

        if api_response.status_code == 200:
            return api_response.json()
        else:
            Logging().error(message=f"Unable to resolve target",
                            debug=f"{api_response.text}")

    def resolve_target_notebook(self, target, branch_to_write, build_id, user_id):
        # In pre transform, source will always be master branch
        payload = {
            "userId": user_id,
            "sources": [],
            "target": target,
            "branch": branch_to_write,
            "repositoryId": None,
            "scriptPath": None,
            "language": None,
            "branchId": None,
            "commitId": None,
            "buildId": build_id,
            "sparkApplicationId": SPARK_APPLICATION_ID,
            "buildTrigger" : "NOTEBOOK",
            "fileName": None,
            "lineNo": None
        }

        api_response = MoveToData().api("POST", "/api/funnel/resolveTarget", payload, "JSON")

        if api_response.status_code == 200:
            return api_response.json()
        else:
            Logging().error(message=f"Unable to resolve target",
                            debug=f"{api_response.text}", build_id=build_id)

    def post_transform(self, dataset_id, sources, transactionId):
        payload = {
            "target": dataset_id,
            "sources": sources,
            "transactionId": transactionId,
            "branch": REPO_BRANCH,
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH,
            "buildId": BUILD_ID,
            "buildTrigger": BUILD_TRIGGER
        }

        api_response = MoveToData().api("POST", "/api/funnel/postTransform", payload, "JSON")
        if api_response.status_code == 200:
            return True
        else:
            Logging().error(message=f"Unable to perform post transform",
                            debug=f"{api_response.text}")

    def post_transform_notebook(self, dataset_id, branch_to_write, transaction_id, build_id, user_id):
        payload = {
            "target": dataset_id,
            "sources": [],
            "transactionId": transaction_id,
            "branch": branch_to_write,
            "repositoryId": None,
            "scriptPath": None,
            "buildId": build_id,
            "buildTrigger": "NOTEBOOK",
            "userId": user_id
        }

        api_response = MoveToData().api("POST", "/api/funnel/postTransform", payload, "JSON")
        if api_response.status_code == 200:
            return True
        else:
            Logging().error(message=f"Unable to perform post transform",
                            debug=f"{api_response.text}", build_id=build_id)

    def preview_post_transform(self, result, schema, target):
        payload = {
            "schema": schema,
            "data": result,
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH,
            "buildId": BUILD_ID,
            "target": target
        }

        api_response = MoveToData().api("POST", f"/api/funnel/previewPostTransform", payload, "JSON")
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

    def log(self, status, stage, message, build_id = None, debug=None, checkpoint_dataset_id=None, checkpoint_status=None):
        SPARK_APPLICATION_ID = os.environ.get('SPARK_APPLICATION_ID')
        now = datetime.datetime.now()
        formatted_date = now.strftime("%y/%m/%d %H:%M:%S")
        log_entry = f"{formatted_date} {status.upper()} Running: {message} {debug}"

        if build_id is None:
            build_id = BUILD_ID
        for handler in self.handlers:
            handler.emit(log_entry)

        payload = {
            "status": status,
            "stage": stage,
            "message": message,
            "debug": f"{debug}",
            "repositoryId": REPOSITORY_ID,
            "scriptPath": SCRIPT_PATH,
            "checkpointDataset": checkpoint_dataset_id,
            "checkpointStatus": checkpoint_status,
            "sparkApplicationId": SPARK_APPLICATION_ID
        }

        api_response = None

        # if BUILD_TYPE == "PREVIEW" and status == Constants().ERROR:
        #     api_response = MoveToData().api("POST", f"/api/funnel/{PREVIEW_ID}/log", payload, "JSON")
        # elif BUILD_TYPE == "DEFAULT":
        api_response = MoveToData().api("POST", f"/api/build/{build_id}/log", payload, "JSON")

        if api_response is not None and api_response.status_code != 200:
            error_message = f"{formatted_date} ERROR Running: {message} {debug}"
            print(error_message)
            sys.exit(0)

            # for handler in self.handlers:
            #     handler.emit(error_message)
            # raise Exception(error_message)

        if status == Constants().ERROR:
            print(f"{formatted_date} [status]: {message} {debug}")
            sys.exit(0)
            # error_exception = Exception(f"{formatted_date} [status]: {message} {debug}")
            # for handler in self.handlers:
            #     handler.emit_error(error_exception)
            # raise error_exception

    def info(self, message, debug=None, build_id=None):
        self.log(Constants().INFO, Constants().RUNNING, message, build_id, debug)

    def finish(self, message, debug=None, build_id=None):
        self.log(Constants().INFO, Constants().FINISHED, message, build_id, debug)

    def checkpoint(self, dataset_id, status, build_id = None):
        self.log(Constants().INFO, Constants().RUNNING, "", build_id, "", dataset_id, status)

    def error(self, message, debug=None, build_id = None):
        self.log(Constants().ERROR, Constants().FINISHED,  message, build_id, debug)


class Constants:
    # Dataset types
    RAWDATASET: Final = "RAWDATASET"
    BUILDDATASET: Final = "BUILDDATASET"
    LIVEDATASET: Final = "LIVEDATASET"

    CSV: Final = "CSV"
    XLS: Final = "XLS"
    PARQUET: Final = "PARQUET"
    # Stages
    RUNNING: Final = "RUNNING"
    FINISHED: Final = "FINISHED"

    # Status
    INFO: Final = "INFO"
    ERROR: Final = "ERROR"
