package io.movetodata.ignite.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "ignite_agent")
public class Agents {

    public @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    public String name;
    public String description;

    private UUID parent;
    private boolean proxy = false;
    private String httpProxy;
    private String httpsProxy;
    private Date lastStatus;

    @CreationTimestamp
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;

}
