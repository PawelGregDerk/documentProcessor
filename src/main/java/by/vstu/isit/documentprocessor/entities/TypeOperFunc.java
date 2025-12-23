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
@Table(name = "type_oper_func")
public class TypeOperFunc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Операция
     */
    @Id
    @Column(name = "idTypeFunc", nullable = false)
    private Integer idTypeFunc;

    /**
     * ПУ-5
     * PFEMA-8
     * <p>
     * Описание функции
     * Функция элемента работы процесса
     */
    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "idTypeOper", nullable = false)
    private Integer idTypeOper;

    /**
     * ПУ-8
     * Продукции процесса, технические требования допуск
     */
    @Column(name = "Param", nullable = false)
    private String param;

    /**
     * В ПУ-5  КП-10 (Продукция) если Prod=true
     * 1.Наличие выходных импульсных сигналов
     * 2. Срок хранения цианоакрила клея
     * В ПУ-6 КП-11 (Процесс) если Prod=false
     * 3  Напряжение питания,
     */
    @Column(name = "isProd", nullable = false)
    private Boolean isProd;

    /**
     * FEMA-18  ПУ-7 Специальные характеристики
     * Если не null то операция отображается в СХПУ (СХПУ-4)
     * Спросить???
     */
    @Column(name = "SpecCharakt")
    private String specCharakt;

}
