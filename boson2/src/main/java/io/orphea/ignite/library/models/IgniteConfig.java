package io.orphea.ignite.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ignite_config")
public class IgniteConfig {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreationTimestamp
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
    UUID agentId;
    UUID version;
}
