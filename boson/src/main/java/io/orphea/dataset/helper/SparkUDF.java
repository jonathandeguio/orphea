package io.orphea.dataset.helper;

import org.apache.spark.sql.expressions.UserDefinedFunction;

public class SparkUDF {
    public String name;
    public UserDefinedFunction function;

    public SparkUDF(String _name, UserDefinedFunction _function) {
        name = _name;
        function = _function;
    }
}
