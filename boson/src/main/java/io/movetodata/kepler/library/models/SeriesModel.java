package io.movetodata.kepler.library.models;

import io.movetodata.sharedutils.MoveToDataUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.UUID;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_series")
public class SeriesModel implements MoveToDataUtils {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID seriesId;
    private UUID id;
    private String seriesName;
    private String columnName;
    private String aggregate;
    private ArrayList<String> groupBy;
    private String sort;
    private String seriesType;
    private String seriesIndex;
    private Boolean reversed;
    private String seriesCustomize;

    public SeriesModel(SeriesModel original) {
        this.id = UUID.randomUUID();
        this.seriesName = original.seriesName;
        this.columnName = original.columnName;
        this.aggregate = original.aggregate;
        this.groupBy = original.groupBy;
        this.sort = original.sort;
        this.seriesType = original.seriesType;
        this.seriesIndex = original.seriesIndex;
        this.reversed = original.reversed;
        this.seriesCustomize = original.seriesCustomize;
    }
}
