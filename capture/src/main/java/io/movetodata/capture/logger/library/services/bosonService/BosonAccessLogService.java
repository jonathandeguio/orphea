//package io.movetodata.capture.logger.library.services.bosonService;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.movetodata.capture.logger.library.models.AccessLogModel;
//import io.movetodata.capture.logger.library.models.ApplicationLogModel;
//import io.movetodata.capture.logger.library.services.LoggerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class BosonAccessLogService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(BosonAccessLogService.class);
//
//    private static final String LOG_PATTERN =
//            "(\\d{2}/[A-Za-z]{3}/\\d{4})\\s+" +           // Date
//                    "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+" +       // Start Time
//                    "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})?\\s+" +      // End Time (optional)
//                    "(\\S+|-)\\s+" +                              // Request ID (optional)
//                    "(\\S+)\\s+" +                                // HTTP Method
//                    "(\\S+)\\s+" +                                // Request URI
//                    "(HTTP/\\d\\.\\d)\\s+" +                      // HTTP Version
//                    "(\\d{3})\\s+" +                              // Status Code
//                    "(\\S+)\\s+" +                                // Client IP (IPv6 format)
//                    "(\\d+|-)\\s+" +                              // Bytes Sent (optional)
//                    "(\\d+\\.\\d{3})\\s+" +                       // Processing Time in seconds
//                    "(\\S+|-)\\s+" +                              // Referer (optional)
//                    "\"([^\"]*)\"\\s+" +                          // User-Agent
//                    "(\\S+|-)";                                   // Username
//
//
//    private static final String MARKER_FILE_PATH = "logs/boson/accessLogs/";
//    private static final String METRIC_TYPE = "boson.accessLog";
//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
//    private static final SimpleDateFormat LOG_TIMESTAMP_FORMAT = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss.SSS");
//    private static final int BATCH_SIZE = 250;
//
//    @Value("${log.api.boson.accessLogEndpoint}")
//    private String logApiEndpoint;
//
//    private final ObjectMapper objectMapper;
//    private final LoggerService loggerService;
//
//    @Scheduled(fixedRate = 30000)
//    public void readLogFile() {
//        try {
//            long latestTimestamp = getLatestTimestamp();
//            String filePath = getFilePathForToday();
//            long lastReadPosition = getLastReadPosition();
//
//            List<Object> logBatch = new ArrayList<>();
//
//            processLogFile(filePath, logBatch, latestTimestamp, lastReadPosition);
//
//            if (!logBatch.isEmpty()) {
//                processLogBatch(logBatch, lastReadPosition);
//            }
//
//        } catch (Exception e) {
//            LOGGER.error("Error while reading log file", e);
//        }
//    }
//
//    private long getLatestTimestamp() throws IOException {
//        String latestTimestampStr = loggerService.getLatestTimestampFromAPI(METRIC_TYPE);
//        return (latestTimestampStr != null && !latestTimestampStr.trim().isEmpty() && !"null".equalsIgnoreCase(latestTimestampStr))
//                ? Long.parseLong(latestTimestampStr)
//                : 0;
//    }
//
//    private String getFilePathForToday() {
//        String formattedDate = DATE_FORMAT.format(new Date());
//        String logFilePath = "logs/boson/accessLogs/";
//        return logFilePath + "boson." + formattedDate + ".log";
//    }
//
//    private long getLastReadPosition() throws IOException {
//        String markerFilePath = getMarkerFilePath();
//        return loggerService.getLastReadPosition(markerFilePath);
//    }
//
//    private String getMarkerFilePath() {
//        String formattedDate = DATE_FORMAT.format(new Date());
//        return MARKER_FILE_PATH + "marker." + formattedDate + ".txt";
//    }
//
//    private void processLogFile(String filePath, List<Object> logBatch, long latestTimestamp, long lastReadPosition) throws IOException, ParseException {
//        Path path = Paths.get(filePath);
//
//        if (Files.exists(path)) {
//            try (RandomAccessFile logFile = new RandomAccessFile(filePath, "r")) {
//                logFile.seek(lastReadPosition);
//
//                String line;
//                Pattern pattern = Pattern.compile(LOG_PATTERN);
//
//                while ((line = logFile.readLine()) != null) {
//                    AccessLogModel entry = parseLogLine(line, pattern, latestTimestamp);
//                    if (entry != null) {
//                        logBatch.add(entry);
//                        if (logBatch.size() >= BATCH_SIZE) {
//                            processLogBatch(logBatch, logFile.getFilePointer());
//                        }
//                    }
//                }
//            }
//        } else {
//            LOGGER.warn("Log file not found at path: {}", filePath);
//        }
//    }
//
//    private AccessLogModel parseLogLine(String line, Pattern pattern, long latestTimestamp) throws ParseException {
//        Matcher matcher = pattern.matcher(line);
//
//        if (matcher.matches()) {
//            String dateTimeString = matcher.group(1) + " " + matcher.group(2);
//            Date parsedDate = LOG_TIMESTAMP_FORMAT.parse(dateTimeString);
//            long timestamp = parsedDate.getTime();
//
//            if (timestamp > latestTimestamp || latestTimestamp == 0) {
//                return AccessLogModel.builder()
//                        .date(timestamp)
//                        .requestId(Optional.ofNullable(matcher.group(4)).filter(s -> !s.equals("-")).orElse(null))
//                        .method(matcher.group(5))
//                        .requestUri(matcher.group(6))
//                        .httpVersion(matcher.group(7))
//                        .statusCode(matcher.group(8))
//                        .clientIp(matcher.group(9))
//                        .responseSize(Optional.ofNullable(matcher.group(10)).filter(s -> !s.equals("-")).orElse(null))
//                        .responseTime(matcher.group(11))
//                        .referer(Optional.ofNullable(matcher.group(12)).filter(s -> !s.equals("-")).orElse(null))
//                        .userAgent(matcher.group(13))
//                        .userName(matcher.group(14))
//                        .build();
//            }
//        } else {
//            LOGGER.debug("No match for line: {}", line);
//        }
//        return null;
//    }
//
//    private void processLogBatch(List<Object> logBatch, long lastReadPosition) throws IOException {
//        logBatch.sort((o1, o2) -> {
//            if (o1 instanceof AccessLogModel && o2 instanceof AccessLogModel) {
//                Long.compare(((AccessLogModel) o1).getDate(), ((AccessLogModel) o2).getDate());
//            }
//            return 0;
//        });
//
//        boolean isSent = loggerService.sendLogBatch(logBatch, logApiEndpoint, METRIC_TYPE);
//        if (isSent) {
//            loggerService.updateLastReadPosition(lastReadPosition, getMarkerFilePath());
//            logBatch.clear();
//        }
//    }
//}
