package io.bosler.snap.platform.library.models;


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
@Table(name = "platform_config_smtp")
public class SMTPConfigModel {
    // For 1 config, getting it via a row, in which config is "platform"
    public @Id
    String config;
    private String smtpEmail;
    private String smtpPassword;
    private String host = "smtp.gmail.com";
    private Integer port = 587;
    private String auth = "true";
    private String ttls = "true";
}
