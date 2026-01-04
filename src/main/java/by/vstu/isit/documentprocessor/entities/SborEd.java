package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

import org.checkerframework.checker.units.qual.C;

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
