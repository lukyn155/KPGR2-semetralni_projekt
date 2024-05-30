package snake;

import lwjglutils.OGLTextRenderer;

import java.util.LinkedList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import snake.global.AbstractRenderer;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Renderer extends AbstractRenderer {

    private static final int GRID_SIZE = 20;
    private static final int INITIAL_SNAKE_LENGTH = 3;

    private LinkedList<int[]> snake;
    private int[] food;
    private int[] direction;
    private boolean gameOver;
    private Random random;
    private long lastTime;
    private long delay;
    private int[] pendingDirection;
    private int score;

    public Renderer() {
        super();

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_UP, GLFW_KEY_W:
                            if (direction[1] == 0) {
                                pendingDirection = new int[]{0, 1};
                            }
                            break;
                        case GLFW_KEY_DOWN, GLFW_KEY_S:
                            if (direction[1] == 0) {
                                pendingDirection = new int[]{0, -1};
                            }
                            break;
                        case GLFW_KEY_LEFT, GLFW_KEY_A:
                            if (direction[0] == 0) {
                                pendingDirection = new int[]{-1, 0};
                            }
                            break;
                        case GLFW_KEY_RIGHT, GLFW_KEY_D:
                            if (direction[0] == 0) {
                                pendingDirection = new int[]{1, 0};
                            }
                            break;
                        case GLFW_KEY_R:
                            textRenderer.setScale(1);
                            initGame();
                            break;
                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, true);
                            break;
                    }
                }
            }
        };

        random = new Random();
        initGame();
    }

    private void initGame() {
        snake = new LinkedList<>();
        for (int i = 0; i < INITIAL_SNAKE_LENGTH; i++) {
            snake.add(new int[]{GRID_SIZE / 2, GRID_SIZE / 2 + i});
        }
        direction = new int[]{0, -1};
        pendingDirection = new int[]{0, -1};
        spawnFood();
        gameOver = false;
        lastTime = System.currentTimeMillis();
        delay = 200;
        score = 0;
    }

    private void spawnFood() {
        boolean foodOnSnake;
        do {
            foodOnSnake = false;
            food = new int[]{random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)};
            for (int[] segment : snake) {
                if (segment[0] == food[0] && segment[1] == food[1]) {
                    foodOnSnake = true;
                    break;
                }
            }
        } while (foodOnSnake);
    }

    private void moveSnake() {
        if (gameOver) {
            return;
        }

        direction = pendingDirection;

        int[] newHead = new int[]{snake.getFirst()[0] + direction[0], snake.getFirst()[1] + direction[1]};

        if (newHead[0] < 0 || newHead[0] >= GRID_SIZE || newHead[1] < 0 || newHead[1] >= GRID_SIZE) {
            gameOver = true;
            return;
        }

        for (int[] segment : snake) {
            if (segment[0] == newHead[0] && segment[1] == newHead[1]) {
                gameOver = true;
                return;
            }
        }

        snake.addFirst(newHead);

        if (newHead[0] == food[0] && newHead[1] == food[1]) {
            score += 10;
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    @Override
    public void init() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        textRenderer = new OGLTextRenderer(width, height);
    }

    @Override
    public void display() {
        glViewport(0,0,width,height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (gameOver) {
            textRenderer.setScale(2);
            textRenderer.addStr2D(width / 2 - 200, height / 2, "Score: " + score);
            textRenderer.addStr2D(width / 2 - 200, height / 2 + 50, "Pro znovu spuštění hry stiskni R");

            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= delay) {
            moveSnake();
            lastTime = currentTime;
        }

        glColor3f(0.0f, 1.0f, 0.0f);
        for (int[] segment : snake) {
            drawSegment(segment[0], segment[1]);
        }

        glColor3f(1.0f, 0.0f, 0.0f);
        drawSegment(food[0], food[1]);

        textRenderer.addStr2D(3, 20, "Score: " + score);
        textRenderer.addStr2D(width - 230, height - 3, " KPGR2 Matějovský Lukáš, 23.5.2024");
    }

    private void drawSegment(int x, int y) {
        float cellSize = 2.0f / GRID_SIZE;
        float halfCell = cellSize / 2.0f;
        float offset = 1.0f - halfCell;

        float centerX = x * cellSize - offset;
        float centerY = y * cellSize - offset;

        glBegin(GL_QUADS);
        glVertex2f(centerX - halfCell, centerY - halfCell);
        glVertex2f(centerX - halfCell, centerY + halfCell);
        glVertex2f(centerX + halfCell, centerY + halfCell);
        glVertex2f(centerX + halfCell, centerY - halfCell);
        glEnd();
    }
}
