package io.movetodata;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
     public static void main(String[] args) {
        String fileName = "example.txt"; // Replace with your actual file name

        // Using Paths.get to create a Path object from the file name
        Path path = Paths.get(fileName);

        // Extracting file name and file type
        String extractedFileName = path.getFileName().toString();
        String fileType = getExtension(extractedFileName);

    }
}
