package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.math.BigInteger;

public interface HorizontMerger {
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
