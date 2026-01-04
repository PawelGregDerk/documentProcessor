package by.vstu.isit.documentprocessor.services.docx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.util.List;

public interface WordTableService<T> {

    XWPFDocument getDocument();

    XWPFTable getTable(int index);

    List<XWPFTable> getAllTables();

    T read(XWPFTable table);

    void write(XWPFTable table, T model);
}

