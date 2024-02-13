package editor.windows;

import components.EditorCamera;
import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector2f;
import system.Camera;
import system.GameObject;
import system.Window;
import util.Settings;

import java.util.List;

import static editor.uihelper.NiceShortCall.*;

public class HierarchyWindow {
    //region Singleton
    private HierarchyWindow() {
    }

    private static HierarchyWindow instance = null;

    public static HierarchyWindow getInstance() {
        if (instance == null) {
            instance = new HierarchyWindow();
        }

        return instance;
    }
    //endregion

    private static String payloadDragDropType = "Hierarchy";
    private static GameObject selectedGameObject = null;

    public void imgui() {
        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Hierarchy");

        List<GameObject> gameObjects = Window.getScene().getGameObjects();

        int index = 0;

        if (ImGui.beginTabBar("HierarchyTabBar")) {
            if (ImGui.beginTabItem("GameObjects")){
                for (GameObject obj : gameObjects) {
                    if (!obj.doSerialization()) {
                        continue;
                    }

                    ImGui.pushID(index);
                    float w = ImGui.getContentRegionAvailX();
                    float h = ImGui.getTextLineHeightWithSpacing();
                    if (obj.equals(selectedGameObject)) {
                        if (NiceImGui.drawButtonWithLeftText(obj.name, new ButtonColor(COLOR_Blue, COLOR_DarkAqua, COLOR_Blue), new Vector2f(w, h))) {
                            setCameraCenterGameObject(obj);
                        }
                        ;
                    } else {
                        if (NiceImGui.drawButtonWithLeftText(obj.name, new ButtonColor(COLOR_DarkBlue, COLOR_DarkAqua, COLOR_Blue), new Vector2f(w, h))) {
                            Window.getImguiLayer().getInspectorWindow().setActiveGameObject(obj);
                            selectedGameObject = obj;
                            setCameraCenterGameObject(obj);
                        }
                    }
                    ImGui.popID();
                    index++;
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("System")) {
                NiceImGui.colorPicker4("Camera's clear color", Window.getScene().camera().clearColor);
                ImGui.text("Screen size: (" + Camera.screenSize.x + ":" + Camera.screenSize.y + ")");
                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }

        ImGui.end();
    }

    private boolean doTreeNode(GameObject obj, int index) {
        ImGui.pushID(index);
        boolean treeNodeOpen = ImGui.treeNodeEx(
                obj.name,
                ImGuiTreeNodeFlags.DefaultOpen |
                        ImGuiTreeNodeFlags.FramePadding |
                        ImGuiTreeNodeFlags.OpenOnArrow |
                        ImGuiTreeNodeFlags.SpanAvailWidth,
                obj.name
        );
        ImGui.popID();

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayloadObject(payloadDragDropType, obj);
            ImGui.text(obj.name);
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            Object payloadObj = ImGui.acceptDragDropPayloadObject(payloadDragDropType);
            if (payloadObj != null) {
                if (payloadObj.getClass().isAssignableFrom(GameObject.class)) {
                    GameObject playerGameObject = (GameObject) payloadObj;
                    System.out.println("Payload accepted: '" + playerGameObject.name + "'");
                }
            }

            ImGui.endDragDropTarget();
        }

        return treeNodeOpen;
    }

    public static void setSelectedGameObject(GameObject go) {
        selectedGameObject = go;
    }

    public static void clearSelectedGameObject() {
        selectedGameObject = null;
    }

    void setCameraCenterGameObject(GameObject go) {
        float zoom = EditorCamera.getEditorCameraZoom();
        Vector2f editorCameraSize = new Vector2f(EditorCamera.getEditorCameraSize().x, EditorCamera.getEditorCameraSize().y);
        editorCameraSize.x = editorCameraSize.x * zoom;
        editorCameraSize.y = editorCameraSize.y * zoom;
        Vector2f centerObjectPosition = new Vector2f(go.transform.position.x - (editorCameraSize.x / 2), go.transform.position.y - (editorCameraSize.y / 2));
        EditorCamera.setEditorCamera(centerObjectPosition);
    }
}
