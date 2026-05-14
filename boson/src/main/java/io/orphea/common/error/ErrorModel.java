package io.orphea.common.error;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "platform_error")
public class ErrorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stack;

    @Column(columnDefinition = "TEXT")
    private String componentStack;
}