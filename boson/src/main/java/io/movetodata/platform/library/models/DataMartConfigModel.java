package io.movetodata.platform.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "platform_config_datamart")
public class DataMartConfigModel {
    public @Id
    String config;

    @OneToMany(targetEntity = DataMartModel.class, cascade = CascadeType.ALL)
    private List<DataMartModel> dataMartModels = new ArrayList<>();
}
