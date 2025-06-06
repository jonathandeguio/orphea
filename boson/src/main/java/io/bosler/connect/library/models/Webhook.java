package io.bosler.connect.library.models;

import io.bosler.dataset.library.models.CsvPreprocessingModel;
import io.bosler.kitab.library.models.IResource;
import io.bosler.sharedutils.BoslerUtils;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
//@JsonSerialize(using = ResourceSerializer.class)
@Table(name = "connect_webhook")
public class Webhook implements BoslerUtils, IResource {
    @Id
    private UUID id;
    private UUID sourceId;

    @OneToMany(targetEntity = RestAPIRequest.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("requestOrder ASC")
    private List<RestAPIRequest> requests;

    private String responseParam = "@completeresponse";

    @OneToOne(targetEntity = CsvPreprocessingModel.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private CsvPreprocessingModel csvPreprocessing;
}
