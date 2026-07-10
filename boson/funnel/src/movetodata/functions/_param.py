from utils import _json_schema
from typing import Any, Dict, List, Optional


class Param(object):

    def __init__(self, description: Optional[str] = None):
        """Base class for any parameter taken by transform compute function.

        Args:
            description (str, optional): Parameter description to be added in json schema.

        .. versionadded:: 1.53.0
        """
        self._schema: Dict[str, Any] = _json_schema.base()
        self._schema.update({'description': description} if description else {})

    @property
    def schema(self):
        """Returns JSON schema for parameter of this type. Must return valid JSON schema."""
        return self._schema

    @property
    def json_value(self) -> Any:
        """Returns JSON value for this parameter to put in jobspec.

        If return value is None, parameter is considered unbound. If any transform's parameter is unbound,
        transform is considered to be unbound. For unbound utils jobspec is not published.
        """
        return None


class MoveToDataSourceParam(Param):
    # pylint: disable=redefined-builtin
    def __init__(
            self,
            aliases: List[str],
            branch: Optional[str] = None,
            description: Optional[str] = None,
    ) -> None:
        
        super().__init__(description)
        self._aliases = aliases
        self._branch = branch


class MoveToDataTargetParam(Param):

    # pylint: disable=redefined-builtin
    def __init__(
            self,
            aliases: List[str],
            description: Optional[str] = None,
    ) -> None:
        super().__init__(description)
        self._aliases = aliases
        self._type = type
