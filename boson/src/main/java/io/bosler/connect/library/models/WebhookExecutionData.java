package io.bosler.connect.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "webhook_execution_data")
public class WebhookExecutionData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID webhookId;
    private Date executedAt;
    private UUID executedBy;

    @OneToMany(targetEntity = WebhookCallData.class, cascade = CascadeType.ALL)
    private List<WebhookCallData> calls;
}
