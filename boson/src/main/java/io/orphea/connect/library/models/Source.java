package io.orphea.connect.library.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.orphea.connect.library.Serializers.SourceConfigSerializer;
import io.orphea.kitab.library.models.IResource;
import io.orphea.sharedutils.OrpheaUtils;
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
@Table(name = "connect_sources")
@JsonSerialize(using = SourceConfigSerializer.class)
public class Source implements OrpheaUtils, IResource {
    @Id
    private UUID id;

    private UUID sourceConfig;
    private String type;

    @Builder.Default
    private boolean directLoad = false;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> agentId;
}
