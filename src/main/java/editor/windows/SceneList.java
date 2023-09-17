package editor.windows;

import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import system.Window;
import util.FileUtils;
import util.SceneUtils;
import util.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.COLOR_Blue;
import static editor.uihelper.NiceShortCall.COLOR_DarkBlue;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class SceneList {
    private static List<String> scenes = new ArrayList<>();

    public static boolean isAutoSave = true;

    public static void update(){
        getAllScene();

        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Scene list");

        isAutoSave = NiceImGui.checkbox("Auto save when change scene or close?", isAutoSave);

        if (ImGui.button("New Scene")){
            CreateNewSceneWindow.open(false);
        }

        for (int i = 0; i < scenes.size(); i++){
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
                    Window.get().changeCurrentScene(s, true, true);
                }
            }
        }

        ImGui.end();
    }

    static void getAllScene(){
        scenes.clear();

        scenes = SceneUtils.getAllScene();
    }
}
