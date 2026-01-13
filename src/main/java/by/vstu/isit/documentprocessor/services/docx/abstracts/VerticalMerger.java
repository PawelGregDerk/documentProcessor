package by.vstu.isit.documentprocessor.services.docx.abstracts;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

public interface VerticalMerger {
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
