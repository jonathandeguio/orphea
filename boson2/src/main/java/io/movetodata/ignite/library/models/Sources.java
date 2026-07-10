package io.movetodata.ignite.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ignite_sources")
public class Sources {

    private @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    private String name;
//    @NotEmpty(message = "source config is mandatory")
//    private HashMap<String, String> sourceConfig;
    private String description;
    private UUID parent;

    private boolean directLoad = false;
    @ElementCollection
    private List<UUID> agentId;

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
