from movetodata.functions import _utils
from movetodata.functions._dataset import Source, Target
from movetodata.functions._transform import Transform
from typing import Any, Callable, Union


def funnel(**kwargs: Union[Source, Target, Any]):
    """This package is platform internal package which deals with Path names and build for spark submissions.

    The "funnel" decorator was utilized for constructing a :class:`Transform` object from a compute function.
    In this process, the names assigned to sources, targets, or other parameters were expected to align with the 
    function arguments of the wrapped compute function. During the compute-time, these parameters were instantiated 
    into specific objects as defined by each parameter type.

        >>> @funnel(
        ... var1="test",
        ...     first_source=Source('/path/to/first/source/dataset'),
        ...     second_source=Source('/path/to/second/source/dataset'),
        ...     first_target=Target('/path/to/first/target/dataset'),
        ...     second_target=Target('/path/to/second/target/dataset'),
        ... )
        ... def my_compute_function(first_source, second_source, first_target, second_target):
        ...     # type: (TransformSource, TransformSource, TransformTarget, TransformTarget) -> None
        ...     first_target.write_dataframe(first_source.dataframe())
        ...     second_target.write_dataframe(second_source.dataframe())

    Args:

        **kwargs (Param): kwargs comprised of named :class:`Param` or subclasses.

    Note:

        The compute function is responsible for writing data to its targets.
    """

    def _transform(compute_func: Callable[..., Any]):
        sources = _utils.filter_instances(kwargs, Source)
        targets = _utils.filter_instances(kwargs, Target)
        try:
            transform = Transform(
                compute_func,
                sources=sources,
                targets=targets,
                parameters=kwargs
            )

            transform.preTransform()
            transform.run(**kwargs)
            transform.postTransform()

            return transform
        except Exception as e:
            print(e)

    return _transform
