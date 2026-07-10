import math
import os

from pyspark.sql import types as T
from typing import Any, Dict, Mapping, Type, TypeVar, Iterable, Union
import inspect

from utils.configurations import REPOSITORY_ID, PHYSICAL_ENDPOINT, BACKING_FS

_Cls = TypeVar("_Cls")


def filter_instances(instance_map: Mapping[str, Any], cls: Type[_Cls]) -> Dict[str, _Cls]:
    """Utility function for filtering map entries by the class of the value.

    Args:
        instance_map (dict of obj: obj): Map of instances to filter.
        cls (type): The type to filter for.

    Returns:
        (dict of obj: cls): The filtered dictionary.
    """
    return {k: v for k, v in instance_map.items() if isinstance(v, cls)}


def data_types_are_equal_ignoring_nullability(
        dt1,  # type: T.DataType
        dt2  # type: T.DataType
):
    """
    Compares the given schemas ignoring the nullability of the fields in the comparison.
    This is needed because of breaking changes in default nullability of some elements between spark versions.
    :return: true if the schemas are the same
    """
    # type: (...) -> bool
    if isinstance(dt1, T.ArrayType) and isinstance(dt2, T.ArrayType):
        return data_types_are_equal_ignoring_nullability(dt1.elementType, dt2.elementType)
    if isinstance(dt1, T.MapType) and isinstance(dt2, T.MapType):
        are_key_datatypes_equal = data_types_are_equal_ignoring_nullability(dt1.keyType, dt2.keyType)
        are_value_datatypes_equal = data_types_are_equal_ignoring_nullability(dt1.valueType, dt2.valueType)
        return are_key_datatypes_equal and are_value_datatypes_equal
    if isinstance(dt1, T.StructType) and isinstance(dt2, T.StructType):
        if len(dt1.fieldNames()) != len(dt2.fieldNames()):
            return False
        return all(map(
            lambda fields: data_types_are_equal_ignoring_nullability(*fields),
            zip(dt1.fields, dt2.fields)
        ))
    if isinstance(dt1, T.StructField) and isinstance(dt2, T.StructField):
        return (dt1.name == dt2.name) and data_types_are_equal_ignoring_nullability(dt1.dataType, dt2.dataType)
    return dt1 == dt2


def _get_most_similar_to(word: str, candidates: Iterable[str]) -> Union[str, None]:
    """Get the most similar string to 'word' from a list of candidate words if one exists."""
    if not candidates:
        return None

    best_distance, best_candidate = min(
        (_levenshtein_distance(c, word), c) for c in candidates
    )

    return best_candidate if best_distance < math.ceil(0.8 * len(word)) else None


def _levenshtein_distance(a: str, b: str) -> int:
    """Calculate the Levenshtein (edit) distance between two pieces of text.


    The edit distance is commutative.


    :param a: first string
    :param b: second string
    :return: edit distance between the sources
    """
    distances = [
        [max(i, j) if i == 0 or j == 0 else 0 for j in range(len(b) + 1)]
        for i in range(len(a) + 1)
    ]

    for i in range(1, len(a) + 1):
        for j in range(1, len(b) + 1):
            if a[i - 1] == b[j - 1]:
                # sources share a character at this position
                distances[i][j] = distances[i - 1][j - 1]
            else:
                distances[i][j] = (
                        min(
                            distances[i][j - 1],  # delete from a
                            distances[i - 1][j],  # insert to a
                            distances[i - 1][j - 1],  # replace character in a
                        )
                        + 1
                )  # each operation has a cost of 1

    return distances[-1][-1]


def get_execution_provenance():
    frame = inspect.currentframe()
    while frame:
        if REPOSITORY_ID in inspect.getsourcefile(frame):
            return inspect.getsourcefile(frame), frame.f_lineno
        frame = frame.f_back

    return None, None

def get_physical_endpoint():
    if BACKING_FS == "s3":
        return "s3a://movetodata/dataset"
    elif BACKING_FS == "gs":
        return "gs://" + os.environ.get("GS_BUCKET") + "/movetodata/dataset"
    elif BACKING_FS == "hdfs":
        return os.environ.get("HDFS_ENDPOINT") + "/movetodata/dataset"
    elif BACKING_FS == "localfs":
        return os.environ.get("LOCAL_FS_DIRECTORY") + "/dataset"
    else:
        return None

def physical_path(dataset_id, transaction_id):
    physical_endpoint = PHYSICAL_ENDPOINT
    if physical_endpoint is None:
        physical_endpoint = get_physical_endpoint()
    return f"{physical_endpoint}/{dataset_id}/{transaction_id}"