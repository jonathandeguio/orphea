package io.movetodata.kitab.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_folder")
public class FolderModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    public String name;
    public String description;

    public String type;
    public UUID parent;
    public String status;
    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
}
