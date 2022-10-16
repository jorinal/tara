package com.sondertara.excel.fast.writer;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

class Comments {
    private static final String COLOR = "#ffffee";
    private final Map<Ref, String> cache = new TreeMap<>();

    void set(int r, int c, String comment) {
        cache.put(new Ref(r, c), comment);
    }

    boolean isEmpty() {
        return cache.isEmpty();
    }

    void writeComments(Writer w) throws IOException {
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        w.append("<comments xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        w.append("<authors><author/></authors>");
        w.append("<commentList>");
        for (Map.Entry<Ref, String> entry : cache.entrySet()) {
            Ref ref = entry.getKey();
            w.append("<comment ref=\"");
            w.append(Range.colToString(ref.col));
            w.append(ref.row + 1);
            w.append("\" authorId=\"0\"><text><t>");
            w.appendEscaped(entry.getValue());
            w.append("</t></text></comment>");
        }
        w.append("</commentList></comments>");
    }

    void writeVmlDrawing(Writer w) throws IOException {
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        w.append("<xml xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\"");
        w.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">");
        w.append("<o:shapelayout v:ext=\"edit\"><o:idmap v:ext=\"edit\" data=\"1\"/></o:shapelayout>");
        w.append("<v:shapetype id=\"c\" coordsize=\"21600,21600\" o:spt=\"202.0\" path=\"m,l,21600r21600,l21600,xe\">");
        w.append("<v:stroke joinstyle=\"miter\"/><v:path gradientshapeok=\"t\" o:connecttype=\"rect\"/>");
        w.append("</v:shapetype>");
        int id = 0;
        for (Map.Entry<Ref, String> entry : cache.entrySet()) {
            Ref ref = entry.getKey();
            w.append("<v:shape id=\"s");
            w.append(id++);
            w.append("\" type=\"#c\" style=\"position:absolute; visibility:hidden\" fillcolor=\"" + COLOR + "\" o:insetmode=\"auto\">");
            w.append("<v:fill color=\"" + COLOR + "\"/><v:shadow on=\"t\" color=\"black\" obscured=\"t\"/><v:path o:connecttype=\"none\"/>");
            w.append("<v:textbox style=\"mso-direction-alt:auto\"/>");
            w.append("<x:ClientData ObjectType=\"Note\">");
            w.append("<x:MoveWithCells/><x:SizeWithCells/>");
            w.append("<x:Anchor>");
            w.append(ref.col).append(',');
            w.append("0,");
            w.append(ref.row).append(',');
            w.append("0,");
            w.append(ref.col + 2).append(',');
            w.append("0,");
            w.append(ref.row + 2).append(',');
            w.append("0");
            w.append("</x:Anchor>");
            w.append("<x:AutoFill>False</x:AutoFill>");
            w.append("<x:Row>").append(ref.row).append("</x:Row><x:Column>").append(ref.col).append("</x:Column>");
            w.append("</x:ClientData></v:shape>");
        }
        w.append("</xml>");

    }

    void writeDrawing(Writer w) throws IOException {
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        w.append("<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\"/>");
    }

    private static class Ref implements Comparable<Ref> {
        final int row;
        final int col;

        public Ref(int row, int col) {
            this.row = row;
            this.col = col;
        }

        private int getRow() {
            return row;
        }

        private int getCol() {
            return col;
        }

        @Override
        public int compareTo(Ref o) {
            return Comparator.comparingInt(Ref::getRow)
                    .thenComparing(Ref::getCol)
                    .compare(this, o);
        }
    }

}


