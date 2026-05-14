//package io.orphea.snap.logging.library.services;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.orphea.snap.logging.library.models.ApplicationLogModel;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.security.Principal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class SnapApplicationLogService {
//
//    private static final String LOG_FILE_PATH = "/orphea/snap/logs/application/snap.log";
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//
//    private static final String MARKER_FILE_PATH = "/orphea/snap/logs/application/";
//
//    @Autowired
//    private final LoggingService loggerService;
//
//    public Principal principal;
//    private static final String LOG_PATTERN = "\\[ *(?<level>INFO|ERROR|WARN|DEBUG|TRACE) *] \\[(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\+\\d{4})] \\[(?<requestId>[^\\]]*)] \\[(?<logger>[^\\]]+)] - \\[(?<message>[^\\]]+)] ";
//    private static final Pattern pattern = Pattern.compile(LOG_PATTERN);
//    private static final String SNAP_DEPLOYMENT_ID = "47b1da68-d9e2-4dfc-b42b-910dc0d1fe8d";
//
//    @Scheduled(fixedRate = 30000)
//    public void readLogFile() throws IOException {
//
//        Date today = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedDate = dateFormat.format(today);
//
//        String markerFileName = "marker." + formattedDate + ".txt";
//        String markerFilePath = MARKER_FILE_PATH + markerFileName;
//        List<ApplicationLogModel> logBatch = new ArrayList<>();
//        long lastReadPosition = loggerService.getLastReadPosition(markerFilePath);
//
//        if (Files.exists(Paths.get(LOG_FILE_PATH))) {
//            try (RandomAccessFile logFile = new RandomAccessFile(LOG_FILE_PATH, "r")) {
//                logFile.seek(lastReadPosition);
//
//                String line;
//                while ((line = logFile.readLine()) != null) {
//                    Matcher matcher = pattern.matcher(line);
//
//                    if (matcher.matches()) {
//                        String timestampStr = matcher.group("timestamp");
//                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
//                        Date parsedDate = formatter.parse(timestampStr);
//                        long timestamp = parsedDate.getTime();
//
//                        ApplicationLogModel logModel = ApplicationLogModel.builder()
//                                .level(matcher.group("level").trim())
//                                .timestamp(timestamp)
//                                .requestId(matcher.group("requestId").isEmpty() ? null : matcher.group("requestId"))
//                                .logger(matcher.group("logger"))
//                                .message(matcher.group("message"))
//                                .build();
//                        logBatch.add(logModel);
//                        if (logBatch.size() >= 2) {
//                            logBatch.sort((o1, o2) -> {
//                                if (o1 != null && o2 != null) {
//                                    return Long.compare(o1.getTimestamp(), o2.getTimestamp());
//                                }
//                                return 0;
//                            });
//                            lastReadPosition = logFile.getFilePointer();
//
//                            if (logBatch.size() >= 250) {
//                                ResponseEntity<Object> result = loggerService.createCSV(principal, logBatch, UUID.fromString(SNAP_DEPLOYMENT_ID), "/application", "ApplicationLogs.csv", ApplicationLogModel.class, "snap", null);
//                                if (result.getStatusCode().is2xxSuccessful()) {
//                                    loggerService.updateLastReadPosition(lastReadPosition, markerFilePath);
//                                    logBatch.subList(0, 250).clear();
//                                }
//                            }
//                        }
//                    } else {
//                        System.out.println("No match for line: " + line);
//                    }
//                }
//
//            } catch (IOException | ParseException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("No log file found");
//        }
//    }
//}
