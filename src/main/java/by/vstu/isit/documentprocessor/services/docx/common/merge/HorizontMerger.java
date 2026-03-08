package by.vstu.isit.documentprocessor.services.docx.common.merge;

import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.math.BigInteger;

/**
 * Вспомогательный интерфейс для горизонтального объединения ячеек в строке таблицы.
 */
public interface HorizontMerger {
    /**
     * Объединяет ячейки по горизонтали, начиная с заданной колонки.
     *
     * @param row  строка таблицы
     * @param col  индекс стартовой колонки
     * @param span количество объединяемых ячеек
     */
    @SuppressWarnings("SameParameterValue")
    default void mergeHorizontal(XWPFTableRow row, int col, int span) {
        var cell = row.getCell(col);
        if (cell == null) {
            return;
        }

        var tcPr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        if (tcPr.isSetGridSpan()) {
            tcPr.getGridSpan().setVal(BigInteger.valueOf(span));
        } else {
            tcPr.addNewGridSpan().setVal(BigInteger.valueOf(span));
        }
    }
}

