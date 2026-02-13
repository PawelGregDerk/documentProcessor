package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@Table(name = "SborEd")
public class SborEd extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    @Column(name = "Nazv", nullable = false)
    private String nazv;

    @Column(name = "Oboznach", nullable = false)
    private String oboznach;

    @ManyToOne
    @JoinColumn(name = "idDocPackage")
    private Docpackage docpackage;

}
