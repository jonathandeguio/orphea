package io.orphea.kepler.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_charts_time")
public class TimeModel {
    @Id
    @GeneratedValue
    private UUID id;
    private String timeColumn;
    private String timeGrain;
    private String timeRange;
}
