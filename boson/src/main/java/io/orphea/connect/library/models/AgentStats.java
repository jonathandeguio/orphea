package io.orphea.connect.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "connect_agent_stats")
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
