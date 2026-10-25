package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import static org.junit.jupiter.api.Assertions.*;

class VerticalMergerTest {

    private final VerticalMerger merger = new VerticalMerger() {};

    @Test
    @DisplayName("mergeVertical: sets RESTART on start row and CONTINUE on subsequent rows")
    void mergeVertical_validRange_setsRestartAndContinue() {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(4, 2);

            merger.mergeVertical(table, 1, 3, 0);

            var startCell = table.getRow(1).getCell(0).getCTTc().getTcPr().getVMerge();
            var middleCell = table.getRow(2).getCell(0).getCTTc().getTcPr().getVMerge();
            var endCell = table.getRow(3).getCell(0).getCTTc().getTcPr().getVMerge();

            assertEquals(STMerge.RESTART, startCell.getVal());
            assertEquals(STMerge.CONTINUE, middleCell.getVal());
            assertEquals(STMerge.CONTINUE, endCell.getVal());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("mergeVertical: handles out-of-bounds rows or columns gracefully without error")
    void mergeVertical_outOfBoundsRowOrCol_handlesGracefully() {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(2, 2);

            assertDoesNotThrow(() -> merger.mergeVertical(table, 0, 5, 0));
            assertDoesNotThrow(() -> merger.mergeVertical(table, 0, 1, 10));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
