package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@Table(name = "type_oper")
@AttributeOverride(name = "id", column = @Column(name = "idTypeOper"))
public class TypeOper extends AbstractEntity<Long> {

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

    @OneToMany(mappedBy = "typeOper", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Oper> opers;

    @OneToMany(mappedBy = "typeOper", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<TypeOperFunc> typeOperFuncs;

}
