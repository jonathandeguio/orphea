package io.bosler.connect.library.responses;

import io.bosler.connect.library.models.AgentOneTimeCode;
import io.bosler.connect.library.models.Agents;
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
