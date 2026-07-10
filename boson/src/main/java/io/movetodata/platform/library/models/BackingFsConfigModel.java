package io.movetodata.platform.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "platform_config_backingfs")
public class BackingFsConfigModel {
    public @Id
    String config;
    //for all 4 types "localfs|hdfs|s3|gs"
    private String fsType;

    private String gsBucket;
    @Column(columnDefinition = "TEXT")
    private String gsCredentials;

    private String localFs;

    private String hdfs;

    private String s3Bucket;
    private String s3AccessKey;
    private String s3SecretKey;
}
