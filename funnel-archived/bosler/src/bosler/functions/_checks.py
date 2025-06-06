from typing import Optional


# if TYPE_CHECKING:
#     from utils.expectations import Expectation


class Check(object):

    def __init__(
            self,
            expectation: "Expectation",
            name: str,
            on_error: str = "FAIL",
            description: Optional[str] = None,
    ) -> None:
        """
        Arguments:
            expectation (Expectation): The expectation to assess.
            name (str): A stable identifier for the check.
            on_error (str, optional): Action to perform if the expectation is not met. Currently supports 'WARN' or 'FAIL'.
            description (str, optional): Description of the check.
        """
        if not name:
            raise ValueError("All expectation checks must have a name.")

        if not hasattr(expectation, "definition"):
            raise ValueError(
                f"Check {name!r} must wrap an expectation. E.g. 'E.col('a').gt(0)'."
            )

        self.expectation = expectation
        self.name = name
        self.description = description

        if on_error not in ("WARN", "FAIL"):
            raise ValueError("'on_error' must be one of 'WARN' or 'FAIL'")

        self.on_error = on_error
