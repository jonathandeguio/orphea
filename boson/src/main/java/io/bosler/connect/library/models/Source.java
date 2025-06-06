package io.bosler.connect.library.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bosler.connect.library.Serializers.SourceConfigSerializer;
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
@Table(name = "connect_sources")
@JsonSerialize(using = SourceConfigSerializer.class)
public class Source implements BoslerUtils, IResource {
    @Id
    private UUID id;

    private UUID sourceConfig;
    private String type;

    @Builder.Default
    private boolean directLoad = false;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> agentId;
}
