package system;

import editor.windows.HierarchyWindow;
import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import renderer.Renderer;
import renderer.*;
import scenes.EditorSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import util.AssetPool;
import util.ProjectUtils;
import util.SceneUtils;
import util.Time;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

import static observers.events.EventType.Export;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements Observer {
    // region Fields
    private static Window window = null;
    private static Scene currentScene;
    private static boolean isWindowFocused = true;
    public long glfwWindow;
    private int width, height;
    private String title;
    private ImGuiLayer imGuiLayer;
    private Framebuffer framebuffer;
    private PickingTexture pickingTexture;
    private long audioContext;
    private long audioDevice;
    // endregion

    private Window() {
        this.width = 3840;
        this.height = 2160;
        this.title = "9 Engine - uit-gamedevintro-editor";
        EventSystem.addObserver(this);
    }

    //region Methods
    public static void changeScene(SceneInitializer sceneInitializer) {
        if (currentScene != null) {
            currentScene.destroy();
        }

        getImguiLayer().getInspectorWindow().setActiveGameObject(null);
        currentScene = new Scene(sceneInitializer);

        currentScene.init();
        currentScene.load();
        currentScene.start();

        Camera.screenSize = ProjectUtils.screenSize;
        Window.getScene().camera().position = new Vector2f(-150, -300);
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    public static Scene getScene() {
        return currentScene;
    }

    public static int getWidth() {
        return get().width;
    }

    private static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static int getHeight() {
        return get().height;
    }

    private static void setHeight(int newHeight) {
        get().height = newHeight;
    }

    public static Framebuffer getFramebuffer() {
        return get().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return 16.0f / 9.0f;
    }

    public static ImGuiLayer getImguiLayer() {
        return get().imGuiLayer;
    }

    public void loop() throws IOException {
        float beginTime = (float) glfwGetTime();
        float endTime;
        float dt = -1.0f;

        Shader defaultShader = AssetPool.getShader("system-assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("system-assets/shaders/pickingShader.glsl");

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll events
            glfwPollEvents();

            // Render pass 1.  Render to picking texture
            glDisable(GL_BLEND);
            pickingTexture.enableWriting();

            glViewport(0, 0, 1920, 1080);

            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Renderer.bindShader(pickingShader);
            currentScene.render();

            pickingTexture.disableWriting();
            glEnable(GL_BLEND);

            // Render pass 2. Render actual game
            GridLineDraw.beginFrame();
            DebugDraw.beginFrame();

            this.framebuffer.bind();
            Vector4f clearColor = currentScene.camera().clearColor;
            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                Renderer.bindShader(defaultShader);

                currentScene.editorUpdate(dt);

                GridLineDraw.draw();
                DebugDraw.draw();
                currentScene.render();
            }
            this.framebuffer.unbind();

            this.imGuiLayer.update(dt, currentScene);

            glfwSetWindowFocusCallback(glfwWindow, (window, focused) -> {
                isWindowFocused = focused;
            });

            if (!isWindowFocused) {
                GLFW.glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }

            KeyListener.endframe();
            MouseListener.endFrame();

            glfwSwapBuffers(glfwWindow);

            endTime = (float) glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;

            Time.deltaTime = dt;

            if (glfwWindowShouldClose(glfwWindow)) {
                int response = JOptionPane.showConfirmDialog(null, "Close the editor?", "CLOSE", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.NO_OPTION) {
                    glfwSetWindowShouldClose(glfwWindow, false);
                } else {
                    EventSystem.notify(null, new Event(Export));
                }
            }
        }
    }

    public void run() throws IOException {
        init();
        loop();

        // Destroy audio context
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() throws IOException {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        // Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);

        SetWindowIcon();

        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create GLFW window!");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });
        glfwSetDropCallback(glfwWindow, MouseListener::mouseDropCallback);

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // Initialize the audio device
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "Audio library not supported";
        }

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        this.framebuffer = new Framebuffer(1920, 1080);
        this.pickingTexture = new PickingTexture(1920, 1080);
        glViewport(0, 0, 1920, 1080);

        this.imGuiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
        this.imGuiLayer.initImGui();

        Window.changeScene(new EditorSceneInitializer());
    }

    public void changeCurrentScene(String sceneName) {
        if (sceneName.equals(SceneUtils.CURRENT_SCENE)) {
            return;
        }

        SceneUtils.CURRENT_SCENE = sceneName;
        glfwSetWindowTitle(glfwWindow, this.title + " - " + sceneName);
        HierarchyWindow.clearSelectedGameObject();
        Window.getImguiLayer().getInspectorWindow().clearSelected();
    }

    void SetWindowIcon() {
        // Load the image file
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("system-assets/images/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the image to GLFWImage format
        GLFWImage.Buffer icons = GLFWImage.create(1);
        ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                pixels.put((byte) ((pixel >> 16) & 0xFF));     // R
                pixels.put((byte) ((pixel >> 8) & 0xFF));      // G
                pixels.put((byte) (pixel & 0xFF));             // B
                pixels.put((byte) ((pixel >> 24) & 0xFF));     // A
            }
        }
        pixels.flip();
        icons.position(0);
        icons.width(image.getWidth());
        icons.height(image.getHeight());
        icons.pixels(pixels);

        // Set the window icon
        glfwSetWindowIcon(glfwWindow, icons);
    }

    @Override
    public void onNotify(GameObject object, Event event) {
        switch (event.type) {
            case GameEngineStart:
                Window.getImguiLayer().getInspectorWindow().clearSelected();
                HierarchyWindow.clearSelectedGameObject();
                currentScene.removeAllGameObjectInScene();
                Window.changeScene(new EditorSceneInitializer());
                break;
            case Import:
                break;
            case Export:
                currentScene.exportScene();
                break;
        }
    }
    //endregion
}
