package io.movetodata.platform.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "platform_config_spark")
public class SparkConfigModel {
    public @Id
    String config;
    private String master = "local[*]";
    private String pgSync = "local";
    private String dataset = "local";
    private String columnStats = "local";
    private String sqlBuild = "kubernetes";
    private String pythonBuild = "kubernetes";
    private String sqlPreview = "local";
    private String pythonPreview = "kubernetes";
}
