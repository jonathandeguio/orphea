# -*- coding: utf-8 -*-

import json
import urllib3

from utils.configurations import PHYSICAL_ENDPOINT, BUILD_TYPE, REPO_BRANCH, ROW_LIMIT, BUILD_ID
from movetodata.functions import _utils

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

from pyspark.sql.types import StructType
import pyspark

from movetodata.internal import Kitab, Build, MoveToData, spark_session, Logging, Constants
import pandas as pd

log = Logging()


class DatasetTransform:

    def __init__(self):
        pass

    def _source(self, dataset_id, transaction_id, branch_type, encoding, schema, live_dataset_config):
        dataset_path = _utils.physical_path(dataset_id, transaction_id)
#         print("BRANCH TYPE")
#         print(branch_type)
#         print(live_dataset_config)
        try:
            if branch_type == Constants().CSV or branch_type == Constants().XLS or branch_type == Constants().RAWDATASET:
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
            elif branch_type == Constants().LIVEDATASET:
                if (live_dataset_config["authType"] is not None and live_dataset_config["authType"] == "KEYPAIR"):
                    df = spark_session().read \
                        .format("jdbc") \
                        .option("url", live_dataset_config["url"]) \
                        .option("query", live_dataset_config["query"]) \
                        .option("user", live_dataset_config["username"]) \
                        .option("privateKey", live_dataset_config["privateKey"]) \
                        .option("privateKeyPassphrase", live_dataset_config["privateKeyPassphrase"]) \
                        .option("driver", live_dataset_config["driver"]) \
                        .load()
                else:
                    df = spark_session().read \
                        .format("jdbc") \
                        .option("url", live_dataset_config["url"]) \
                        .option("query", live_dataset_config["query"]) \
                        .option("user", live_dataset_config["username"]) \
                        .option("password", live_dataset_config["password"]) \
                        .option("driver", live_dataset_config["driver"]) \
                        .load()


            else:
                log.error(f"Not valid dataset type {branch_type}")

        except Exception as error:
            print(error)
            log.error(f"Dataframe Expected in return, got {error}",
                      debug=f"{error}")

        if BUILD_TYPE == "PREVIEW":
            if ROW_LIMIT:
                df = df.limit(int(ROW_LIMIT))

        return df

    def _target(self, target, df_to_write, transaction_id, build_id = None):
        """
        This is for target dataset, resolve target dataset is performed in Run Itself.
        :param path:
        :param df_to_write:
        :param build_id:
        :return:
        """
        # checking if source is a dataframe
        if not isinstance(df_to_write, pyspark.sql.DataFrame):
            if isinstance(df_to_write, pd.DataFrame):
                df_to_write = spark_session().createDataFrame(df_to_write)
            elif df_to_write is None:
                Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH, build_id=build_id)
                log.checkpoint(dataset_id=target, status="FAILED", build_id=build_id)
                log.error(message=f"Please check the code if it is returning dataframe",
                          debug=f"Dataframe Expected in return. This is normally an issue within user code function which is under the decorator.")
            else:
                Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH, build_id=build_id)
                log.checkpoint(dataset_id=target, status="FAILED", build_id=build_id)
                log.error(message=f"Dataframe Expected in return a pyspark.sql.DataFrame, got {df_to_write}",
                          debug=f"{df_to_write}", build_id=build_id)

        try:
            dataset_path = _utils.physical_path(target, transaction_id)
            if dataset_path is False:
                raise Exception("Target dataset not found in catalog")

            df_to_write.write.mode("overwrite").parquet(dataset_path)
        except Exception as e:
            Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH, build_id=build_id)
            log.checkpoint(dataset_id=target, status="FAILED", build_id=build_id)
            log.error(f"Failed to write {e}", build_id=build_id)



    def _sources_df(self, kwargs, sources, sources_transaction_ids, sources_branch_type, sources_encoding,
                    sources_schema, live_dataset_configs):
        sources_map = dict()
        _idx = 0
        for key, source_path_or_id in kwargs.items():
            # checking if source is a dataframe
            try:
                source = self._source(sources[_idx], sources_transaction_ids[_idx], sources_branch_type[_idx],
                                      sources_encoding[_idx], sources_schema[_idx], live_dataset_configs[_idx])
            except:
                source = self._source(sources[_idx], sources_transaction_ids[_idx], sources_branch_type[_idx],
                                      None, None, None)
            if not isinstance(source, pyspark.sql.DataFrame):
                log.error(message=f"Dataframe Expected in return a pyspark.sql.DataFrame, got {source}",
                          debug=f"{source}")

            sources_map[key] = source
            _idx += 1

        return sources_map

    def run(self, user_function, target, kwargs):
        sources_path_or_id = [source_path_or_id for source_path_or_id in kwargs.values()]
        response = Build().pre_transform(sources=sources_path_or_id)
        transaction_id = response["transactionId"]
        target = response["target"]
        # As sources can be either id or path, getting only ids
        sources = response["sources"]

        log.info(f"Processing {target}")

        sources_map = self._sources_df(kwargs, sources, response["sourcesTransactionIds"],
                                       response["sourcesBranchType"], response["sourcesEncoding"],
                                       response["sourcesSchema"], response["liveDatasetConfigs"])

        try:
            target_df = user_function(**sources_map)
        except Exception as error:
            log.error(f"Error in transformation", debug=f"{error}")

        if BUILD_TYPE == "PREVIEW":
            result = target_df.limit(100).toJSON().collect()

            parsed_data = [json.loads(json_string) for json_string in result]
            schema_arr = []
            for field in target_df.schema.fields:
                field_json = {
                    "headerName": field.name,
                    "field": field.name,
                    "type": field.dataType.typeName()  # Use simpleString() for more concise type representation
                }
                schema_arr.append(field_json)

            Build().preview_post_transform(result=parsed_data, schema=schema_arr, target=target)
        elif BUILD_TYPE == "DEFAULT":
            is_created = Kitab().start_transaction(dataset_id=target,
                                                   branch=REPO_BRANCH)

            self._target(target, target_df, transaction_id)

            if is_created:
                Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH)

            Build().post_transform(dataset_id=target, sources=sources, transactionId=transaction_id)

