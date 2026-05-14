package io.orphea.dataset.library.chartfactory;

import io.orphea.dataset.library.DTOs.ChartResponse;
import io.orphea.dataset.requests.ChartDataRequest;
import io.orphea.dataset.requests.ChartSeriesRequest;
import io.orphea.passport.util.AuthUtils;
import io.orphea.passport.util.CommonUtils;
import io.orphea.sharedutils.language.labels;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.shaded.org.eclipse.jetty.util.ajax.JSON;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.collection.JavaConverters;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

public interface IChartService {
    ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception;

    String getChartServiceType();

    default JSONObject getColumnStats(Dataset<Row> resultSet, List<String> cols) {
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (String columnName : cols) {
            List<Column> colList = new ArrayList<>();
            colList.add(min(columnName));

            Column[] columns = colList.toArray(new Column[0]);
            Row maxRow = resultSet.agg(max(columnName), columns).first();
            Object maxValue = maxRow.get(0);
            Object minValue = maxRow.get(1);

            Map<String, Object> stat = new HashMap<>();
            stat.put("max", maxValue);
            stat.put("min", minValue);

            result.put(columnName, stat);
        }

        return new JSONObject(result);
    }

    default JSONArray convertToTabularData(Dataset<Row> sparkData) {
        JSONArray resultantDataArray = new JSONArray();

        for (Row r : sparkData.collectAsList()) {
            resultantDataArray.add(JSON.parse(r.json()));
        }
        return resultantDataArray;
    }

    default HashMap<String, ArrayList<Object>> convertToDimensionData(Dataset<Row> resultSet, ChartDataRequest chartDataRequest, boolean includeDimensions) {
        HashMap<String, ArrayList<Object>> result = new HashMap<>();

        List<String> aggCols = chartDataRequest.getSeries().stream().map(ser -> generateColumnName(ser.getColumnName(), ser.getAggregate())).collect(Collectors.toList());

        String colName = String.join(",", aggCols);

        for (Row row : resultSet.collectAsList()) {
            scala.collection.Map<String, Object> aggValMap = row.getValuesMap(JavaConverters.asScalaIteratorConverter(aggCols.iterator()).asScala().toSeq());
            scala.collection.Iterator<Object> aggIterator = aggValMap.valuesIterator();

            ArrayList<Object> sub = new ArrayList<>();

            while (aggIterator.hasNext()) {
                sub.add(aggIterator.next());
            }

            if (!chartDataRequest.getDimensions().isEmpty() && includeDimensions) {
                scala.collection.Map<String, Object> mp = row.getValuesMap(JavaConverters.asScalaIteratorConverter(chartDataRequest.getDimensions().iterator()).asScala().toSeq());
                scala.collection.Iterator<Object> iterator = mp.valuesIterator();

                StringBuilder builder = new StringBuilder(String.valueOf(iterator.next()));
                while (iterator.hasNext()) {
                    builder.append(", ").append(iterator.next());
                }
                result.put(builder.toString(), sub);
            } else {
                result.put(colName, sub);
            }
        }

        return result;
    }

    default HashMap<String, ArrayList<ArrayList<Object>>> convertToAxisData(Dataset<Row> resultSet, ChartDataRequest chartDataRequest, ChartSeriesRequest seriesRequest, List<String> dimensions) throws Exception {
        String currentColumn = generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate());

        HashMap<String, ArrayList<ArrayList<Object>>> result = new HashMap<>();

        for (Row row : resultSet.collectAsList()) {
            ArrayList<Object> sub = new ArrayList<>();

            if (chartDataRequest.getXAxis() != null) {
                Object value1 = row.get(resultSet.schema().fieldIndex(chartDataRequest.getXAxis()));
                if (value1 instanceof Double && ((Double) value1 % 1) == 0) {
                    value1 = ((Double) value1).intValue();
                }
                sub.add(value1 == null ? "null" : String.valueOf(value1));
            }

            Object value2 = row.getAs(currentColumn);
            sub.add(value2 == null ? 0 : value2);

            if (CollectionUtils.isEmpty(dimensions) && CollectionUtils.isEmpty(seriesRequest.getGroupBy())) {
                result.computeIfAbsent(currentColumn, k -> new ArrayList<>());
                result.get(currentColumn).add(sub);
            } else {
                List<String> gByList = new ArrayList<>();
                gByList.addAll(CollectionUtils.isNotEmpty(seriesRequest.getGroupBy()) ? seriesRequest.getGroupBy() : new ArrayList<>());
                gByList.addAll(CollectionUtils.isNotEmpty(dimensions) ? dimensions : new ArrayList<>());

                scala.collection.Map<String, Object> mp = row.getValuesMap(JavaConverters.asScalaIteratorConverter(gByList.iterator()).asScala().toSeq());
                scala.collection.Iterator<Object> iterator = mp.valuesIterator();

                StringBuilder builder = new StringBuilder(String.valueOf(iterator.next()));
                while (iterator.hasNext()) {
                    builder.append(", ").append(iterator.next());
                }

                result.computeIfAbsent(builder.toString(), k -> new ArrayList<>());
                result.get(builder.toString()).add(sub);
            }
        }

