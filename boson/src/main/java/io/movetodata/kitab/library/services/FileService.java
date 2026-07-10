package io.movetodata.kitab.library.services;

import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.sharedutils.CommonService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;

@Service
public class FileService {
    @Autowired
    ResourceService resourceService;

    public Map<String, Object> getFileInputStream(UUID id) throws Exception {
        ResourceModel resourceModel = resourceService.findById(id).orElseThrow();

        Map<String, Object> mp = new HashMap<>();
        InputStream inputStream = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ResponseEntity<Object> responseEntity = null;

        String backingFS = System.getenv("BACKING_FS");

        mp.put("headers", headers);
        switch (backingFS) {
            case "s3":
                throw new Exception("s3 not supported yet");
            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/file/" + id + "/file/" + resourceModel.getId());
                Blob blob = storage.get(blobId);
                inputStream = new ByteArrayInputStream(blob.getContent());
                headers.setContentDispositionFormData("attachment", resourceModel.getName());
                mp.put("stream", new InputStreamResource(inputStream));
                return mp;
            case "hdfs":
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                FileSystem fs = FileSystem.get(conf);
                FSDataInputStream fileOpen = fs.open(new Path("/movetodata/file/" + id + "/file/" + resourceModel.getId()));
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", resourceModel.getName());
                mp.put("stream", new InputStreamResource(fileOpen));
                return mp;
            case "localfs":
                FileSystemResource resource = new FileSystemResource(getResourcePath("file", id, "file") + "/" + id);
                inputStream = resource.getInputStream();
                headers.setContentDispositionFormData("attachment", resourceModel.getName());
                mp.put("stream", new InputStreamResource(inputStream));
                return mp;
            default:
                throw new Exception("Unknown backing FS ");
        }

    }
}
