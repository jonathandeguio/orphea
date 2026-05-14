package io.orphea.platform.library.models;

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
@Table(name = "platform_config_git")
public class GitConfigModel {
    public @Id
    String config;
    private String host = "julia";
    private Integer apiPort = 58080;
    private Integer port = 58542;
}
