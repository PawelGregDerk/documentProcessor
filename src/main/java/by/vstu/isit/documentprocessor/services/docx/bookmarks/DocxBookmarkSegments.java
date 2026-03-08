package by.vstu.isit.documentprocessor.services.docx.bookmarks;

import java.util.List;

public final class DocxBookmarkSegments {
    private DocxBookmarkSegments() {
    }

    public static void removeTrailingBreak(List<Docx4jBookmarkWriter.CellSegment> segments) {
        if (segments.isEmpty()) {
            return;
        }
        var last = segments.remove(segments.size() - 1);
        segments.add(new Docx4jBookmarkWriter.CellSegment(
                last.bookmark(), last.text(), last.bold(), false));
    }
}

