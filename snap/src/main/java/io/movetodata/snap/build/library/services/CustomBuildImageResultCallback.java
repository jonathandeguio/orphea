package io.movetodata.snap.build.library.services;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomBuildImageResultCallback extends BuildImageResultCallback {
    private final BufferedWriter logWriter;

    public CustomBuildImageResultCallback(String logFilePath) throws IOException {
        // Ensure the directory for the log file exists
        Path logPath = Paths.get(logFilePath).getParent();
        if (logPath != null && !Files.exists(logPath)) {
            Files.createDirectories(logPath);
        }

        // Create or append to the log file
        this.logWriter = new BufferedWriter(new FileWriter(logFilePath, true));
    }

    @Override
    public void onNext(BuildResponseItem item) {
        try {
            if (item.getStream() != null) {
                logWriter.write(item.getStream());
                logWriter.flush(); // Flush after each write for real-time logging
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onNext(item);
    }

    @Override
    public void onComplete() {
        try {
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onComplete();
    }
}
