package io.movetodata.connect.library.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProcessOverlayResultBodyDTO {
    String body;
    List<String> errors = new ArrayList<>();
}
