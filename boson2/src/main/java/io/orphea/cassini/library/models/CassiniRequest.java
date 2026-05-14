package io.orphea.cassini.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CassiniRequest {


    public String name;
    public String description;
    public String color;

    @NotNull
    @NotEmpty
    private UUID tagsCategoryId;
}
