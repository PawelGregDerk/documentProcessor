package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "SborEd")
public class SborEd implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_docpackage", nullable = false, insertable = false, updatable = false)
    private Long idDocpackage;

    @Column(name = "Nazv", nullable = false)
    private String nazv;

    @Column(name = "Oboznach", nullable = false)
    private String oboznach;

    @ManyToOne
    @JoinColumn(name = "id_docpackage")
    private Docpackage docpackage;

}
