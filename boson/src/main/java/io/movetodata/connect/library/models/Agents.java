package io.movetodata.connect.library.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.movetodata.kitab.library.models.IResource;
import io.movetodata.sharedutils.MoveToDataUtils;
import io.movetodata.sharedutils.Serializers.ResourceSerializer;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "connect_agent")
@JsonSerialize(using = ResourceSerializer.class)
public class Agents implements MoveToDataUtils, IResource {
    @Id
    @NotEmpty
    public UUID id;

    @Builder.Default
    private boolean proxy = false;
    private String httpProxy;
    private String httpsProxy;
    private Date lastStatus;
}
