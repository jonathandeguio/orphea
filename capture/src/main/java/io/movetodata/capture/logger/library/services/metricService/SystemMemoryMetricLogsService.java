//package io.movetodata.capture.logger.library.services.metricService;
//
//import io.movetodata.capture.logger.library.models.MetricsModel;
//import io.movetodata.capture.logger.library.services.LoggerService;
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
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class SystemMemoryMetricLogsService {
//    private static final String LOG_FILE_PATH = "logs/boson/metrics/memory/memory_metrics.log";
//    private static final String MARKER_FILE_DIR = "logs/boson/metrics/memory/";
//
//    @Value("${log.api.metric.memoryMetricsEndpoint}")
//    private String logApiEndpoint;
//
//    private static final String METRIC_TYPE = "boson.metric.memory";
//
//    @Autowired
//    private final LoggerService loggerService;
//
//    @Scheduled(fixedRate = 30000)
//    public void readLogFile() throws IOException {
//        long latestTimestamp = 0;
//        String latestTimestampStr = loggerService.getLatestTimestampFromAPI(METRIC_TYPE);
//        if (latestTimestampStr != null && !latestTimestampStr.trim().isEmpty() && !"null".equalsIgnoreCase(latestTimestampStr)) {
//            latestTimestamp = Long.parseLong(latestTimestampStr);
//        }
//
//        Date today = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedDate = dateFormat.format(today);
//
//        String markerFileName = "marker." + formattedDate + ".txt";
//        String markerFilePath = MARKER_FILE_DIR + markerFileName;
//
//        long lastReadPosition = loggerService.getLastReadPosition(markerFilePath);
//
//        Path logPath = Paths.get(LOG_FILE_PATH);
//
//        if (Files.exists(logPath)) {
//            try (RandomAccessFile logFile = new RandomAccessFile(LOG_FILE_PATH, "r")) {
//                logFile.seek(lastReadPosition);
//
//                List<Object> logBatch = new ArrayList<>();
//                String line;
//
//                while ((line = logFile.readLine()) != null) {
//                    if (line.trim().isEmpty()) continue;
//
//                    Object metricsModel = loggerService.extractMessageFromLine(line, MetricsModel.class);
//
//                    if (metricsModel instanceof MetricsModel) {
//                        long modelTime = ((MetricsModel) metricsModel).getTime();
//                        if (modelTime > latestTimestamp) {
//                            logBatch.add(metricsModel);
//                        } else if (latestTimestamp == 0) {
//                            logBatch.add(metricsModel);
//                        }
//                    }
//                    lastReadPosition = logFile.getFilePointer();
//
//                    if (logBatch.size() == 250) {
//                        sendBatch(logBatch, lastReadPosition, markerFilePath);
//                    }
//                }
//
//                // Final batch send
//                if (!logBatch.isEmpty()) {
//                    sendBatch(logBatch, lastReadPosition, markerFilePath);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void sendBatch(List<Object> logBatch, long lastReadPosition, String markerFilePath) throws IOException {
//        logBatch.sort(this::compareMetricsModel);
//
//        boolean isSent = loggerService.sendLogBatch(logBatch, logApiEndpoint, METRIC_TYPE);
//        if (isSent) {
//            loggerService.updateLastReadPosition(lastReadPosition, markerFilePath);
//        }
//        logBatch.clear();
//    }
//
//    private int compareMetricsModel(Object o1, Object o2) {
//        if (o1 instanceof MetricsModel && o2 instanceof MetricsModel) {
//            return Long.compare(((MetricsModel) o1).getTime(), ((MetricsModel) o2).getTime());
//        }
//        return 0;
//    }
//}
