package io.movetodata.utils;

import io.minio.messages.Item;
import io.minio.messages.Owner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinioItem {

    private String objectName;
    private ZonedDateTime lastModified;
    private String etag;
    private long size;
    private String storageClass;
    private Owner owner;
    private Item item;
    private String type;

    public MinioItem(Item item) {
        this.objectName = item.objectName();
        this.lastModified = item.lastModified();
        this.etag = item.etag();
        this.size = item.size();
        this.storageClass = item.storageClass();
        this.owner = item.owner();
        this.type = item.isDir() ? "directory" : "file";
    }
}
