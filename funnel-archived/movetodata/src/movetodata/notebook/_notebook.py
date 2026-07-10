
import os
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

from pyspark.sql.types import StructType
import pyspark

from movetodata.internal import Kitab, Build, spark_session, Logging

repository = os.getenv('REPOSITORY_ID')
branch = os.getenv('BRANCH')
TRANSACTION_ID = os.environ.get("TRANSACTION_ID")
PHYSICAL_ENDPOINT = os.environ.get("PHYSICAL_ENDPOINT")
BUILD_TYPE = os.environ.get("BUILD_TYPE")

log = Logging()


def physical_path(physical_endpoint, datasetId, transactionId):
    return f"{physical_endpoint}/{datasetId}/{transactionId}"

def _source(physical_endpoint, dataset_id, transaction_id, branch_type, encoding, schema):
        dataset_path = physical_path(physical_endpoint, dataset_id, transaction_id)

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
            df = df.limit(100000)

        return df

def sources_df(kwargs, sources, sources_transaction_ids, sources_branch_type, sources_encoding,
                sources_schema, physical_endpoint):
        sources_map = dict()
        _idx = 0
        for key, source_path_or_id in kwargs.items():
            # checking if source is a dataframe
            if sources_branch_type[_idx] == "PARQUET":
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

def ReadDataFrame(dataset):

    sources_path_or_id = [dataset]

    response = Build().pre_transform(sources=sources_path_or_id) # this is a random UUID : c121c6c3-00ed-4a2d-b7a8-c9d60595cd43 , it is not required to have target UUID for notebook
    # As sources can be either id or path, getting only ids
    sources = response["sources"]
    physical_endpoint = response["physicalEndpoint"]

    sources_map = sources_df({"readDataset": dataset}, sources, response["sourcesTransactionIds"],
                                   response["sourcesBranchType"], response["sourcesEncoding"],
                                   response["sourcesSchema"], physical_endpoint)

    df = sources_map["readDataset"]

    return df


def WriteDataFrame(df_to_write, path):
    """
    This is to write dataframe back to fs.

    :param df_to_write:
    :param path:
    :return:
    """

    branch = "master"

    if Kitab().resolve_target_dataset(path) is False:
        log.error(f"Not able to create Target dataset in catalog {path}")

    if not Kitab().dataset_exists(path):
        log.error(f"Target dataset not found in catalog {path}")

    dataset_path = Kitab().physical_path(path, "master", "00000000-0000-0000-0000-000000000000")
    target_dataset_id = Kitab().path(path)
    Kitab().resolve_branch(repository_id=repository, dataset_id=target_dataset_id, branch=branch)

    if dataset_path is not False:
        dataset_path = f"{dataset_path}/{branch}"
    else:
        log.error(f"Target dataset not found in catalog {path}")

    is_created = Kitab().start_transaction(dataset_id=target_dataset_id, branch=branch)

    df_to_write.write.mode("overwrite").parquet(dataset_path)

    # transaction ends here
    if is_created:
        Kitab().end_transaction(dataset_id=target_dataset_id, branch=branch)