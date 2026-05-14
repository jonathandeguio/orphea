package io.orphea.connect.library.responses;

import io.orphea.connect.library.models.AgentOneTimeCode;
import io.orphea.connect.library.models.Agents;
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
