package io.bosler.kepler.library.models;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "kepler_dataset_filter")
public class FilterModel {
    @Id
    @GeneratedValue
    private UUID id;

    private String key;

    @NotNull
    private String operator;

    private String value;

    public FilterModel(UUID id, String key, String operator, String value) {
        this.id = id;
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public FilterModel(FilterModel other) {
        this.key = other.key;
        this.operator = other.operator;
        this.value = other.value;
    }
}
