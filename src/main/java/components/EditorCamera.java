package components;

import imgui.ImGui;
import imgui.flag.ImGuiMouseCursor;
import system.Camera;
import system.KeyListener;
import system.MouseListener;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class EditorCamera extends Component implements INonAddableComponent {

    private static Camera levelEditorCamera;
    //region Fields
    private float dragDebounce = 0.032f;
    private Vector2f clickOrigin;
    private boolean reset = false;
    private float lerpTime = 0.0f;
    private float dragSensitivity = 30.0f;
    private float scrollSensivity = 100f;
    //endregion

    //region Constructors
    public EditorCamera(Camera levelEditorCamera) {
        this.levelEditorCamera = levelEditorCamera;
        this.clickOrigin = new Vector2f();
    }

    public EditorCamera() {
    }
//endregion

    public static void setEditorCamera(Vector2f position) {
        levelEditorCamera.position = position;
    }

    public static Vector2f getEditorCameraSize() {
        return levelEditorCamera.getProjectionSize();
    }

    public static float getEditorCameraZoom() {
        return levelEditorCamera.getZoom();
    }

    //region Override methods
    @Override
    public void editorUpdate(float dt) {
        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (ImGui.getMouseCursor() != ImGuiMouseCursor.Hand) {
                ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
            }
        }

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && dragDebounce > 0) {
            this.clickOrigin = MouseListener.getWorld();
            dragDebounce -= dt;
            return;
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            Vector2f mousePos = MouseListener.getWorld();
            Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);

            levelEditorCamera.position.sub(delta.mul(dt).mul(dragSensitivity));
            this.clickOrigin.lerp(mousePos, dt);
        }
        if (dragDebounce <= 0.0f && !MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            dragDebounce = 0.1f;
        }

        if (MouseListener.getScrollY() != 0.0f) {
            float addValue = (float) Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensivity),
                    1 / levelEditorCamera.getZoom());

            addValue *= -Math.signum(MouseListener.getScrollY()) * 5;
            levelEditorCamera.addZoom(addValue);
        }
    }
    //endregion
}
