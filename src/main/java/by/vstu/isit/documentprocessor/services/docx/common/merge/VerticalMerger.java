package by.vstu.isit.documentprocessor.services.docx.common.merge;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

/**
 * Вспомогательный интерфейс для вертикального объединения ячеек в таблице.
 */
public interface VerticalMerger {
    /**
     * Объединяет ячейки в заданной колонке по вертикали.
     *
     * @param table таблица
     * @param start первая строка диапазона
     * @param end   последняя строка диапазона
     * @param col   индекс колонки
     */
    default void mergeVertical(XWPFTable table, int start, int end, int col) {
        for (int r = start; r <= end; r++) {
            var row = table.getRow(r);
            if (row == null) {
                continue;
            }

            var cell = row.getCell(col);
            if (cell == null) {
                continue;
            }

            var tcPr = cell.getCTTc().isSetTcPr()
                    ? cell.getCTTc().getTcPr()
                    : cell.getCTTc().addNewTcPr();
            var merge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
            merge.setVal(r == start ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }
}

