//package io.bosler.capture.logger.library.services.metricService;
//
//import io.bosler.capture.logger.library.models.MetricsModel;
//import io.bosler.capture.logger.library.services.LoggerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class SystemSwapMetricLogsService {
//    private static final String LOG_FILE_PATH = "logs/boson/metrics/swap/swap_metrics.log";
//    private static final String MARKER_FILE_PATH = "logs/boson/metrics/swap/marker.txt";
//
//    @Value("${log.api.metric.swapMetricsEndpoint}")
//    private String logApiEndpoint;
//    private static final String METRIC_TYPE = "boson.metric.swap";
//    @Autowired
//    private final LoggerService loggerService;
//
//    @Scheduled(fixedRate = 30000)
//    public void readLogFile() throws IOException {
//        long latestTimestamp = 0;
//        String latestTimestampStr = loggerService.getLatestTimestampFromAPI(METRIC_TYPE);
//        if (latestTimestampStr != null && !latestTimestampStr.trim().isEmpty()) {
//            // Check if the string is not "null" and is a valid number
//            if (!"null".equalsIgnoreCase(latestTimestampStr)) {
//                latestTimestamp = Long.parseLong(latestTimestampStr);
//            }
//        }
//
//        long lastReadPosition = loggerService.getLastReadPosition(MARKER_FILE_PATH);
//
//        List<Object> logBatch = new ArrayList<>();
//        Path logPath = Paths.get(LOG_FILE_PATH);
//
//        if (Files.exists(logPath)) {
//            try (RandomAccessFile logFile = new RandomAccessFile(LOG_FILE_PATH, "r")) {
//                logFile.seek(lastReadPosition);
//
//                String line;
//                while ((line = logFile.readLine()) != null) {
//                    if (line.trim().isEmpty()) continue;
//
//                    Object metricsModel = loggerService.extractMessageFromLine(line, MetricsModel.class);
//
//                    if (metricsModel instanceof MetricsModel) {
//                        Long modelTime = ((MetricsModel) metricsModel).getTime();
//                        if (modelTime != null && modelTime > latestTimestamp) {
//                            logBatch.add(metricsModel);
//                        } else if (latestTimestamp == 0) {
//                            logBatch.add(metricsModel);
//                        }
//                    }
//
//                    lastReadPosition = logFile.getFilePointer();
//
//                    // When the batch size reaches 250, sort it and send to the API
//                    if (logBatch.size() >= 250) {
//                        logBatch.sort((o1, o2) -> {
//                            if (o1 instanceof MetricsModel && o2 instanceof MetricsModel) {
//                                return Long.compare(((MetricsModel) o1).getTime(), ((MetricsModel) o2).getTime());
//                            }
//                            return 0;
//                        });
//                        boolean isSent = loggerService.sendLogBatch(new ArrayList<>(logBatch.subList(0, 250)), logApiEndpoint, METRIC_TYPE);
//                        if (isSent) {
//                            // Update the last read position
//                            loggerService.updateLastReadPosition(lastReadPosition, MARKER_FILE_PATH);
//                        }
//                        logBatch.subList(0, 250).clear();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
