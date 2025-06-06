package io.bosler.connect.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rest_api_source_config")
public class RestAPISourceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToMany(targetEntity = RestAPISourceDomain.class, cascade = CascadeType.ALL)
    private List<RestAPISourceDomain> domains = new ArrayList<>();
}
