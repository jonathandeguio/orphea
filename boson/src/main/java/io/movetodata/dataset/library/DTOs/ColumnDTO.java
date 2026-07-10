package io.movetodata.dataset.library.DTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDTO {
    public String headerName;
    public String field;
    public String type;
}
