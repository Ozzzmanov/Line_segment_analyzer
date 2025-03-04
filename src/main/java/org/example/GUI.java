package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.glfw.GLFW.*;


public class GUI {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private long window;
    private Line line1, line2;
    private LineAnalyzer analyzer;
    private Grid grid;

    public static final int WIDTH_TEXT_VIEW = 100;

    // Стан перетягування мишею
    private boolean dragging = false;
    private Point dragPoint = null;
    private Line dragLine = null;
    private boolean isStartPoint = false;
    private float worldMouseX, worldMouseY;

    // Поля введення для ліній
    private final ImFloat inputX1 = new ImFloat();
    private final ImFloat inputY1 = new ImFloat();
    private final ImFloat inputX2 = new ImFloat();
    private final ImFloat inputY2 = new ImFloat();
    private final ImFloat inputX3 = new ImFloat();
    private final ImFloat inputY3 = new ImFloat();
    private final ImFloat inputX4 = new ImFloat();
    private final ImFloat inputY4 = new ImFloat();

    // Поля для налаштування сітки
    private final ImFloat gridSizeInput = new ImFloat(0.1f);
    private final ImFloat majorLineWidthInput = new ImFloat(1.5f);
    private final ImFloat minorLineWidthInput = new ImFloat(0.8f);
    private final ImFloat axisLineWidthInput = new ImFloat(2.0f);
    private final ImBoolean showLabelsInput = new ImBoolean(true);
    private final float[] majorColorInput = {0.5f, 0.5f, 0.5f};
    private final float[] minorColorInput = {0.3f, 0.3f, 0.3f};
    private final float[] xAxisColorInput = {0.8f, 0.2f, 0.2f};
    private final float[] yAxisColorInput = {0.2f, 0.8f, 0.2f};

    public GUI(long window, Line line1, Line line2, LineAnalyzer analyzer, Grid grid) {
        this.window = window;
        this.line1 = line1;
        this.line2 = line2;
        this.analyzer = analyzer;
        this.grid = grid;

        // Ініціалізація полів введення
        updateInputFields();
        updateGridInputFields();

        // Налаштування обробників подій миші
        setupMouseCallbacks();
    }

    private void updateInputFields() {
        inputX1.set(line1.getStart().getX());
        inputY1.set(line1.getStart().getY());
        inputX2.set(line1.getEnd().getX());
        inputY2.set(line1.getEnd().getY());
        inputX3.set(line2.getStart().getX());
        inputY3.set(line2.getStart().getY());
        inputX4.set(line2.getEnd().getX());
        inputY4.set(line2.getEnd().getY());
    }

    private void updateGridInputFields() {
        // Оновлення полів з поточних значень сітки
        gridSizeInput.set(grid.getGridSize());
        majorLineWidthInput.set(grid.getMajorLineWidth());
        minorLineWidthInput.set(grid.getMinorLineWidth());
        axisLineWidthInput.set(grid.getAxisLineWidth());
        showLabelsInput.set(grid.isShowLabels());

        // Копіювання кольорів з сітки
        float[] majorColor = grid.getMajorColor();
        float[] minorColor = grid.getMinorColor();
        float[] xAxisColor = grid.getXAxisColor();
        float[] yAxisColor = grid.getYAxisColor();

        if (majorColor != null) System.arraycopy(majorColor, 0, majorColorInput, 0, 3);
        if (minorColor != null) System.arraycopy(minorColor, 0, minorColorInput, 0, 3);
        if (xAxisColor != null) System.arraycopy(xAxisColor, 0, xAxisColorInput, 0, 3);
        if (yAxisColor != null) System.arraycopy(yAxisColor, 0, yAxisColorInput, 0, 3);
    }

