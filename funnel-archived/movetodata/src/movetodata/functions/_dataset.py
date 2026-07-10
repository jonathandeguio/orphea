import json

from movetodata import DatasetTransform, Build, Kitab
from movetodata.functions._param import MoveToDataSourceParam, MoveToDataTargetParam
from pyspark.sql import DataFrame
from typing import Optional

from utils.configurations import BUILD_TYPE, REPO_BRANCH


class Source(MoveToDataSourceParam):

    def __init__(
            self,
            alias: Optional[str] = None,
            branch: Optional[str] = None,
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

    def update_properties_after_pre_transform(self, dataset_id, transaction_id, source_schema, encoding, branch_type):
        self.dataset_id = dataset_id
        self.transaction_id = transaction_id
        self.source_schema = source_schema
        self.encoding = encoding
        self.branch_type = branch_type
        self.df = DatasetTransform()._source(self.dataset_id, self.transaction_id, self.branch_type, self.encoding,
                                             self.source_schema)

    def dataframe(self):
        return self.df


class Target(MoveToDataTargetParam):
    def __init__(
            self,
            alias: Optional[str] = None,
            # sever_permissions: bool = False,
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
        # self.sever_permissions = sever_permissions
        self.transaction_id = None
        self._written = False
        # self.checks = _as_list(checks)

    def update_properties_after_pre_transform(self, dataset_id, transaction_id):
        self.dataset_id = dataset_id
        self.transaction_id = transaction_id

    def write_dataframe(self, target_df: DataFrame) -> None:
        if self._written:
            # Throw error here
            return

        self._written = True

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

            Build().preview_post_transform(result=parsed_data, schema=schema_arr, target=self.dataset_id)
        elif BUILD_TYPE == "DEFAULT":
            is_created = Kitab().start_transaction(dataset_id=self.dataset_id,
                                                   branch=REPO_BRANCH)

            DatasetTransform()._target(self.dataset_id, target_df, self.transaction_id)

            if is_created:
                Kitab().end_transaction(dataset_id=self.dataset_id, branch=REPO_BRANCH)

            # Build().post_transform(dataset_id=self.dataset_id, sources=sources, transactionId=transaction_id)
