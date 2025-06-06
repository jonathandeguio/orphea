package io.bosler.connect.library.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ProcessOverlayResultDTO {
    Map<String, String> keyValuePair = new HashMap<>();
    List<String> errors = new ArrayList<>();
}
