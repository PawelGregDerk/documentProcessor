package by.vstu.isit.documentprocessor.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "type_oper")
public class TypeOper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Операция
     */
    @Id
    @Column(name = "idTypeOper", nullable = false)
    private Long idTypeOper;

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

}
