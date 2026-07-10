import json

from movetodata.internal import Build, Kitab, spark_session
from movetodata.functions._dataset_transform import DatasetTransform, log
from movetodata.functions._param import MoveToDataSourceParam, MoveToDataTargetParam
from pyspark.sql import DataFrame
from typing import Optional, List, Dict
import pandas as pd

from utils.configurations import BUILD_TYPE, REPO_BRANCH, FILE_NAME, LINE_NO, DEFAULT_BRANCH


class Source(MoveToDataSourceParam):

    def __init__(
            self,
            alias: Optional[str] = None,
            branch: Optional[str] = DEFAULT_BRANCH,
            description: Optional[str] = None,
            # checks: Union[Check, List[Check], None] = None,
    ) -> None:
        """
        The Source class should be used to pass a single source into the decorator.

        Args:
            alias:
            branch:
            description:
        """

        super().__init__([alias] if alias else [], branch=branch, description=description)
        self.alias = alias
        self.branch = branch

        self.dataset_id = None
        self.transaction_id = None
        self.source_schema = None
        self.encoding = None
        self.branch_type = None
        self.df = None
        self.live_dataset_config = None

    def update_properties_after_pre_transform(self, dataset_id, transaction_id, source_schema, encoding, branch_type, live_dataset_config):
        self.dataset_id = dataset_id
        self.transaction_id = transaction_id
        self.source_schema = source_schema
        self.encoding = encoding
        self.branch_type = branch_type
        self.live_dataset_config = live_dataset_config
        self.df = DatasetTransform()._source(self.dataset_id, self.transaction_id, self.branch_type, self.encoding,
                                             self.source_schema, self.live_dataset_config)

    def dataframe(self):
        return self.df

    def pandas(self):
        return self.df.toPandas()


class Target(MoveToDataTargetParam):
    def __init__(
            self,
            alias: Optional[str] = None,
#             sever_permissions: bool = False,
            description: Optional[str] = None,
            # checks: Union[Check, List[Check], None] = None,
    ) -> None:
        """
        The Target class should be used to pass a single target into the decorator.

        Args:
            alias:
            description:
        """

        super().__init__([alias] if alias else [], description=description)
        self.alias = alias
        self.dataset_id = None
#         self.sever_permissions = sever_permissions
        self.transaction_id = None
        self._written = False

        self.sources: List[Dict[str, str]] = []
        self.filename = None
        self.lineno = None
        # self.checks = _as_list(checks)

    def update_properties_after_pre_transform(self, dataset_id, transaction_id):
        self.dataset_id = dataset_id
        self.transaction_id = transaction_id

    def update_transform_related_properties(self, sources, filename, lineno):
        self.sources = sources
        self.filename = filename
        self.lineno = lineno

    def write_dataframe(self, target_df: DataFrame) -> None:
        if self._written:
            # Throw error here
            return

        # Check for single dataset build case
        if FILE_NAME and LINE_NO and FILE_NAME == self.filename and LINE_NO == self.lineno:
            return

        try:
            if isinstance(target_df, pd.DataFrame):
                target_df = spark_session().createDataFrame(target_df)
        except Exception as e:
            raise Exception(f"Failed to convert pandas df to spark df: {e}")

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

            Build().preview_post_transform(result=parsed_data, schema=schema_arr, target=self.alias)
        elif BUILD_TYPE == "DEFAULT":
            response = Build().resolve_target(target=self.alias, sources=self.sources, filename=self.filename, lineno=self.lineno)
            self.update_properties_after_pre_transform(response["target"], response["transactionId"])
            is_created = Kitab().start_transaction(dataset_id=self.dataset_id,
                                                   branch=REPO_BRANCH)
            log.info(message="Writing to dataset " + self.dataset_id)
            DatasetTransform()._target(self.dataset_id, target_df, self.transaction_id)

            if is_created:
                Kitab().end_transaction(dataset_id=self.dataset_id, branch=REPO_BRANCH)

            Build().post_transform(dataset_id=self.dataset_id, sources=self.sources, transactionId=self.transaction_id)
            self._written = True
