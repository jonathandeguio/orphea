package io.orphea.kitab.library.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kitab_favourites")
public class Favourites implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    UUID userId;
    UUID resourceId;

    @CreationTimestamp
    public Date createdAt = new Date();
}
