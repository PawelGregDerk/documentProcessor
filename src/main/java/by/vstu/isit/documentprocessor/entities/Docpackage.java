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
@Table(name = "docpackage")
@AttributeOverride(name = "id", column = @Column(name = "idDocPackage"))
public class Docpackage extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    @Column(name = "PackageName", nullable = false)
    private String packageName;

    @Column(name = "Path", nullable = false)
    private String path;

    /**
     * 'План управления
     * ПУ 0360, где 0360-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "PUName", nullable = false)
    private String puName;

    /**
     * Спец. характеристики плана управления
     * СХПУ 0002, где 0002-порядковый номер спец. характеристик плана управления, вводиться оператором
     */
    @Column(name = "SPUName", nullable = false)
    private String spuName;

    /**
     * •	Карта потока
     * КП 0309, где 0309-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "KPName", nullable = false)
    private String kpName;

    /**
     * FMEA
     * например, Т0363, где 0363-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "FMEAName", nullable = false)
    private String fmeaName;

    /**
     * 15-132, где 15-номер цеха, 132-порядковый номер для рабочих инструкций данного цеха, вводиться оператором
     */
    @Column(name = "VedIName", nullable = false)
    private String vedIName;

    @OneToMany(mappedBy = "docpackage", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Oper> opers;

    @OneToMany(mappedBy = "docpackage", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SborEd> sborEds;

}