    private void setupMouseCallbacks() {
        // Обробник натискання кнопки миші
        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);

                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos[0], ypos[0]);

                if (action == GLFW_PRESS) {
                    // Перевіряємо, чи натиснули на точку
                    checkPointSelection();
                } else if (action == GLFW_RELEASE) {
                    // Завершуємо перетягування
                    dragging = false;
                    dragPoint = null;
                    dragLine = null;
                }
            }
        });

        // Обробник руху миші
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            if (dragging && dragPoint != null) {
                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos, ypos);

                // Оновлюємо позицію точки
                dragPoint.setX(worldMouseX);
                dragPoint.setY(worldMouseY);

                // Оновлюємо поля введення
                updateInputFields();
            }
        });
    }

    private void updateWorldMouseCoordinates(double xpos, double ypos) {
        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        glfwGetWindowSize(window, windowWidth, windowHeight);

        float screenX = (float) xpos;
        float screenY = (float) ypos;

        // Перетворення координат екрана в координати OpenGL (від -1 до 1) *Нормалізація
        float aspectRatio = (float) windowWidth[0] / windowHeight[0];
        worldMouseX = (2.0f * screenX / windowWidth[0] - 1.0f) * (aspectRatio > 1.0f ? aspectRatio : 1.0f);
        worldMouseY = (1.0f - 2.0f * screenY / windowHeight[0]) * (aspectRatio < 1.0f ? 1.0f / aspectRatio : 1.0f);
    }

    private void checkPointSelection() {
        float threshold = 0.05f; // Поріг відстані для вибору точки

        // Перевіряємо точки першого відрізка
        float distStart1 = distanceToPoint(worldMouseX, worldMouseY, line1.getStart().getX(), line1.getStart().getY());
        float distEnd1 = distanceToPoint(worldMouseX, worldMouseY, line1.getEnd().getX(), line1.getEnd().getY());

        // Перевіряємо точки другого відрізка
        float distStart2 = distanceToPoint(worldMouseX, worldMouseY, line2.getStart().getX(), line2.getStart().getY());
        float distEnd2 = distanceToPoint(worldMouseX, worldMouseY, line2.getEnd().getX(), line2.getEnd().getY());

        // Знаходимо найближчу точку
        float minDist = Math.min(Math.min(distStart1, distEnd1), Math.min(distStart2, distEnd2));

        if (minDist < threshold) {
            dragging = true;

            if (minDist == distStart1) {
                dragPoint = line1.getStart();
                dragLine = line1;
                isStartPoint = true;
            } else if (minDist == distEnd1) {
                dragPoint = line1.getEnd();
                dragLine = line1;
                isStartPoint = false;
            } else if (minDist == distStart2) {
                dragPoint = line2.getStart();
                dragLine = line2;
                isStartPoint = true;
            } else {
                dragPoint = line2.getEnd();
                dragLine = line2;
                isStartPoint = false;
            }
        }
    }
    // Евклідова відстань
    private float distanceToPoint(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void init() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        // Загрузка шрифта
        io.getFonts().clear();

        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (fontStream != null) {
                // Временное сохранение шрифта
                Path tempFont = Files.createTempFile("roboto", ".ttf");
                Files.copy(fontStream, tempFont, StandardCopyOption.REPLACE_EXISTING);

                io.getFonts().addFontFromFileTTF(tempFont.toString(), 18, io.getFonts().getGlyphRangesCyrillic());
                System.out.println("Шрифт успешно загружен");

                // Удаление временного файла
                Files.delete(tempFont);
            } else {
                System.err.println("Шрифт не найден в ресурсах");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке шрифта: " + e.getMessage());
        }

        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 150");
    }

    public void render() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderLineEditorWindow();
        renderAnalysisWindow();
        renderGridSettingsWindow();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }


    private void renderLineEditorWindow() {
        ImGui.begin("Редактор відрізків", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.textWrapped("Використовуйте слайдери, введіть координати, або перетягніть точки за допомогою миші");

        // Перша лінія
        ImGui.separator();
        ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Відрізок 1 (P1-P2) (червоний)");

        renderLineControls(line1, inputX1, inputY1, inputX2, inputY2, "1", "2");

        // Друга лінія
        ImGui.separator();
        ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, "Відрізок 2 (P3-P4) (зелений)");

        renderLineControls(line2, inputX3, inputY3, inputX4, inputY4, "3", "4");

        // Підказка щодо перетягування мишею
        ImGui.separator();
        ImGui.textColored(1.0f, 1.0f, 0.5f, 1.0f, "Підказка: Ви можете перетягувати точки за допомогою миші");

        // Показуємо статус перетягування
        if (dragging) {
            ImGui.text("Перетягування: " + (dragLine == line1 ? "Відрізок 1" : "Відрізок 2") +
                    ", точка " + (isStartPoint ? "P" + (dragLine == line1 ? "1" : "3") : "P" + (dragLine == line1 ? "2" : "4")));
        }

        ImGui.end();
    }

    private void renderLineControls(Line line, ImFloat inputX1, ImFloat inputY1, ImFloat inputX2, ImFloat inputY2, String p1Name, String p2Name) {
        float[] start = {line.getStart().getX(), line.getStart().getY()};
        float[] end = {line.getEnd().getX(), line.getEnd().getY()};

        // P1 з слайдерами та полями введення
        ImGui.text("P" + p1Name + ":");
        ImGui.pushItemWidth(200);
        if (ImGui.sliderFloat2("##Start" + p1Name, start, -1.0f, 1.0f)) {
            line.setStart(new Point(start[0], start[1]));
            inputX1.set(start[0]);
            inputY1.set(start[1]);
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##X" + p1Name, inputX1, 0.01f, 0.1f, "%.2f")) {
            if (inputX1.get() < -1.0f) inputX1.set(-1.0f);
            if (inputX1.get() > 1.0f) inputX1.set(1.0f);
            line.getStart().setX(inputX1.get());
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##Y" + p1Name, inputY1, 0.01f, 0.1f, "%.2f")) {
            if (inputY1.get() < -1.0f) inputY1.set(-1.0f);
            if (inputY1.get() > 1.0f) inputY1.set(1.0f);
            line.getStart().setY(inputY1.get());
        }
        ImGui.popItemWidth();

        // P2 з слайдерами та полями введення
        ImGui.text("P" + p2Name + ":");
        ImGui.pushItemWidth(200);
        if (ImGui.sliderFloat2("##End" + p2Name, end, -1.0f, 1.0f)) {
            line.setEnd(new Point(end[0], end[1]));
            inputX2.set(end[0]);
            inputY2.set(end[1]);
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##X" + p2Name, inputX2, 0.01f, 0.1f, "%.2f")) {
            if (inputX2.get() < -1.0f) inputX2.set(-1.0f);
            if (inputX2.get() > 1.0f) inputX2.set(1.0f);
            line.getEnd().setX(inputX2.get());
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##Y" + p2Name, inputY2, 0.01f, 0.1f, "%.2f")) {
            if (inputY2.get() < -1.0f) inputY2.set(-1.0f);
            if (inputY2.get() > 1.0f) inputY2.set(1.0f);
            line.getEnd().setY(inputY2.get());
        }
        ImGui.popItemWidth();
    }

    private void renderAnalysisWindow() {
        ImGui.begin("Результати аналізу", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.textWrapped("Відношення між відрізками:");
        ImGui.separator();
        ImGui.textColored(1.0f, 1.0f, 0.0f, 1.0f, analyzer.getRelationshipType());

        if (analyzer.hasIntersection() && analyzer.getIntersectionPoint() != null) {
            Point intersect = analyzer.getIntersectionPoint();

            ImGui.separator();
            ImGui.text("Точка перетину:");
            ImGui.textColored(1.0f, 1.0f, 0.0f, 1.0f,
                    String.format("X: %.4f, Y: %.4f", intersect.getX(), intersect.getY()));
        }

        // Відображення діагностичної інформації про орієнтовані площі
        ImGui.separator();
        ImGui.text("Діагностична інформація:");
        String debugInfo = analyzer.getDebugInfo(line1, line2);
        ImGui.textWrapped(debugInfo);

        ImGui.end();
    }

    private void renderGridSettingsWindow() {
        ImGui.begin("Налаштування сітки", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Основні параметри");
        ImGui.separator();

        // Розмір сітки
        ImGui.text("Розмір сітки:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);

        float[] gridSizeArray = new float[]{gridSizeInput.get()};
        if (ImGui.sliderFloat("##GridSize", gridSizeArray, 0.05f, 0.5f, "%.2f")) {
            gridSizeInput.set(gridSizeArray[0]);
            grid.setGridSize(gridSizeInput.get());
        }
        ImGui.popItemWidth();

        // Товщина ліній
        ImGui.text("Товщина ліній:");

        ImGui.text("  Основні:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] majorWidthArray = new float[]{majorLineWidthInput.get()};
        if (ImGui.sliderFloat("##MajorWidth", majorWidthArray, 0.5f, 3.0f, "%.1f")) {
            majorLineWidthInput.set(majorWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        ImGui.text("  Допоміжні:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] minorWidthArray = new float[]{minorLineWidthInput.get()};
        if (ImGui.sliderFloat("##MinorWidth", minorWidthArray, 0.5f, 3.0f, "%.1f")) {
            minorLineWidthInput.set(minorWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        ImGui.text("  Осі:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] axisWidthArray = new float[]{axisLineWidthInput.get()};
        if (ImGui.sliderFloat("##AxisWidth", axisWidthArray, 0.5f, 5.0f, "%.1f")) {
            axisLineWidthInput.set(axisWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        // Показувати мітки
        if (ImGui.checkbox("Показувати мітки осей", showLabelsInput)) {
            grid.setShowLabels(showLabelsInput.get());
        }

        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Кольори");
        ImGui.separator();

        // Кольори
        if (ImGui.colorEdit3("Колір основних ліній", majorColorInput)) {
            grid.setMajorColor(majorColorInput);
        }

        if (ImGui.colorEdit3("Колір допоміжних ліній", minorColorInput)) {
            grid.setMinorColor(minorColorInput);
        }

        if (ImGui.colorEdit3("Колір осі X", xAxisColorInput)) {
            grid.setXAxisColor(xAxisColorInput);
        }

        if (ImGui.colorEdit3("Колір осі Y", yAxisColorInput)) {
            grid.setYAxisColor(yAxisColorInput);
        }

        ImGui.end();
    }



    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}