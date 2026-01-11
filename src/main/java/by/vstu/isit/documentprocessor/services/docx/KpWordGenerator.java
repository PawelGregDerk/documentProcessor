package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE;
import static org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART;

@Service
public class KpWordGenerator {
    private static final int COLUMN_COUNT = 11;
    private static final int DATA_START_ROW = 2;

    public void generate(DockPackageDto dto) throws Exception {
    }
}
