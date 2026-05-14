//package io.orphea.capture.logger.library.services.metricService;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.orphea.capture.logger.library.models.MetricsModel;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import com.sun.management.OperatingSystemMXBean;
//import oshi.SystemInfo;
//import oshi.hardware.HWDiskStore;
//
//import java.io.*;
//import java.lang.management.ManagementFactory;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.DecimalFormat;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.zip.GZIPOutputStream;
//
//@Component
//public class MetricsLoggerService {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final String cpuFilePath = "logs/boson/metrics/cpu/cpu_metrics.log";
//    private final String memoryFilePath = "logs/boson/metrics/memory/memory_metrics.log";
//    private final String swapFilePath = "logs/boson/metrics/swap/swap_metrics.log";
//    private final String diskFilePath = "logs/boson/metrics/disk/disk_metrics.log";
//    private final String archiveFolderPath = "logs/boson/metrics/archive/";
//    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @Scheduled(fixedRate = 3000) // Fixed rate of Fetching Metrics
//    public void logMetrics() {
//        ensureLogFileDirectoryExists();
//
//        List<MetricsModel> cpuMetrics = new ArrayList<>();
//        List<MetricsModel> memoryMetrics = new ArrayList<>();
//        List<MetricsModel> swapMetrics = new ArrayList<>();
//        List<MetricsModel> diskMetrics = new ArrayList<>();
//
//        // Log CPU Metrics
//        logCpuUsage(cpuMetrics);
//
//        // Log Memory Metrics
//        logMemoryMetrics(memoryMetrics);
//
//        // Log Swap Metrics
////        logSwapMetrics(swapMetrics);
//
//        // Log Disk Metrics
//        logDiskMetrics(diskMetrics);
//
//        // Write Metrics to File
//        writeMetricsToFile(cpuMetrics, cpuFilePath);
//        writeMetricsToFile(memoryMetrics, memoryFilePath);
//        writeMetricsToFile(swapMetrics, swapFilePath);
//        writeMetricsToFile(diskMetrics, diskFilePath);
//    }
//
//    private void ensureLogFileDirectoryExists() {
//        ensureDirectoryExists(cpuFilePath);
//        ensureDirectoryExists(memoryFilePath);
//        ensureDirectoryExists(swapFilePath);
//        ensureDirectoryExists(diskFilePath);
//    }
//
//    private void ensureDirectoryExists(String filePath) {
//        File logFile = new File(filePath);
//        File parentDir = logFile.getParentFile();
//        if (parentDir != null && !parentDir.exists()) {
//            boolean created = parentDir.mkdirs();
//            if (!created) {
//                System.err.println("Failed to create directory: " + parentDir);
//            }
//        }
//    }
//
//    private void logCpuUsage(List<MetricsModel> metrics) {
//        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        double systemCpuLoad = osBean.getCpuLoad(); // This value is already in percentage
//        long currentTimeMillis = System.currentTimeMillis();
//        metrics.add(createMetric("system.cpu", systemCpuLoad, null, null, currentTimeMillis));
//    }
//
//    private void logMemoryMetrics(List<MetricsModel> metrics) {
//        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        double totalMemory = (double) osBean.getTotalMemorySize() / (1024 * 1024 * 1024); // Convert to GB
//        double freeMemory = (double) osBean.getFreeMemorySize() / (1024 * 1024 * 1024); // Convert to GB
//        double usedMemory = totalMemory - freeMemory;
//        long currentTimeMillis = System.currentTimeMillis();
//        metrics.add(createMetric("system.memory", totalMemory, freeMemory, usedMemory, currentTimeMillis));
//    }
//
////    private void logSwapMetrics(List<MetricsModel> metrics) {
////        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
////        double totalSwap = (double) osBean.getTotalSwapSpaceSize() / (1024 * 1024 * 1024); // Convert to GB
////        double freeSwap = (double) osBean.getFreeSwapSpaceSize() / (1024 * 1024 * 1024); // Convert to GB
////        double usedSwap = totalSwap - freeSwap;
////        long currentTimeMillis = System.currentTimeMillis();
////        metrics.add(createMetric("system.swap", totalSwap, freeSwap, usedSwap, currentTimeMillis));
////    }
//
//    private void logDiskMetrics(List<MetricsModel> metrics) {
//        SystemInfo systemInfo = new SystemInfo();
//        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();
//
//        double totalSpaceSum = 0;
//        double usableSpaceSum = 0;
//        double usedSpaceSum = 0;
//
//        for (HWDiskStore diskStore : diskStores) {
//            diskStore.updateAttributes();
//        }
//
//        for (File root : File.listRoots()) {
//            double totalSpace = (double) root.getTotalSpace() / (1024 * 1024 * 1024); // Convert to GB
//            double usableSpace = (double) root.getUsableSpace() / (1024 * 1024 * 1024); // Convert to GB
//            double usedSpace = totalSpace - usableSpace;
//
//            totalSpaceSum += totalSpace;
//            usableSpaceSum += usableSpace;
//            usedSpaceSum += usedSpace;
//        }
//        long currentTimeMillis = System.currentTimeMillis();
//        metrics.add(createMetric("system.disk", totalSpaceSum, usableSpaceSum, usedSpaceSum, currentTimeMillis));
//    }
//
//    private void writeMetricsToFile(List<MetricsModel> metrics, String filePath) {
//        try {
//            if (isTimeBetween(LocalTime.of(0, 0), LocalTime.of(0, 2))) {
//                LocalDate yesterday = LocalDate.now().minusDays(1);
//                if (!fileExistsInArchive(yesterday, filePath)) {
//                    moveFileToArchive(filePath);
//                }
//            }
//
//            try (FileWriter fileWriter = new FileWriter(filePath, true)) {
//                for (MetricsModel metric : metrics) {
//                    fileWriter.write(objectMapper.writeValueAsString(metric) + System.lineSeparator());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean isTimeBetween(LocalTime start, LocalTime end) {
//        LocalTime now = LocalTime.now();
//        return !now.isBefore(start) && !now.isAfter(end);
//    }
//
//    private boolean fileExistsInArchive(LocalDate date, String filePath) {
//        String archiveSubFolder = determineArchiveSubFolder(filePath);
//        String fileName = Paths.get(filePath).getFileName().toString();
//        String archiveFilePath = archiveFolderPath + archiveSubFolder + "/" + fileName + "-" + date.format(dateFormatter);
//
//        File archiveFile = new File(archiveFilePath);
//        return archiveFile.exists();
//    }
//
//    private void moveFileToArchive(String filePath) throws IOException {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        String archiveSubFolder = determineArchiveSubFolder(filePath);
//        Path archivePath = Paths.get(archiveFolderPath, archiveSubFolder, new File(filePath).getName() + "-" + yesterday.format(dateFormatter));
//        Files.createDirectories(archivePath.getParent());
//        Files.move(Paths.get(filePath), archivePath);
//        try {
//            compressFile(archivePath.toAbsolutePath().toString(), archivePath.toAbsolutePath().toString() + ".gz");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static void compressFile(String inputFile, String outputFile) throws IOException {
//        try (FileInputStream fis = new FileInputStream(inputFile);
//             FileOutputStream fos = new FileOutputStream(outputFile);
//             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
//
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = fis.read(buffer)) > 0) {
//                gos.write(buffer, 0, length);
//            }
//        }
//    }
//
//    private String determineArchiveSubFolder(String filePath) {
//        if (filePath.contains("cpu")) {
//            return "cpu";
//        } else if (filePath.contains("memory")) {
//            return "memory";
//        } else if (filePath.contains("swap")) {
//            return "swap";
//        } else if (filePath.contains("disk")) {
//            return "disk";
//        }
//        return "others";
//    }
//
//    private String formatToTwoDecimals(double value) {
//        DecimalFormat decimalFormat = new DecimalFormat("#.######");
//        return decimalFormat.format(value);
//    }
//
//    private MetricsModel createMetric(String metricName, Object totalValue, Object freeValue, Object usedValue, long timeInMilliseconds) {
//        Object formattedTotalValue = formatValue(totalValue);
//        Object formattedFreeValue = formatValue(freeValue);
//        Object formattedUsedValue = formatValue(usedValue);
//
//        try {
//            Thread.sleep(1);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            e.printStackTrace();
//        }
//
//        return MetricsModel.builder()
//                .metricName(metricName)
//                .total(formattedTotalValue)
//                .free(formattedFreeValue)
//                .used(formattedUsedValue)
//                .time(timeInMilliseconds)
//                .build();
//    }
//
//    private Object formatValue(Object value) {
//        if (value instanceof Double) {
//            return formatToTwoDecimals((Double) value);
//        } else if (value instanceof Float) {
//            return formatToTwoDecimals((Float) value);
//        } else if (value instanceof Long) {
//            return formatToTwoDecimals((Long) value);
//        } else {
//            return value;
//        }
//    }
//
//    private String formatToTwoDecimals(long value) {
//        DecimalFormat decimalFormat = new DecimalFormat("#.##");
//        return decimalFormat.format((double) value);
//    }
//}
