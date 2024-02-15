package editor.windows;

import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import scenes.EditorSceneInitializer;
import system.Window;
import util.FileUtils;
import util.ProjectUtils;
import util.SceneUtils;
import util.Settings;

import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.COLOR_Blue;
import static editor.uihelper.NiceShortCall.COLOR_DarkBlue;

public class ScenesWindow {
    public static List<String> scenes = new ArrayList<>();

    public static void update() {
        getAllScene();

        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Scenes");

        for (int i = 0; i < scenes.size(); i++) {
            String s = scenes.get(i);

            NiceImGui.buttonFullWidthLeftTextAndHaveIcon("Scene from scene list " + s,
                    s + (s.equals(SceneUtils.CURRENT_SCENE) ? " (Current scene)" : ""),
                    FileUtils.getIcon(FileUtils.ICON_NAME.PROJECT),
                    new ButtonColor(COLOR_DarkBlue, COLOR_Blue, COLOR_DarkBlue),
                    new Vector4f(0, 0, 0, 0));
            if (ImGui.isItemHovered() && !s.equals(SceneUtils.CURRENT_SCENE)) {
                ImGui.beginTooltip();
                Vector4f textColor = Settings.NAME_COLOR;
                ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Double-click to open this scene!");
                ImGui.endTooltip();

                if (ImGui.isMouseDoubleClicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                    EventSystem.notify(null, new Event(EventType.Export));
                    ProjectUtils.changeCurrentSceneId(s);
                    Window.changeScene(new EditorSceneInitializer());
                    Window.get().changeCurrentScene(s);
                }
            }
        }

        ImGui.end();
    }

    static void getAllScene() {
        scenes.clear();

        scenes = ProjectUtils.convertToList(ProjectUtils.scenes);

        for (int i = 0; i < scenes.size(); i++){
            String scene = scenes.get(i);
            scene = getSceneName(scene);
            scenes.set(i, scene);
        }
    }

    static private String getSceneName(String scenePath){
        int lastSlashIndex = scenePath.lastIndexOf("\\");

        String newSceneName = scenePath.substring(lastSlashIndex + 1);

        newSceneName = FileUtils.getFileNameWithoutExtension(newSceneName);

        return newSceneName;
    }
}
