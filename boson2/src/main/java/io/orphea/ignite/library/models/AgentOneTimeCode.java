package io.orphea.ignite.library.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ignite_agent_one_time_code")
public class AgentOneTimeCode {

    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private String code;
    private UUID agentId;
    private Date expiryTime;
    private boolean installUsed;
    private boolean downloadUsed;

    @CreationTimestamp
    private Date createdAt = new Date();
    private UUID createdBy;
}
