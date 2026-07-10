# pylint: disable=cyclic-import,  import-outside-toplevel
import inspect
from movetodata.internal import Build, Logging
from movetodata.functions import _utils
from movetodata.functions._checks import Check
from movetodata.functions._dataset import Source, Target
from movetodata.functions._param import Param
from movetodata.functions._utils import _get_most_similar_to
from typing import (
    TYPE_CHECKING,
    Any,
    Callable,
    Dict,
    Iterable,
    List,
    Optional,
    Union,
)

if TYPE_CHECKING:
    pass

log = Logging()


class Transform(object):
    """A Transform embodies a single computational step, encapsulating parameters that inherit from the :class:`Param` class alongside a compute function.

    Creating a Transform object is conventionally achieved through the use of designated decorators, such as :func:`funnel`.

    It's worth noting that the original compute function can be accessed via the Transform's ``__call__`` method.
    """

    def __init__(
            self,
            compute_func: Callable[..., Any],
            sources: Optional[Dict[str, Source]] = None,
            targets: Optional[Dict[str, Target]] = None,
            parameters: Optional[Dict[str, Param]] = None,
    ) -> None:
        """
        Args:
            compute_func (Callable): The compute function to wrap.
            sources (Dict[str, Source]): A dictionary mapping source names to :class:`Source` specs.
            targets (Dict[str, Target]): A dictionary mapping target names to :class:`Target` specs.
            parameters (Dict[str, Param]): A dictionary mapping parameter names to :class:`Param` specs or subclass.
        """
        # Wrap the compute function and store its properties
        self._compute_func = compute_func
        self.__name__ = compute_func.__name__
        self.__module__ = compute_func.__module__
        self.__doc__ = compute_func.__doc__

        self.sources = sources or {}
        self.targets = targets or {}
        self.parameters = parameters or {}

        # Used to create string representation
        self._clean_reference = _format_reference(compute_func)
        self._target_aliases: Union[List[str], None] = None
        self._reference = self._clean_reference

        self._argspec = inspect.getfullargspec(compute_func)
        self.validate()
        self.filename, self.lineno = _utils.get_execution_provenance()

    def preTransform(self):
        sources_path_or_id = [{"source": source.alias, "branch": source.branch} for source in self.sources.values()]
        target_path_or_id = [target.alias for target in self.targets.values()]
        if len(sources_path_or_id) == 0:
            return

        response = Build().pre_transform(sources=sources_path_or_id)
        _index = 0
        for source in self.sources.values():
            transaction_id = response["sourcesTransactionIds"][_index]
            branch_type = response["sourcesBranchType"][_index]
            encoding = response["sourcesEncoding"][_index]
            source_schema = response["sourcesSchema"][_index]
            dataset_id = response["sources"][_index]["source"]
            live_dataset_config = response["liveDatasetConfigs"][_index]
            source.update_properties_after_pre_transform(dataset_id, transaction_id, source_schema, encoding,
                                                         branch_type, live_dataset_config)
            _index += 1

        _targetIndex = 0
        for target in self.targets.values():
            target.update_transform_related_properties(response["sources"], self.filename, self.lineno)

            _targetIndex += 1

    def postTransform(self):
        log.finish("Finished execution of script")

    def validate(self) -> None:
        """Validations to be executed during checks time.

        These won't be executed during runtime, to improve performance and satisfy
        backwards compatibility during runtime upgrades.
        """
        if "ctx" in self.parameters:
            raise ValueError(
                f"'ctx' is a reserved parameter name, cannot use it as a parameter for {self}"
            )

        self._assert_parameter_types_are_correct()
        self._check_sources_are_present_in_parameters()
        self._check_targets_are_present_in_parameters()

    def _assert_parameter_types_are_correct(self) -> None:
        for name, tsource in self.sources.items():
            if not isinstance(tsource, Source):
                raise ValueError(
                    f"Source {name!r} to transform {self} is not a transforms.api.Source"
                )
        for name, ttarget in self.targets.items():
            if not isinstance(ttarget, Target):
                raise ValueError(
                    f"Target {name!r} of transform {self} is not a transforms.api.Target"
                )
        # for name, param in self.parameters.items():
        #     if not isinstance(param, Param):
        #         raise ValueError(
        #             f"Parameter {name!r} of transform {self} is not a utils.functions.Param"
        #         )
        #     if param.json_value is None:
        #         self._bound_transform = False

    def _check_sources_are_present_in_parameters(self) -> None:
        self._check_expected_params_are_present("source", self.sources.keys())

    def _check_targets_are_present_in_parameters(self) -> None:
        self._check_expected_params_are_present("target", self.targets.keys())

    def _check_expected_params_are_present(
            self, params_designator: str, expected_params: Iterable[str]
    ) -> None:
        """
        Args:

        - `params_designator: Literal["source", "target"]`
        - `expected_params: Iterable[str]` - names of params which should be present
        """

        io_params = self.sources.keys() | self.targets.keys()
        unused_fn_params = set(self._argspec.args) - io_params

        for param in expected_params:
            if self._argspec.varkw is None and param not in self._argspec.args:
                error_message = (
                    f"{params_designator.capitalize()} '{param}' "
                    f"to transform {self._clean_reference} doesn't have a "
                    "corresponding parameter in the function's parameter "
                    f"list: {self._argspec.args}"
                )

                most_similar_fn_param = _get_most_similar_to(param, unused_fn_params)

                if not most_similar_fn_param and len(unused_fn_params) == 1:
                    most_similar_fn_param = unused_fn_params.pop()

                if most_similar_fn_param:
                    error_message += (
                        f"\n\nDid you mean to write '{most_similar_fn_param}' "
                        f"instead of '{param}'?"
                    )
                # TODO(bhagesh) uncomment below
                # raise _errors.NoCorrespondingFunctionParameter(error_message)

    def run(self, **kwargs: Any) -> Any:
        """Passthrough call to the underlying compute function."""
        try:
            return self._compute_func(**kwargs)
        except Exception as e:
            # setattr(e, "__transform_compute_error", True)
            raise

    def __repr__(self) -> str:
        # Format here is subject to change
        return f"Transform({self._clean_reference})<{', '.join(self._target_aliases or [])}>"

    @staticmethod
    def _get_checks_in_items(items: List[Union[Source, Target]]) -> List[Check]:
        """Get `Check` objects across `items`

        Returns:
            List[Check]: list of checks across all sources and targets
        """

        all_checks: List[Check] = []

        for item in items:
            checks = item.checks

            if not checks:
                continue

            all_checks.extend(checks)

        return all_checks

    @property
    def reference(self) -> str:
        """Reference to this transform, unique in pipeline.

        .. versionadded:: 1.53.0
        """
        return self._reference


def _format_reference(compute_function: Callable[..., Any]) -> str:
    return f"{compute_function.__module__}:{compute_function.__name__}"
