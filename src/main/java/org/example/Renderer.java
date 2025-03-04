package org.example;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private Grid grid;

    public Renderer() {
        this.grid = new Grid();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void drawLine(Line line) {
        glLineWidth(2.0f); // Збільшуємо товщину лінії для кращої видимості
        glBegin(GL_LINES);
        glColor3f(line.getColor()[0], line.getColor()[1], line.getColor()[2]);
        glVertex2f(line.getStart().getX(), line.getStart().getY());
        glVertex2f(line.getEnd().getX(), line.getEnd().getY());
        glEnd();

        // Малюємо точки кінців відрізка для кращої видимості
        drawPoint(line.getStart(), line.getColor(), 5.0f);
        drawPoint(line.getEnd(), line.getColor(), 5.0f);
    }

    public void drawGrid(int width, int height) {
        if (grid != null) {
            grid.draw(width, height);
        }
    }

    public void drawPoint(Point point, float[] color, float size) {
        glPointSize(size);
        glBegin(GL_POINTS);
        glColor3f(color[0], color[1], color[2]);
        glVertex2f(point.getX(), point.getY());
        glEnd();
    }

    public void drawIntersectionPoint(Point point) {
        if (point != null) {
            // Малюємо точку перетину жовтим кольором
            float[] color = {1.0f, 1.0f, 0.0f}; // Жовтий
            drawPoint(point, color, 7.0f);
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}