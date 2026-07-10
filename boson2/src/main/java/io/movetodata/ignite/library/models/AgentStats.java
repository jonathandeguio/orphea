package io.movetodata.ignite.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ignite_agent_stats")
public class AgentStats {

    public @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreationTimestamp
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    private String os;
    private String javaVersion;
    private String installDirectory;
    private String hostname;
    private String canonicalHostName;
    private String ipAddress;
    private long freeSpace;
    private long totalSpace;
//    public UUID createdBy;
//    public UUID updatedBy;
}
