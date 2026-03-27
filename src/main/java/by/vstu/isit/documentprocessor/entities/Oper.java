package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@Table(name = "oper")
@AttributeOverride(name = "id", column = @Column(name = "idOper"))
public class Oper extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * КП-1, Пу-1
     */
    @Column(name = "NumOper", nullable = false)
    private String numOper;

    /**
     * РИ шапка 15-132/025, где 15-ном цеха, 132-порядковый номер для РИ данного цеха вводиться операт
     * ВИ-3
     */
    @Column(name = "NomInstr", nullable = false)
    private String nomInstr;

    /**
     * ПУ-3 (всё списком вместе с OstnasInstr) Используемое оборудование
     * Отдельная графа в РИ
     */
    @Column(name = "Oborud")
    private String oborud;

    /**
     * Используемая остнаска и инструмент
     * ПУ-3 (всё списком вместе с Oborud) Используемое оборудование
     * Отдельная графа в РИ
     */
    @Column(name = "OstnasInstr")
    private String ostnasInstr;

    /**
     * КП-8 (часть строки), Пу-2
     */
    @Column(name = "Name")
    private String name;

    /**
     * РИ Шапка - часть наименования
     * ВИ-2  часть строки
     */
    @Column(name = "Shifr")
    private String shifr;

    /**
     * Номер цеха
     */
    @Column(name = "NumZech")
    private String numZech;

    @ManyToOne
    @JoinColumn(name = "idDocPackage")
    private Docpackage docpackage;

    @ManyToOne
    @JoinColumn(name = "idTypeOper")
    private TypeOper typeOper;

    @OneToMany(mappedBy = "oper", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @ToString.Exclude
    private List<Func> funcs;

}
