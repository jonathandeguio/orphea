package io.orphea.connect.library.DTOs;


import io.orphea.connect.library.models.RestAPIRequest;
import io.orphea.dataset.library.DTOs.CsvPreprocessingDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LinkPreviewDTO {
    private HashMap<String, String> query;
    private List<RestAPIRequest> requests;
    private String responseParam;
    private CsvPreprocessingDTO csvPreprocessing;
}