        return result;
    }

    default Dataset<Row> performAggregate(RelationalGroupedDataset groupedData, List<ChartSeriesRequest> seriesRequests) {
        List<Column> cols = seriesRequests.stream().filter(sr -> !"none".equals(sr.getAggregate()) && Objects.nonNull(sr.getColumnName())).map(sr -> getAggregate(sr.getColumnName(), sr.getAggregate())).collect(Collectors.toList());

        if (cols.size() == 0) {
            return groupedData.df();
        } else if (cols.size() == 1) {
            return groupedData.agg(cols.get(0));
        } else {
            return groupedData.agg(cols.get(0), cols.subList(1, cols.size()).toArray(new Column[0]));
        }
    }

    default Column getAggregate(String columnName, String aggregateFunction) {
        switch (aggregateFunction) {
            case "first":
                return first(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "sum":
                return sum(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "avg":
                return avg(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "max":
                return max(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "min":
                return min(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "count":
                return count(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "approx_count_distinct":
                return approx_count_distinct(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "stddev":
                return stddev(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "stddev_samp":
                return stddev_samp(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "stddev_pop":
                return stddev_pop(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "variance":
                return variance(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "var_samp":
                return var_samp(columnName).as(generateColumnName(columnName, aggregateFunction));
            case "var_pop":
                return var_pop(columnName).as(generateColumnName(columnName, aggregateFunction));
            // Add more cases for other aggregate functions as needed
            default:
                throw new IllegalArgumentException("Unknown aggregate function: " + aggregateFunction);
        }
    }

    default String generateColumnName(String column, String aggregate) {
        String userLang = AuthUtils.getCurrentUser().getLanguage();

        String aggregateL = labels.getLabel(aggregate, userLang);
        if (aggregateL == null) {
            aggregateL = aggregate;
        }

        if (column != null && aggregate != null) {
            if (aggregate.equals("none"))
                return column;

            return String.format("%s(%s)", aggregateL, column);
        }
        return null;
    }

    default Dataset<Row> sortDf(Dataset<Row> dataset, boolean xAxisIsTimeColumn, ChartDataRequest chartDataRequest) {
        String sortingMethod = chartDataRequest.getSortingMethod();
        String sortingDirection = chartDataRequest.getSortingDirection();
        String columnName = chartDataRequest.getCustomColumnName();
        String aggregateFunction = chartDataRequest.getCustomAggregateFunction();

        Dataset<Row> sortedDataset = dataset;

        if(sortingMethod.equals("xaxis")) {
            if (Objects.nonNull(chartDataRequest.getXAxis())) {
                sortedDataset = dataset.orderBy(sortingDirection.equals("desc") ? col(chartDataRequest.getXAxis()).desc() : col(chartDataRequest.getXAxis()));

                if (xAxisIsTimeColumn) {
                    Column sortCol = col(generateColumnName(chartDataRequest.getXAxis()+"timestamp", "first"));
                    sortedDataset = sortedDataset.orderBy(sortingDirection.equals("desc") ? sortCol.desc() : sortCol);
                } else {
                    sortedDataset = sortedDataset.orderBy(sortingDirection.equals("desc") ? col(chartDataRequest.getXAxis()).desc() : col(chartDataRequest.getXAxis()));
                }
            }
        } else {
            if(CommonUtils.isValidUUID(sortingMethod)) {
                Optional<ChartSeriesRequest> chartSeriesRequest = chartDataRequest.getSeries().stream().filter(csr -> csr.getId().toString().equals(sortingMethod)).findFirst();
                if(chartSeriesRequest.isPresent()) {
                    columnName = chartSeriesRequest.get().getColumnName();
                    aggregateFunction = chartSeriesRequest.get().getAggregate();
                }
            }

            Column sortedByCol = sortingDirection.equals("asc") ? col(generateColumnName(columnName, aggregateFunction)).asc() : col(generateColumnName(columnName, aggregateFunction)).desc();
            sortedDataset = dataset.orderBy(sortedByCol);
        }

        return sortedDataset;
    }

}
