//package io.bosler.snap.logging.library.services;
//
//import io.bosler.snap.logging.library.models.AccessLogModel;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.Files;
//import java.security.Principal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class SnapAccessLogService {
//
//    @Autowired
//    private final LoggingService loggerService;
//    public Principal principal;
//    // Define the regex pattern to match the log entries with optional fields
//    private static final String LOG_PATTERN =
//            "(\\d{2}/[A-Za-z]{3}/\\d{4}) " +  // Date
//                    "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) " +  // Start Time
//                    "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})? " +  // End Time (optional)
//                    "(\\S+|-) " +  // Request ID (optional)
//                    "(\\S+) " +  // HTTP Method
//                    "(\\S+) " +  // Request URI
//                    "(HTTP/\\d\\.\\d) " +  // HTTP Version
//                    "(\\d{3}) " +  // Status Code
//                    "(\\S+) " +  // Client IP (IPv6 format)
//                    "(\\d+|-) " +  // Bytes Sent (optional)
//                    "(\\d+\\.\\d{3}) " +  // Processing Time in seconds
//                    "(\\S+|-) " +  // Referer (optional)
//                    "\"([^\"]*)\"";  // User-Agent
//
//    private static final String MARKER_FILE_PATH = "/bosler/snap/logs/accessLog/";
//    private static final String SNAP_DEPLOYMENT_ID = "47b1da68-d9e2-4dfc-b42b-910dc0d1fe8d";
//    String LOG_FILE_PATH = "/bosler/snap/logs/accessLogs/";
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
//        String FILE_NAME = "capture." + formattedDate + ".log";
//        String FILE_PATH = LOG_FILE_PATH + FILE_NAME;
//        List<AccessLogModel> logBatch = new ArrayList<>();
//        Path path = Paths.get(FILE_PATH);
//
//        long lastReadPosition = loggerService.getLastReadPosition(markerFilePath);
//
//        if (Files.exists(path)) {
//            try (RandomAccessFile logFile = new RandomAccessFile(FILE_PATH, "r")) {
//                logFile.seek(lastReadPosition);
//
//                String line;
//                Pattern pattern = Pattern.compile(LOG_PATTERN);
//
//                while ((line = logFile.readLine()) != null) {
//                    Matcher matcher = pattern.matcher(line);
//
//                    if (matcher.matches()) {
//                        // Extract matched groups
//                        String date = matcher.group(1);
//                        String startTime = matcher.group(2);
//                        String dateTimeString = date + " " + startTime;
//                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss.SSS");
//
//                        Date parsedDate = formatter.parse(dateTimeString);
//                        long timestamp = parsedDate.getTime();
//
//                        // Build AccessLogModel
//                        AccessLogModel entry = AccessLogModel.builder()
//                                .date(timestamp)
//                                .requestId(matcher.group(4) != null && !matcher.group(4).equals("-") ? matcher.group(4) : null)
//                                .method(matcher.group(5))
//                                .requestUri(matcher.group(6))
//                                .httpVersion(matcher.group(7))
//                                .statusCode(matcher.group(8))
//                                .clientIp(matcher.group(9))
//                                .responseSize(matcher.group(10) != null && !matcher.group(10).equals("-") ? matcher.group(10) : null)
//                                .responseTime(matcher.group(11))
//                                .referer(matcher.group(12) != null && !matcher.group(12).equals("-") ? matcher.group(12) : null)
//                                .userAgent(matcher.group(13))
//                                .build();
//                        logBatch.add(entry);
//                    } else {
//                        System.out.println("No match for line: " + line);
//                    }
//
//                    lastReadPosition = logFile.getFilePointer();
//
//                    if (logBatch.size() >= 2) {
//                        logBatch.sort((o1, o2) -> {
//                            if (o1 != null && o2 != null) {
//                                return Long.compare(o1.getDate(), o2.getDate());
//                            }
//                            return 0;
//                        });
//
//                        if (logBatch.size() >= 250) {
//                            ResponseEntity<Object> result = loggerService.createCSV(principal, logBatch, UUID.fromString(SNAP_DEPLOYMENT_ID), "/accessLogs", "AccessLogs.csv", AccessLogModel.class, "snap", null);
//                            if (result.getStatusCode().is2xxSuccessful()) {
//                                loggerService.updateLastReadPosition(lastReadPosition, markerFilePath);
//                                logBatch.subList(0, 250).clear();
//                            }
//                        }
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
