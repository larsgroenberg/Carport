package app.services;

public class Svg {
    private static final String SVG_TEMPLATE = "<svg version=\"1.1\"\n" +
            "     x=\"%d\" y=\"%d\"\n" +
            "     viewBox=\"%s\"  width=\"%s\" \n" +
            "     preserveAspectRatio=\"xMinYMin\">";

    private static final String SVG_ARROW_DEFS = "<defs>\n" +
            "        <marker id=\"beginArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"0\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,6 L12,0 L12,12 L0,6\" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "        <marker id=\"endArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"12\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,0 L12,6 L0,12 L0,0 \" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "    </defs>";

    private static final String SVG_RECT_TEMPLATE = "<rect x=\"%.2f\" y=\"%.2f\" height=\"%f\" width=\"%f\" style=\"%s\" />";
    private static final String SVG_LINE_TEMPLATE = "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" style=\"%s;\" />";
    private static final String SVG_LINE_TEMPLATE_WITH_STROKE = "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" style=\"%s; stroke-dasharray: 4 4;\" />\n";
    private static final String SVG_ARROW_TEMPLATE = "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" style=\"stroke:#000000;\n" +
            "marker-start: url(#beginArrow);\n" + "marker-end: url(#endArrow);\"/>\n";
    public static final String VERTICAL_TEXT_TEMPLATE = "<text x=\"%.2f\" y=\"%d\" transform=\"rotate(%d)\">%s</text>\n";
    public static final String HORIZONTAL_TEXT_TEMPLATE = "<text x=\"%.2f\" y=\"%d\" transform=\"rotate(%d)\" style=\"writing-mode: tb;\">%s</text>\n";
    private StringBuilder svg = new StringBuilder();

    public Svg(int x, int y, String viewBox, String width) {
        svg.append(String.format(SVG_TEMPLATE, x, y, viewBox, width));
        svg.append(SVG_ARROW_DEFS);
    }

    public void addRectangle(double x, double y, double length, double width, String style) {
        svg.append(String.format(SVG_RECT_TEMPLATE, x, y, length, width, style));
    }

    public void addLineWithStroke(double x1, double y1, double x2, double y2, String style) {
        svg.append(String.format(SVG_LINE_TEMPLATE_WITH_STROKE, x1, y1, x2, y2, style));
    }
    public void addLine(double x1, double y1, double x2, double y2, String style) {
        svg.append(String.format(SVG_LINE_TEMPLATE, x1, y1, x2, y2, style));
    }

    public void addArrow(double x1, double y1, double x2, double y2, String style) {
        svg.append(String.format(SVG_ARROW_TEMPLATE, x1, y1, x2, y2, style));
    }

    public void addTextH(double x, int y, int rotation, String text, String style) {
        svg.append(String.format(HORIZONTAL_TEXT_TEMPLATE, x, y, rotation, text, style));
    }

    public void addTextV(double x, int y, int rotation, String text, String style) {
        svg.append(String.format(VERTICAL_TEXT_TEMPLATE, x, y, rotation, text, style));
    }

    public void addSvg(Svg innerSvg) {
        svg.append(innerSvg.toString());
    }

    @Override
    public String toString() {
        return svg.append("</svg>").toString();
    }
}