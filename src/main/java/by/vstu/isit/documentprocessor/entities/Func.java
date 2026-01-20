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
@Table(name = "func")
@AttributeOverride(name = "id", column = @Column(name = "idFunc"))
public class Func extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * ПУ-5
     * PFEMA-8
     * <p>
     * Описание функции
     * Функция элемента работы процесса
     */
    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "idOper", nullable = false, insertable = false, updatable = false)
    private Long idOper;

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

    @ManyToOne
    @JoinColumn(name = "idOper")
    private Oper oper;

}
