import json
import os
import uuid
import re

import pandas as pd
import urllib3

from bosler.functions._dataset_transform import DatasetTransform
from utils.configurations import REPO_BRANCH, DEFAULT_BRANCH, BUILD_ID

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

from pyspark.sql.types import StructType
import pyspark

from bosler.internal.internal import Kitab, Build, spark_session, Logging, Constants

repository = os.getenv('REPOSITORY_ID')
branch = os.getenv('BRANCH')
TRANSACTION_ID = os.environ.get("TRANSACTION_ID")
PHYSICAL_ENDPOINT = os.environ.get("PHYSICAL_ENDPOINT")
BUILD_TYPE = os.environ.get("BUILD_TYPE")

log = Logging()


def physical_path(physical_endpoint, datasetId, transactionId):
    return f"{physical_endpoint}/{datasetId}/{transactionId}"

def _source(physical_endpoint, dataset_id, transaction_id, branch_type, encoding, schema):
        dataset_path = physical_path(physical_endpoint, dataset_id['source'], transaction_id)

        try:
            if branch_type == Constants().RAWDATASET or branch_type == Constants().CSV or branch_type == Constants().XLS:
                schema_obj = StructType.fromJson(schema['schema'])

                df = spark_session().read.format("csv") \
                    .schema(schema_obj) \
                    .option("header", "true") \
                    .option("sep", schema['customSchema']['fieldDelimiter']) \
                    .option("delimiter", schema['customSchema']['fieldDelimiter']) \
                    .option("quote", schema['customSchema']['escapeCharacter']) \
                    .option("escape", schema['customSchema']['escapeCharacter']) \
                    .option("ignoreLeadingWhiteSpace", True) \
                    .option("header", "true") \
                    .option("encoding", encoding) \
                    .load(dataset_path)
            elif branch_type == Constants().PARQUET or branch_type == Constants().BUILDDATASET:
                df = spark_session().read \
                    .parquet(dataset_path)
            else:
                log.error(f"Not valid dataset type {branch_type}")

        except Exception as error:
            log.error(f"Dataframe Expected in return, got {error}",
                      debug=f"{error}")

        if BUILD_TYPE == "PREVIEW":
            df = df.limit(100000)

        return df

def sources_df(kwargs, sources, sources_transaction_ids, sources_branch_type, sources_encoding,
                sources_schema, physical_endpoint):
        sources_map = dict()
        _idx = 0
        for key, source_path_or_id in kwargs.items():
            # checking if source is a dataframe
            if sources_branch_type[_idx] == Constants().BUILDDATASET or sources_branch_type[_idx] == Constants().PARQUET:
                source = _source(physical_endpoint, sources[_idx], sources_transaction_ids[_idx], sources_branch_type[_idx],
                                      None, None)
            else:
                source = _source(physical_endpoint, sources[_idx], sources_transaction_ids[_idx],
                                 sources_branch_type[_idx],
                                 sources_encoding[_idx], sources_schema[_idx])

            if not isinstance(source, pyspark.sql.DataFrame):
                log.error(message=f"Dataframe Expected in return a pyspark.sql.DataFrame, got {source}",
                          debug=f"{source}")

            sources_map[key] = source
            _idx += 1

        return sources_map

def get_user_id_from_url(url):
    # Pattern to match '/api/jupyter/lab/tree/{userId}/{repoId}' with possible additional segments
    path_parts = url.strip('/').split('/')
    if len(path_parts) >= 4:
        return path_parts[4]
    return None


def ReadDataFrame(dataset, branch = DEFAULT_BRANCH):

    sources_path_or_id = [{"source": dataset, "branch": branch}]

    response = Build().pre_transform(sources=sources_path_or_id)
    # As sources can be either id or path, getting only ids
    sources = response["sources"]
    physical_endpoint = response["physicalEndpoint"]

    sources_map = sources_df({"readDataset": dataset}, sources, response["sourcesTransactionIds"],
                                   response["sourcesBranchType"], response["sourcesEncoding"],
                                   response["sourcesSchema"], physical_endpoint)

    df = sources_map["readDataset"]

    return df


def WriteDataFrame(target_df, path, branch_to_write = "master"):
    """
    This is to write dataframe back to fs.

    :param target_df: dataframe to write
    :param path: path or id of the dataset to write
    :param branch_to_write: branch of dataset to write the dataset, default is master
    :return:
    """
    build_id = str(uuid.uuid4())
    user_id = get_user_id_from_url(os.environ["JPY_SESSION_NAME"])
    Build().initiate_notebook_build(build_id=build_id, user_id=user_id, branch=branch_to_write)
    log.info(message="Transaction Started", build_id=build_id)

    try:
        if isinstance(target_df, pd.DataFrame):
            target_df = spark_session().createDataFrame(target_df)
    except Exception as e:
        raise Exception(f"Failed to convert pandas df to spark df: {e}")
    response = Build().resolve_target_notebook(target=path, branch_to_write=branch_to_write, build_id=build_id, user_id=user_id)
    dataset_id, transaction_id = response["target"], response["transactionId"]
    is_created = Kitab().start_transaction(dataset_id=dataset_id,
                                               branch=REPO_BRANCH, build_id=build_id)
    DatasetTransform()._target(dataset_id, target_df, transaction_id, build_id)
    if is_created:
        log.info(message="Transaction Completed", build_id=build_id)
        Kitab().end_transaction(dataset_id=dataset_id, branch=REPO_BRANCH)
    log.info(message="Writing to dataset " + dataset_id, build_id=build_id)
    log.info(message="Performing post transforms", build_id=build_id)
    Build().post_transform_notebook(dataset_id=dataset_id, branch_to_write=branch_to_write, transaction_id=transaction_id, build_id=build_id, user_id=user_id)
    # In case of PYTHON/SQL, build is finished from shyneEntrypoint, but for notebook that wont come, so finish Build here
    log.finish(message="Build finished", build_id=build_id)
