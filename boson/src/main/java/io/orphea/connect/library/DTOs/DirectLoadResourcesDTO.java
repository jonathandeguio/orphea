package io.orphea.connect.library.DTOs;


import io.orphea.connect.library.models.Link;
import io.orphea.connect.library.models.Source;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DirectLoadResourcesDTO {
    private Link link;
    private Source source;
}
