package org.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Core {
    private long window;
    private int WIDTH = 800;
    private int HEIGHT = 600;

    private Line line1, line2;
    private Renderer renderer;
    private GUI gui;
    private LineAnalyzer analyzer;
    private Grid grid;

    public void run() {
        init();
        loop();

        // Звільняємо ресурси
        if (gui != null) {
            gui.dispose();
        }

        // Звільняємо вікно та колбеки
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Завершуємо GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Налаштування обробника помилок
        GLFWErrorCallback.createPrint(System.err).set();

        // Ініціалізація GLFW
        if (!glfwInit())
            throw new IllegalStateException("Помилка ініціалізації GLFW");

        // Налаштування GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Створення вікна
        window = glfwCreateWindow(WIDTH, HEIGHT, "Аналізатор відрізків", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Помилка створення GLFW вікна");

        // Налаштування колбеку клавіатури
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        // Налаштування колбеку зміни розміру вікна
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            WIDTH = width;
            HEIGHT = height;
            glViewport(0, 0, width, height);
        });

        // Отримуємо потік для управління пам'яттю стеку
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Отримуємо розмір вікна
            glfwGetWindowSize(window, pWidth, pHeight);

            // Отримуємо роздільну здатність основного монітора
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Центруємо вікно
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Робимо контекст OpenGL поточним
        glfwMakeContextCurrent(window);
        // Вмикаємо вертикальну синхронізацію
        glfwSwapInterval(1);
        // Показуємо вікно
        glfwShowWindow(window);

        // Створюємо можливості OpenGL
        GL.createCapabilities();

        // Ініціалізація ліній та рендерера
        renderer = new Renderer();

        line1 = new Line(
                new Point(0f, 0f),
                new Point(0.5f, 0.5f),
                new float[]{1f, 0f, 0f} // Червоний
        );
        line2 = new Line(
                new Point(-0.5f, 0f),
                new Point(0f, 0.5f),
                new float[]{0f, 1f, 0f} // Зелений
        );

        // Ініціалізація сітки
        grid = new Grid();

        renderer.setGrid(grid);

        // Ініціалізація аналізатора ліній
        analyzer = new LineAnalyzer();

        // Ініціалізація GUI
        gui = new GUI(window, line1, line2, analyzer, grid);
        gui.init();
    }

    private void loop() {
        // Встановлюємо колір очищення
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Основний цикл рендерингу
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Оновлюємо viewport і матрицю проекції
            updateViewport();

            renderer.drawGrid(WIDTH, HEIGHT);

            renderer.drawLine(line1);
            renderer.drawLine(line2);

            // Рендер точки перетину в випадку перетину
            if(analyzer.hasIntersection()){
                renderer.drawIntersectionPoint(analyzer.getIntersectionPoint());
            }

            // Аналізуємо відрізки
            analyzer.analyze(line1, line2);

            gui.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updateViewport() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float aspectRatio = (float) WIDTH / HEIGHT;
        if (WIDTH > HEIGHT) {
            // Широке вікно
            glOrtho(-aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
        } else {
            // Високе вікно
            glOrtho(-1.0f, 1.0f, -1.0f / aspectRatio, 1.0f / aspectRatio, -1.0f, 1.0f);
        }

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    public static void main(String[] args) {
        new Core().run();
    }
}