# TODO : below is for notebook which is not working at the moment

# def _get_schema(dataset_id, branch):
#     """
#     Get if schema exists :
#
#         kitab_dataset_schema
#
#     :param dataset_id:
#     :param branch:
#     :return:
#     """
#
#     response = MoveToData().functions(method="GET", endpoint=f"/api/dataset/schema/{dataset_id}/{branch}", payload=None,
#                             payload_type=None)
#
#     if response.status_code == 200:
#         return response.json()
#     else:
#         log.error(f"Error getting schema {dataset_id}", debug=f"{response.text}")


# def ReadDataFrame(path):
#     if not Kitab().dataset_exists(path):
#         log.error(f"Dataset not found in catalog {path}")
#     # else:
#     #     print("dataset found")
#
#     # TODO : need to think about this... whether the data comes from base branch or always from master
#     dataset_path = Kitab().physical_path(path, REPO_BRANCH, "00000000-0000-0000-0000-000000000000")
#
#     try:
#         dataset_id = Kitab().path(path).strip()
#
#         schema = _get_schema(dataset_id=dataset_id, branch=REPO_BRANCH)  # TODO : source branch automatically
#
#         schema_obj = StructType.fromJson(schema['schema'])
#
#         dataset_type = Kitab().branch_type(dataset_id=dataset_id,
#                                            branch=REPO_BRANCH).replace('"', '')  # TODO : source branch automatically
#
#         if dataset_type == Constants().RAWDATASET.toString():
#
#             df = spark_session().read.format("csv") \
#                 .schema(schema_obj) \
#                 .option("TimeStampFormat", "MM/dd/yyyy H:mm") \
#                 .option("dateFormat", "MM/dd/yyyy") \
#                 .option("header", "true") \
#                 .option("sep", schema['customSchema']['fieldDelimiter']) \
#                 .option("delimiter", schema['customSchema']['fieldDelimiter']) \
#                 .option("quote", schema['customSchema']['escapeCharacter']) \
#                 .option("escape", schema['customSchema']['escapeCharacter']) \
#                 .option("ignoreLeadingWhiteSpace", True) \
#                 .option("header", "true") \
#                 .load(dataset_path)
#         elif dataset_type == Constants().BUILDDATASET.toString():
#             df = spark_session().read \
#                 .parquet(dataset_path)
#         else:
#             log.error(f"Not valid dataset type {dataset_type}")
#
#     except Exception as error:
#         log.error(f"Dataframe Expected in return, got {error}", debug=f"{error}")
#
#     return df
#
#
# def WriteDataFrame(df_to_write, path):
#     """
#     This is to write dataframe back to fs.
#
#     :param df_to_write:
#     :param path:
#     :return:
#     """
#
#     branch = REPO_BRANCH
#
#     if Kitab().resolve_target_dataset(path) is False:
#         log.error(f"Not able to create Target dataset in catalog {path}")
#
#     if not Kitab().dataset_exists(path):
#         log.error(f"Target dataset not found in catalog {path}")
#
#     dataset_path = Kitab().physical_path(path, REPO_BRANCH, "00000000-0000-0000-0000-000000000000")
#     target_dataset_id = Kitab().path(path)
#     Kitab().resolve_branch(repository_id=repository, dataset_id=target_dataset_id, branch=branch)
#
#     if dataset_path is not False:
#         dataset_path = f"{dataset_path}/{branch}"
#     else:
#         log.error(f"Target dataset not found in catalog {path}")
#
#     is_created = Kitab().start_transaction(dataset_id=target_dataset_id, branch=branch)
#
#     df_to_write.write.mode("overwrite").parquet(dataset_path)
#
#     # transaction ends here
#     if is_created:
#         Kitab().end_transaction(dataset_id=target_dataset_id, branch=branch)
