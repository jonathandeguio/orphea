package io.movetodata.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_parameters")
public class ParametersModel {
    @Id
    @GeneratedValue
    private UUID id;

    private String label;

    @Column(name = "column_name")
    private String column;
}
