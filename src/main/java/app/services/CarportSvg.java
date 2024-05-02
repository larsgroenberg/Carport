package app.services;

public class CarportSvg {
    private int length;
    private int width;
    private int height;
    private Svg carportSvg;
    private Svg carportOuterSvg;

    public CarportSvg(int width, int length, int height)
    {
        this.length = length;
        this.width = width;
        this.height = height;
        carportSvg = new Svg(75, 0, "0 0 900 900", "100%");
        carportOuterSvg = new Svg(0, 0, "0 0 900 900", "100%");

        carportSvg.addRectangle(0,0, width, length,"stroke-width:1px; stroke:#000000; fill: #ffffff");
        addBeams(length, width);
        addRafters(width, length);
        addLine(width, length);
        addPoles(width, length);
        addArrows(width, length);
        addText(0, width/2, -90, "6.00 cm" );
        addText(length/2, width+75, 0, "7.50 cm" );
    }

    private void addBeams(int width, int length) {
        carportSvg.addRectangle(0,35,4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0,(length-35),4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }

    private void addRafters(int width, int length) {
        for (double i = 0; i < length; i+= 55.714)
        {
            carportSvg.addRectangle(i, 0.0, width, 4.5,"stroke:#000000; fill: #ffffff" );
        }
    }

    private void addLine(int width, int length) {
        carportSvg.addLine(55, 35, length-55, width-30,"stroke:#000000; fill: #ffffff" );
        carportSvg.addLine(55, width-30.5, length-55, 35,"stroke:#000000; fill: #ffffff" );
    }

    public void addPoles(int width, int length) {
        for (int h = 53; h < length; h += 300) {
            carportSvg.addRectangle(h,31, 12,12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(h,width-38, 12,12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        }
    }

    public void addArrows(int width, int length) {
        carportOuterSvg.addArrow( 50, width,50,0, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow( 75, width+50, length+75,width+50, "stroke:#000000; marker-end: url(#endArrow);");
     }

    public void addText(int width, int length, int rotate, String text) {
        carportOuterSvg.addText(width, length, rotate, text, "text-anchor: middle; transform: rotate(-90);");
        //addText(0, width / 2, -90, "6.00 cm", "text-anchor: middle; transform: translate(30,300) rotate(-90);");

    }

    @Override
    public String toString() {
        carportOuterSvg.addSvg(carportSvg);
        System.out.println(carportOuterSvg);
        return carportOuterSvg.toString();
    }

}