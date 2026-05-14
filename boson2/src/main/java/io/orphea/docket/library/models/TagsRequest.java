package io.orphea.docket.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagsRequest {


    public String name;
    public String description;
    public String color;

    @NotNull
    @NotEmpty
    private UUID tagsCategoryId;
}
