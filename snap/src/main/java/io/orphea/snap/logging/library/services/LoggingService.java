package io.orphea.snap.logging.library.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import io.orphea.snap.deployments.library.models.DeploymentModel;
import io.orphea.snap.deployments.library.repository.DeploymentRepository;
import io.orphea.snap.logging.library.models.AccessLogModel;
import io.orphea.snap.logging.library.models.ApplicationLogModel;
import io.orphea.snap.logging.library.models.FrontendLogModel;
import io.orphea.snap.logging.library.models.MetricsModel;
import io.orphea.snap.passport.library.repository.UserRepository;
import io.orphea.snap.passport.library.service.UserService;
import io.orphea.snap.passport.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LoggingService {
    private final UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeploymentRepository deploymentRepository;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy EEE MMM dd HH:mm:ss zzz", Locale.ENGLISH);

    public <T> ResponseEntity<Object> createCSV(Principal principal, List<T> logModels, UUID deploymentId, String logPath, String fileName, Class<T> modelClass, String serviceName, String metricName) {
        UUID userId = userService.getUser(principal.getName()).getId();
        String stringDeploymentId = String.valueOf(deploymentId);
        try {
            if (deploymentId == null) {
                return new ResponseEntity<>("Deployment ID is missing.", HttpStatus.BAD_REQUEST);
            }

            DeploymentModel deploymentModel = deploymentRepository.getReferenceById(deploymentId);
            if (!deploymentRepository.existsById(deploymentId)) {
                return new ResponseEntity<>("No Deployment with the id exist", HttpStatus.NOT_FOUND);
            }

            updateDeploymentTimestamp(deploymentModel, logModels.get(logModels.size() - 1), metricName);
            deploymentRepository.save(deploymentModel);

            setDeploymentId(logModels, stringDeploymentId);

            String folderName = System.getenv("LOG_FILE_PATH") + "/" + serviceName + "/" + deploymentId + logPath;
            File folder = new File(folderName);
            createDirectoryIfNotExists(folder);

            File csvFile = new File(folder, fileName);

            boolean isFileNew = !csvFile.exists();
            boolean hasHeaders = !isFileNew && checkHeadersExist(csvFile, modelClass);

            if (isFileNew || !hasHeaders) {
                writeHeadersToCSV(csvFile, modelClass);
            }

            List<T> newLogs = new ArrayList<>(logModels);
            newLogs.sort(Comparator.comparing(this::getTimestamp));
            appendDataToCSV(csvFile, newLogs, modelClass);

            return new ResponseEntity<>(fileName + " CSV File Generated Successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to write CSV file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean checkHeadersExist(File csvFile, Class<?> modelClass) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] headers = reader.readNext();
            String[] expectedHeaders = getHeader(modelClass);
            return Arrays.equals(headers, expectedHeaders);
        }
    }

    private <T> void writeHeadersToCSV(File csvFile, Class<T> modelClass) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true))) {
            String[] header = getHeader(modelClass);
            writer.writeNext(header);
        }
    }

    private <T> void appendDataToCSV(File csvFile, List<T> logModels, Class<T> modelClass) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true))) {
            for (T logModel : logModels) {
                String[] line = getCSVLine(logModel, modelClass);
                writer.writeNext(line);
            }
        }
    }

    private void updateDeploymentTimestamp(DeploymentModel deploymentModel, Object logModel, String metricName) {
        if (logModel instanceof ApplicationLogModel) {
            if (Objects.equals(metricName, "boson.applicationLog")) {
                Long timestamp = ((ApplicationLogModel) logModel).getTimestamp();
                deploymentModel.setLastApplicationLogTimestamp(timestamp);
            } else if (Objects.equals(metricName, "capture.applicationLog")) {
                Long timestamp = ((ApplicationLogModel) logModel).getTimestamp();
                deploymentModel.setCaptureLastApplicationLogTimestamp(timestamp);
            }
        } else if (logModel instanceof MetricsModel) {
            MetricsModel metricsModel = (MetricsModel) logModel;
            Long time = metricsModel.getTime();
            if (Objects.equals(metricsModel.getMetricName(), "boson.metric.cpu")) {
                deploymentModel.setLastCpuMetricLogTimestamp(time);
            } else if (Objects.equals(metricsModel.getMetricName(), "boson.metric.memory")) {
                deploymentModel.setLastMemoryMetricLogTimestamp(time);
            } else if (Objects.equals(metricsModel.getMetricName(), "boson.metric.swap")) {
                deploymentModel.setLastSwapMetricLogTimestamp(time);
            } else if (Objects.equals(metricsModel.getMetricName(), "boson.metric.disk")) {
                deploymentModel.setLastDiskMetricLogTimestamp(time);
            }
        } else if (logModel instanceof AccessLogModel) {
            if (Objects.equals(metricName, "boson.accessLog")) {
                Long date = ((AccessLogModel) logModel).getDate();
                Long timestamp = date != null ? date : 0;
                deploymentModel.setLastAccessLogTimestamp(timestamp);
            }
        }
    }

    private <T> void setDeploymentId(List<T> logModels, String deploymentId) {
        for (T logModel : logModels) {
            if (logModel instanceof ApplicationLogModel) {
                ((ApplicationLogModel) logModel).setDeploymentId(deploymentId);
            } else if (logModel instanceof MetricsModel) {
                ((MetricsModel) logModel).setDeploymentId(deploymentId);
            } else if (logModel instanceof AccessLogModel) {
                ((AccessLogModel) logModel).setDeploymentId(deploymentId);
            } else if (logModel instanceof FrontendLogModel) {
                ((FrontendLogModel) logModel).setDeploymentId(UUID.fromString(deploymentId));
            }
        }
    }

    private void createDirectoryIfNotExists(File folder) {
        if (!folder.exists()) {
            try {
                folder.mkdirs();
            } catch (SecurityException se) {
                throw new RuntimeException("Failed to create directory due to security issues.");
            }
        }
    }

    private <T> String[] getHeader(Class<T> modelClass) {
        if (modelClass == ApplicationLogModel.class) {
            return new String[]{"deploymentId", "level", "timestamp", "requestId", "logger", "message"};
        } else if (modelClass == MetricsModel.class) {
            return new String[]{"deploymentId", "time", "metricName", "total", "used", "free"};
        } else if (modelClass == AccessLogModel.class) {
            return new String[]{"deploymentId", "date", "requestId", "method", "requestUri", "httpVersion", "statusCode", "clientIp", "responseSize", "responseTime", "referer", "userAgent", "userName"};
        } else if (modelClass == FrontendLogModel.class) {
            return new String[]{"deploymentId", "name", "message", "stack", "componentStack"};
        }
        throw new IllegalArgumentException("Unknown model class: " + modelClass.getName());
    }

    private <T> String[] getCSVLine(T logModel, Class<T> modelClass) {
        if (modelClass == ApplicationLogModel.class) {
            ApplicationLogModel model = (ApplicationLogModel) logModel;
            return new String[]{model.getDeploymentId(), model.getLevel(), dateFormat.format(model.getTimestamp()), model.getRequestId(), model.getLogger(), model.getMessage()};
        } else if (modelClass == MetricsModel.class) {
            MetricsModel model = (MetricsModel) logModel;
            return new String[]{model.getDeploymentId(), dateFormat.format(model.getTime()), model.getMetricName(), model.getTotal(), model.getUsed(), model.getFree()};
        } else if (modelClass == AccessLogModel.class) {
            AccessLogModel model = (AccessLogModel) logModel;
            return new String[]{model.getDeploymentId(), dateFormat.format(model.getDate()), model.getRequestId(), model.getMethod(), model.getRequestUri(), model.getHttpVersion(), model.getStatusCode(), model.getClientIp(), model.getResponseSize(), model.getResponseTime(), model.getReferer(), model.getUserAgent(), model.getUserName()};
        } else if (modelClass == FrontendLogModel.class) {
            FrontendLogModel model = (FrontendLogModel) logModel;
            return new String[]{model.getDeploymentId().toString(), model.getName(), model.getMessage(), model.getStack(), model.getComponentStack()};
        }
        throw new IllegalArgumentException("Unknown model class: " + modelClass.getName());
    }

    private <T> Long getTimestamp(T logModel) {
        if (logModel instanceof ApplicationLogModel) {
            return ((ApplicationLogModel) logModel).getTimestamp();
        } else if (logModel instanceof MetricsModel) {
            return ((MetricsModel) logModel).getTime();
        } else if (logModel instanceof AccessLogModel) {
            return ((AccessLogModel) logModel).getDate();
        } else if (logModel instanceof FrontendLogModel) {
            return ((FrontendLogModel) logModel).getDate();
        }
        return new Date(0).getTime(); // Default fallback date
    }

    public long getLastReadPosition(String MARKER_FILE_PATH) throws IOException {
        if (Files.exists(Paths.get(MARKER_FILE_PATH))) {
            String positionStr = new String(Files.readAllBytes(Paths.get(MARKER_FILE_PATH))).trim();
            return positionStr.isEmpty() ? 0 : Long.parseLong(positionStr);
        } else {
            return 0;
        }
    }

    public void updateLastReadPosition(long position, String MARKER_FILE_PATH) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(MARKER_FILE_PATH))) {
            writer.write(String.valueOf(position));
        }
    }
}
