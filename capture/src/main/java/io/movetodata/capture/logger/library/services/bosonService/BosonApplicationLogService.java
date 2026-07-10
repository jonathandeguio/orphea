//package io.movetodata.capture.logger.library.services.bosonService;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
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
//import java.nio.file.Paths;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class BosonApplicationLogService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(BosonApplicationLogService.class);
//
//    private static final String LOG_FILE_PATH = "logs/boson/application/boson.log";
//    private static final String MARKER_FILE_PATH = "logs/boson/application/";
//    private static final String LOG_PATTERN = "\\[ *(?<level>INFO|ERROR|WARN|DEBUG|TRACE) *] \\[(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\+\\d{4})] \\[(?<requestId>[^\\]]*)] \\[(?<logger>[^\\]]+)] - \\[(?<message>[^\\]]+)] ";
//    private static final Pattern PATTERN = Pattern.compile(LOG_PATTERN);
//    private static final String METRIC_TYPE = "boson.applicationLog";
//    private static final int BATCH_SIZE = 250;
//    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${log.api.boson.appLogEndpoint}")
//    private String logApiEndpoint;
//
//    @Autowired
//    private final LoggerService loggerService;
//
//    @Scheduled(fixedRate = 30000)
//    public void readLogFile() {
//        try {
//            long latestTimestamp = getLatestTimestamp();
//            long lastReadPosition = getLastReadPosition();
//
//            List<Object> logBatch = new ArrayList<>();
//
//            processLogFile(logBatch, latestTimestamp, lastReadPosition);
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
//        if (latestTimestampStr != null && !latestTimestampStr.trim().isEmpty() && !"null".equalsIgnoreCase(latestTimestampStr)) {
//            return Long.parseLong(latestTimestampStr);
//        }
//        return 0;
//    }
//
//    private long getLastReadPosition() throws IOException {
//        Date today = new Date();
//        String formattedDate = DATE_FORMAT.format(today);
//        String markerFilePath = MARKER_FILE_PATH + "marker." + formattedDate + ".txt";
//        return loggerService.getLastReadPosition(markerFilePath);
//    }
//
//    private void processLogFile(List<Object> logBatch, long latestTimestamp, long lastReadPosition) throws IOException, ParseException {
//        if (Files.exists(Paths.get(LOG_FILE_PATH))) {
//            try (RandomAccessFile logFile = new RandomAccessFile(LOG_FILE_PATH, "r")) {
//                logFile.seek(lastReadPosition);
//
//                String line;
//                while ((line = logFile.readLine()) != null) {
//                    ApplicationLogModel logModel = parseLogLine(line, latestTimestamp);
//                    if (logModel != null) {
//                        logBatch.add(logModel);
//                        if (logBatch.size() >= BATCH_SIZE) {
//                            processLogBatch(logBatch, logFile.getFilePointer());
//                        }
//                    }
//                }
//            }
//        } else {
//            LOGGER.warn("Log file not found at path: {}", LOG_FILE_PATH);
//        }
//    }
//
//    private ApplicationLogModel parseLogLine(String line, long latestTimestamp) throws ParseException {
//        Matcher matcher = PATTERN.matcher(line);
//        if (matcher.matches()) {
//            Date parsedDate = TIMESTAMP_FORMAT.parse(matcher.group("timestamp"));
//            long timestamp = parsedDate.getTime();
//
//            if (timestamp > latestTimestamp || latestTimestamp == 0) {
//                return ApplicationLogModel.builder()
//                        .level(matcher.group("level").trim())
//                        .timestamp(timestamp)
//                        .requestId(matcher.group("requestId").isEmpty() ? null : matcher.group("requestId"))
//                        .logger(matcher.group("logger"))
//                        .message(matcher.group("message"))
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
//            if (o1 instanceof ApplicationLogModel && o2 instanceof ApplicationLogModel) {
//                Long.compare(((ApplicationLogModel) o1).getTimestamp(), ((ApplicationLogModel) o2).getTimestamp());
//            }
//            return 0;
//        });
//        boolean isSent = loggerService.sendLogBatch(logBatch, logApiEndpoint, METRIC_TYPE);
//        if (isSent) {
//            String markerFilePath = MARKER_FILE_PATH + "marker." + DATE_FORMAT.format(new Date()) + ".txt";
//            loggerService.updateLastReadPosition(lastReadPosition, markerFilePath);
//            logBatch.clear();
//        }
//    }
//}
