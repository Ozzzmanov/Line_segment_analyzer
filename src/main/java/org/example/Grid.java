package org.example;

import static org.lwjgl.opengl.GL11.*;

public class Grid {
    private float gridSize = 0.1f; // Відстань між лініями сітки
    private float[] majorColor = {0.5f, 0.5f, 0.5f}; // Колір основних ліній сітки
    private float[] minorColor = {0.3f, 0.3f, 0.3f}; // Колір другорядних ліній сітки
    private float majorLineWidth = 1.5f;
    private float minorLineWidth = 0.8f;
    private float axisLineWidth = 2.0f;
    private float[] xAxisColor = {0.8f, 0.2f, 0.2f}; // Червоний для осі X
    private float[] yAxisColor = {0.2f, 0.8f, 0.2f}; // Зелений для осі Y
    private boolean showLabels = true;

    public void draw(float width, float height) {
        float aspectRatio = width / height;

        // Обчислення меж сітки на основі співвідношення сторін
        float xBound = aspectRatio > 1.0f ? aspectRatio : 1.0f;
        float yBound = aspectRatio > 1.0f ? 1.0f : 1.0f / aspectRatio;

        // Малювання другорядних ліній сітки
        drawGridLines(xBound, yBound, gridSize, minorColor, minorLineWidth);

        // Малювання основних ліній сітки (кожні 5 другорядних ліній)
        drawGridLines(xBound, yBound, gridSize * 5, majorColor, majorLineWidth);

        // Малювання осей координат
        drawAxes(xBound, yBound);
    }

    private void drawGridLines(float xBound, float yBound, float spacing, float[] color, float lineWidth) {
        glLineWidth(lineWidth);
        glBegin(GL_LINES);
        glColor3f(color[0], color[1], color[2]);

        // Вертикальні лінії (паралельні осі Y)
        for (float x = -xBound; x <= xBound; x += spacing) {
            // Пропуск лінії осі, оскільки вона буде намальована окремо
            if (Math.abs(x) < 0.001f) continue;

            glVertex2f(x, -yBound);
            glVertex2f(x, yBound);
        }

        // Горизонтальні лінії (паралельні осі X)
        for (float y = -yBound; y <= yBound; y += spacing) {
            // Пропуск лінії осі
            if (Math.abs(y) < 0.001f) continue;

            glVertex2f(-xBound, y);
            glVertex2f(xBound, y);
        }

        glEnd();
    }

    private void drawAxes(float xBound, float yBound) {
        // Малювання осі X
        glLineWidth(axisLineWidth);
        glBegin(GL_LINES);
        glColor3f(xAxisColor[0], xAxisColor[1], xAxisColor[2]);
        glVertex2f(-xBound, 0);
        glVertex2f(xBound, 0);
        glEnd();

        // Малювання осі Y
        glBegin(GL_LINES);
        glColor3f(yAxisColor[0], yAxisColor[1], yAxisColor[2]);
        glVertex2f(0, -yBound);
        glVertex2f(0, yBound);
        glEnd();

        // Малювання міток на осях
        if (showLabels) {
            drawAxisMarkers(xBound, yBound);
        }
    }

    private void drawAxisMarkers(float xBound, float yBound) {
        float markerSize = 0.02f;

        // Мітки на осі X
        for (float x = -xBound; x <= xBound; x += gridSize * 5) {
            if (Math.abs(x) < 0.001f) continue; // Пропуск нульової точки

            glLineWidth(majorLineWidth);
            glBegin(GL_LINES);
            glColor3f(xAxisColor[0], xAxisColor[1], xAxisColor[2]);
            glVertex2f(x, -markerSize);
            glVertex2f(x, markerSize);
            glEnd();
        }

        // Мітки на осі Y
        for (float y = -yBound; y <= yBound; y += gridSize * 5) {
            if (Math.abs(y) < 0.001f) continue; // Пропуск нульової точки

            glLineWidth(majorLineWidth);
            glBegin(GL_LINES);
            glColor3f(yAxisColor[0], yAxisColor[1], yAxisColor[2]);
            glVertex2f(-markerSize, y);
            glVertex2f(markerSize, y);
            glEnd();
        }
    }

    // Гетери та сетери для налаштування сітки
    public float getGridSize() {
        return gridSize;
    }

    public void setGridSize(float gridSize) {
        this.gridSize = gridSize;
    }

    public float[] getMajorColor() {
        return majorColor;
    }

    public void setMajorColor(float[] majorColor) {
        this.majorColor = majorColor;
    }

    public float[] getMinorColor() {
        return minorColor;
    }

    public void setMinorColor(float[] minorColor) {
        this.minorColor = minorColor;
    }

    public float getMajorLineWidth() {
        return majorLineWidth;
    }

    public float getMinorLineWidth() {
        return minorLineWidth;
    }

    public float getAxisLineWidth() {
        return axisLineWidth;
    }

    public void setLineWidths(float major, float minor, float axis) {
        this.majorLineWidth = major;
        this.minorLineWidth = minor;
        this.axisLineWidth = axis;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public float[] getXAxisColor() {
        return xAxisColor;
    }

    public void setXAxisColor(float[] xAxisColor) {
        this.xAxisColor = xAxisColor;
    }

    public float[] getYAxisColor() {
        return yAxisColor;
    }

    public void setYAxisColor(float[] yAxisColor) {
        this.yAxisColor = yAxisColor;
    }
}