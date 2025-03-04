package org.example;

public class Line {
    private Point start;
    private Point end;
    private float[] color;

    public Line(Point start, Point end, float[] color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public Point getStart() { return start; }
    public Point getEnd() { return end; }
    public float[] getColor() { return color; }

    public void setStart(Point start) { this.start = start; }
    public void setEnd(Point end) { this.end = end; }
    public void setColor(float[] color) { this.color = color; }
}
