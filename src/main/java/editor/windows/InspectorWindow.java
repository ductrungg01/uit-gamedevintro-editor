package editor.windows;

import components.*;
import editor.Debug;
import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import org.joml.Vector2f;
import system.GameObject;
import org.joml.Vector4f;
import renderer.PickingTexture;
import system.Window;
import util.Settings;

import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.*;

public class InspectorWindow {
    //region Fields
    List<GameObject> activeGameObjects;
    private List<Vector4f> activeGameObjectOriginalColor;
    private GameObject activeGameObject = null;
    private GameObject copyGameObject = null;
    private PickingTexture pickingTexture;

    String searchText = "";
    boolean showAddComponentMenu = false;
    //endregion

    //region Constructors
    public InspectorWindow(PickingTexture pickingTexture) {
        this.activeGameObjects = new ArrayList<>();
        this.pickingTexture = pickingTexture;
        this.activeGameObjectOriginalColor = new ArrayList<>();
    }
    //endregion

    //region Methods
    public void imgui() {
        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Inspector");

        if (activeGameObjects.size() == 1 && activeGameObjects.get(0) != null) {
            activeGameObject = activeGameObjects.get(0);
        }

        if (activeGameObject == null) {
            ImGui.end();
            return;
        }

        activeGameObject.imgui();

        ImGui.separator();

        if (this.activeGameObject.getComponent(StateMachine.class) == null && this.activeGameObject.isPrefab()) {
            if (NiceImGui.drawButton("Add StateMachine",
                    new ButtonColor(COLOR_DarkBlue, COLOR_Blue, COLOR_Blue),
                    new Vector2f(ImGui.getContentRegionAvailX(), 50f))) {
                showAddComponentMenu = true;
                searchText = "";
                ImGui.openPopup("AddComponentMenu");
            }

            if (showAddComponentMenu) {
                this.activeGameObject.addComponent(new StateMachine());
            }
        }

        ImGui.end();
    }

    public GameObject getActiveGameObject() {
        return activeGameObjects.size() == 1 ? this.activeGameObjects.get(0) : null;
    }

    public void clearSelected() {
        if (activeGameObjectOriginalColor.size() > 0) {
            int i = 0;
            for (GameObject go : activeGameObjects) {
                SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
                if (spr != null) {
                    spr.setColor(activeGameObjectOriginalColor.get(i));
                }
                i++;
            }
        }

        this.activeGameObjects.clear();
        this.activeGameObject = null;
        this.activeGameObjectOriginalColor.clear();
    }

    public List<GameObject> getActiveGameObjects() {
        return this.activeGameObjects;
    }

    public void setActiveGameObject(GameObject go) {
        if (this.activeGameObject != null && this.activeGameObject == go) return;

        if (go != null) {
            clearSelected();
            this.activeGameObjects.add(go);
        }
    }

    public void addActiveGameObject(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            this.activeGameObjectOriginalColor.add(new Vector4f(spr.getColor()));
            spr.setColor(new Vector4f(0.8f, 0.8f, 0.0f, 0.8f));
        } else {
            this.activeGameObjectOriginalColor.add(new Vector4f());
        }
        this.activeGameObjects.add(go);
    }

    public PickingTexture getPickingTexture() {
        return this.pickingTexture;
    }

    public GameObject getCopyGameObject() {
        return copyGameObject;
    }

    public void setCopyGameObject(GameObject copyGameObject) {
        this.copyGameObject = copyGameObject;
    }

    //endregion
}
