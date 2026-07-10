package io.movetodata.connect.library.responses;

import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.models.Source;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectResponse {
    private List<Source> sources;
    private List<Link> links;
    private List<Link> buildNow;
}
