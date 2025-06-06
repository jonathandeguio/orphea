package io.bosler.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
