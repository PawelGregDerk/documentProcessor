package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Table(name = "SborEd")
public class SborEd implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

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
