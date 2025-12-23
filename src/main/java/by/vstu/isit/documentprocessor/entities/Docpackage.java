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
@Table(name = "docpackage")
public class Docpackage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "idDocPackage", nullable = false)
    private Long idDocPackage;

    @Column(name = "PackageName", nullable = false)
    private String packageName;

    @Column(name = "Path", nullable = false)
    private String path;

    /**
     * 'План управления
     * ПУ 0360, где 0360-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "PUName", nullable = false)
    private String PUName;

    /**
     * Спец. характеристики плана управления
     * СХПУ 0002, где 0002-порядковый номер спец. характеристик плана управления, вводиться оператором
     */
    @Column(name = "SPUName", nullable = false)
    private String SPUName;

    /**
     * •	Карта потока
     * КП 0309, где 0309-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "KPName", nullable = false)
    private String KPName;

    /**
     * FMEA
     * например, Т0363, где 0363-порядковый номер плана управления, вводиться оператором
     */
    @Column(name = "FEMAName", nullable = false)
    private String FEMAName;

    /**
     * 15-132, где 15-номер цеха, 132-порядковый номер для рабочих инструкций данного цеха, вводиться оператором
     */
    @Column(name = "VedIName", nullable = false)
    private String vedIName;

}
