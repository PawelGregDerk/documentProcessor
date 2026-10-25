package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class HorizontMergerTest {

    private final HorizontMerger merger = new HorizontMerger() {};

    @Test
    @DisplayName("mergeHorizontal: sets gridSpan on existing cell")
    void mergeHorizontal_validCell_setsGridSpan() {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 3);
            XWPFTableRow row = table.getRow(0);

            merger.mergeHorizontal(row, 0, 2);

            var tcPr = row.getCell(0).getCTTc().getTcPr();
            assertNotNull(tcPr);
            assertTrue(tcPr.isSetGridSpan());
            assertEquals(BigInteger.valueOf(2), tcPr.getGridSpan().getVal());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("mergeHorizontal: updates existing gridSpan when already set")
    void mergeHorizontal_alreadyHasGridSpan_updatesValue() {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 3);
            XWPFTableRow row = table.getRow(0);

            merger.mergeHorizontal(row, 1, 2);
            merger.mergeHorizontal(row, 1, 3);

            var tcPr = row.getCell(1).getCTTc().getTcPr();
            assertEquals(BigInteger.valueOf(3), tcPr.getGridSpan().getVal());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("mergeHorizontal: cell is out of bounds (null) does not throw exception")
    void mergeHorizontal_invalidColumn_doesNothing() {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 2);
            XWPFTableRow row = table.getRow(0);

            assertDoesNotThrow(() -> merger.mergeHorizontal(row, 99, 2));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
