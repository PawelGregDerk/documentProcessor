package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@Table(name = "SborEd")
public class SborEd extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    @Column(name = "idDocPackage", nullable = false, insertable = false, updatable = false)
    private Long idDocpackage;

    @Column(name = "Nazv", nullable = false)
    private String nazv;

    @Column(name = "Oboznach", nullable = false)
    private String oboznach;

    @ManyToOne
    @JoinColumn(name = "idDocPackage")
    private Docpackage docpackage;

}
