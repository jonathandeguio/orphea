package io.bosler.kitab.library.DTOs;


import io.bosler.dataset.library.DTOs.CsvPreprocessingDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadFileDTO {
    private MultipartFile file;
    private String sheetName = "first";
    private String fileName;
    private String description = "uploaded";
    private CsvPreprocessingDTO csvPreprocessing;

    private String mode;
}
