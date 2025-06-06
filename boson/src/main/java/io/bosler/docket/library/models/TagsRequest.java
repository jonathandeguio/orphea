package io.bosler.docket.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
