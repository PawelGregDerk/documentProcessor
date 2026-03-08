package by.vstu.isit.documentprocessor.services.docx.bookmarks;

import org.docx4j.XmlUtils;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * РЈС‚РёР»РёС‚Р° РґР»СЏ СѓРїСЂР°РІР»РµРЅРёСЏ Р·Р°РєР»Р°РґРєР°РјРё docx4j РІРЅСѓС‚СЂРё РґРѕРєСѓРјРµРЅС‚Р°.
 */
final class Docx4jBookmarkHelper {
    private final WordprocessingMLPackage doc;

    Docx4jBookmarkHelper(WordprocessingMLPackage doc) {
        this.doc = doc;
    }

    void removeBookmarkIfExists(String name) {
        Set<BigInteger> ids = new HashSet<>();
        collectBookmarkIds(doc.getMainDocumentPart().getContent(), name, ids);
        for (SectionWrapper section : doc.getDocumentModel().getSections()) {
            if (section.getHeaderFooterPolicy() == null) {
                continue;
            }
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultHeader(), name, ids);
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultFooter(), name, ids);
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getEvenHeader(), name, ids);
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getEvenFooter(), name, ids);
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getFirstHeader(), name, ids);
            collectFromHeaderFooter(section.getHeaderFooterPolicy().getFirstFooter(), name, ids);
        }
        if (ids.isEmpty()) {
            return;
        }
        removeBookmarkIds(doc.getMainDocumentPart().getContent(), ids);
        for (SectionWrapper section : doc.getDocumentModel().getSections()) {
            if (section.getHeaderFooterPolicy() == null) {
                continue;
            }
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultHeader(), ids);
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultFooter(), ids);
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getEvenHeader(), ids);
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getEvenFooter(), ids);
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getFirstHeader(), ids);
            removeFromHeaderFooter(section.getHeaderFooterPolicy().getFirstFooter(), ids);
        }
    }

    BigInteger maxBookmarkId() {
        Set<BigInteger> ids = new HashSet<>();
        collectAllBookmarkIds(doc.getMainDocumentPart().getContent(), ids);
        for (SectionWrapper section : doc.getDocumentModel().getSections()) {
            if (section.getHeaderFooterPolicy() == null) {
                continue;
            }
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultHeader(), ids);
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getDefaultFooter(), ids);
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getEvenHeader(), ids);
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getEvenFooter(), ids);
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getFirstHeader(), ids);
            collectAllFromHeaderFooter(section.getHeaderFooterPolicy().getFirstFooter(), ids);
        }
        return ids.stream().max(BigInteger::compareTo).orElse(BigInteger.ZERO);
    }

    private static void collectFromHeaderFooter(HeaderPart header, String name, Set<BigInteger> ids) {
        if (header == null) {
            return;
        }
        Hdr hdr = header.getJaxbElement();
        collectBookmarkIds(hdr, name, ids);
    }

    private static void collectFromHeaderFooter(FooterPart footer, String name, Set<BigInteger> ids) {
        if (footer == null) {
            return;
        }
        Ftr ftr = footer.getJaxbElement();
        collectBookmarkIds(ftr, name, ids);
    }

    private static void removeFromHeaderFooter(HeaderPart header, Set<BigInteger> ids) {
        if (header == null) {
            return;
        }
        removeBookmarkIds(header.getJaxbElement(), ids);
    }

    private static void removeFromHeaderFooter(FooterPart footer, Set<BigInteger> ids) {
        if (footer == null) {
            return;
        }
        removeBookmarkIds(footer.getJaxbElement(), ids);
    }

    private static void collectAllFromHeaderFooter(HeaderPart header, Set<BigInteger> ids) {
        if (header == null) {
            return;
        }
        collectAllBookmarkIds(header.getJaxbElement(), ids);
    }

    private static void collectAllFromHeaderFooter(FooterPart footer, Set<BigInteger> ids) {
        if (footer == null) {
            return;
        }
        collectAllBookmarkIds(footer.getJaxbElement(), ids);
    }

    private static void collectBookmarkIds(List<Object> content, String name, Set<BigInteger> ids) {
        for (Object child : content) {
            Object unwrapped = XmlUtils.unwrap(child);
            if (unwrapped instanceof CTBookmark bookmark) {
                if (name.equals(bookmark.getName())) {
                    ids.add(bookmark.getId());
                }
            }
            if (unwrapped instanceof ContentAccessor accessor) {
                collectBookmarkIds(accessor.getContent(), name, ids);
            }
        }
    }

    private static void collectBookmarkIds(Object root, String name, Set<BigInteger> ids) {
        Object unwrapped = XmlUtils.unwrap(root);
        if (unwrapped instanceof ContentAccessor accessor) {
            collectBookmarkIds(accessor.getContent(), name, ids);
        }
    }

    private static void collectAllBookmarkIds(List<Object> content, Set<BigInteger> ids) {
        for (Object child : content) {
            Object unwrapped = XmlUtils.unwrap(child);
            if (unwrapped instanceof CTBookmark bookmark) {
                ids.add(bookmark.getId());
            }
            if (unwrapped instanceof ContentAccessor accessor) {
                collectAllBookmarkIds(accessor.getContent(), ids);
            }
        }
    }

    private static void collectAllBookmarkIds(Object root, Set<BigInteger> ids) {
        Object unwrapped = XmlUtils.unwrap(root);
        if (unwrapped instanceof ContentAccessor accessor) {
            collectAllBookmarkIds(accessor.getContent(), ids);
        }
    }

    private static void removeBookmarkIds(List<Object> content, Set<BigInteger> ids) {
        for (int i = content.size() - 1; i >= 0; i--) {
            Object child = content.get(i);
            Object childUnwrapped = XmlUtils.unwrap(child);
            if (childUnwrapped instanceof CTBookmark bookmark && ids.contains(bookmark.getId())) {
                content.remove(i);
                continue;
            }
            if (childUnwrapped instanceof CTMarkupRange range && ids.contains(range.getId())) {
                content.remove(i);
                continue;
            }
            if (childUnwrapped instanceof ContentAccessor accessor) {
                removeBookmarkIds(accessor.getContent(), ids);
            }
        }
    }

    private static void removeBookmarkIds(Object root, Set<BigInteger> ids) {
        Object unwrapped = XmlUtils.unwrap(root);
        if (unwrapped instanceof ContentAccessor accessor) {
            removeBookmarkIds(accessor.getContent(), ids);
        }
    }
}

