package editor.windows;

import components.Sprite;
import components.SpriteRenderer;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import system.GameObject;
import system.Window;
import util.Settings;

public class PrefabsWindow {
    //region Singleton
    private PrefabsWindow() {
    }

    private static PrefabsWindow instance = null;

    public static PrefabsWindow getInstance() {
        if (instance == null) {
            instance = new PrefabsWindow();
        }

        return instance;
    }
    //endregion

    final float DEFAULT_BUTTON_SIZE = 45;
    boolean isClick = false;
    boolean isCreateChild = false;

    public void imgui() {
        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Prefabs");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 oldItemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(oldItemSpacing);
        final Vector2f ITEM_SPACING_DEFAULT = new Vector2f(12, 12);
        ImGui.getStyle().setItemSpacing(ITEM_SPACING_DEFAULT.x, ITEM_SPACING_DEFAULT.y);
        float windowX2 = windowPos.x + windowSize.x;
        ImVec2 itemSpacing = new ImVec2(ITEM_SPACING_DEFAULT.x, ITEM_SPACING_DEFAULT.y);

        for (int i = 0; i < GameObject.PrefabLists.size(); i++) {
            GameObject prefab = GameObject.PrefabLists.get(i);

            isClick = false;
            isCreateChild = false;

            drawPrefabButton(prefab);

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + DEFAULT_BUTTON_SIZE;
            if (nextButtonX2 <= windowX2) {
                ImGui.sameLine();
            }

            if (isClick) {
                SceneHierarchyWindow.clearSelectedGameObject();
                Window.getImguiLayer().getInspectorWindow().setActiveGameObject(prefab);
            }
            if (isCreateChild) {
                GameObject childGo = prefab.generateChildGameObject();
                Window.getScene().getMouseControls().pickupObject(childGo);
            }
        }

        ImGui.getStyle().setItemSpacing(oldItemSpacing.x, oldItemSpacing.y);
        ImGui.end();
    }

    private void drawPrefabButton(GameObject prefab) {
        Sprite sprite = prefab.getComponent(SpriteRenderer.class).getSprite();

        Vector2f[] texCoords = sprite.getTexCoords();

        String idPush = sprite.getTexId() + "### Prefab shower" + prefab.hashCode();
        ImGui.pushID(idPush);

        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        if (ImGui.imageButton(sprite.getTexId(), DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE, texCoords[3].x, texCoords[3].y, texCoords[1].x, texCoords[1].y)) {
            isClick = true;
        }

        if (ImGui.isItemHovered()) {
            if (ImGui.isMouseClicked(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                ImGui.openPopup("RightClick of Prefab" + prefab.hashCode());
            } else {
                ImGui.beginTooltip();
                Vector4f color = Settings.NAME_COLOR;
                ImGui.textColored(color.x, color.y, color.z, color.w, prefab.name);
                ImGui.text("Click to see details in Inspectors window!");
                ImGui.text("Right-click if you want to create a child!");
                ImGui.endTooltip();
            }
        }

        if (ImGui.beginPopup("RightClick of Prefab" + prefab.hashCode())) {
            if (ImGui.menuItem("Create a child game object")) {
                isCreateChild = true;
            }
            ImGui.endPopup();
        }

        ImGui.popID();
    }
}
