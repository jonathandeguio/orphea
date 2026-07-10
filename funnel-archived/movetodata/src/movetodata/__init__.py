# -*- coding: utf-8 -*-

import json
import urllib3

from utils.configurations import PHYSICAL_ENDPOINT, BUILD_TYPE, REPO_BRANCH

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

from pyspark.sql.types import StructType
import pyspark

from .internal import Kitab, Build, MoveToData, spark_session, Logging, Constants

log = Logging()

class DatasetTransform:

    def __init__(self):
        pass

    def _source(self, dataset_id, transaction_id, branch_type, encoding, schema):
        dataset_path = self._physical_path(dataset_id, transaction_id)

        try:
            if branch_type == "RAW":
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
            elif branch_type == "PARQUET":
                df = spark_session().read \
                    .parquet(dataset_path)
            else:
                log.error(f"Not valid dataset type {branch_type}")

        except Exception as error:
            log.error(f"Dataframe Expected in return, got {error}",
                      debug=f"{error}")

        if BUILD_TYPE == "PREVIEW":
            df = df.limit(100)

        return df

    def _physical_path(self, datasetId, transactionId):
        return f"{PHYSICAL_ENDPOINT}/{datasetId}/{transactionId}"

    def _target(self, target, df_to_write, transaction_id):
        """
        This is for target dataset, resolve target dataset is performed in Run Itself.
        :param path:
        :param df_to_write:
        :return:
        """

        # checking if source is a dataframe
        if not isinstance(df_to_write, pyspark.sql.DataFrame):
            if df_to_write is None:
                Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH)
                log.error(message=f"Please check the code if it is returning dataframe",
                          debug=f"Dataframe Expected in return. This is normally an issue within user code function which is under the decorator.")
            else:
                Kitab().end_transaction(dataset_id=target, branch=REPO_BRANCH)
                log.error(message=f"Dataframe Expected in return a pyspark.sql.DataFrame, got {df_to_write}",
                          debug=f"{df_to_write}")

        dataset_path = self._physical_path(target, transaction_id)

        if dataset_path is False:
            log.error(f"Target dataset not found in catalog {dataset_path}")

        df_to_write.write.mode("overwrite").parquet(dataset_path)

    def _sources_df(self, kwargs, sources, sources_transaction_ids, sources_branch_type, sources_encoding,
                    sources_schema):
        sources_map = dict()
        _idx = 0
        for key, source_path_or_id in kwargs.items():
            # checking if source is a dataframe
            try:
                source = self._source(sources[_idx], sources_transaction_ids[_idx], sources_branch_type[_idx],
                                      sources_encoding[_idx], sources_schema[_idx])
            except:
                source = self._source(sources[_idx], sources_transaction_ids[_idx], sources_branch_type[_idx],
                                      None, None)
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
                                       response["sourcesSchema"])

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