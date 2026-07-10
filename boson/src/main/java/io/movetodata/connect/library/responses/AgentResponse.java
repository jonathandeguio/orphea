package io.movetodata.connect.library.responses;

import io.movetodata.connect.library.models.AgentOneTimeCode;
import io.movetodata.connect.library.models.Agents;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    Agents agent;
    String agentOneTimeCode;
}
