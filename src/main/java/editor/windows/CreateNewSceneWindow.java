package editor.windows;

import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import system.Window;
import util.SceneUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.*;

public class CreateNewSceneWindow {
    private static boolean isOpen = false;
    private static boolean showOpenSceneWindow = false;
    private static String sceneName = "";

    public static void open(boolean showOpenSceneWindowAfterCancel) {
        isOpen = true;
        sceneName = "";
        showOpenSceneWindow = showOpenSceneWindowAfterCancel;
    }

    public static void imgui() {
        if (!isOpen) return;

        ImGui.openPopup("Create new scene");

        float popupWidth = Window.getWidth() * 0.3f;
        float popupHeight = Window.getHeight() * 0.15f;
        ImGui.setNextWindowSize(popupWidth, popupHeight);

        float popupPosX = (float) Window.getWidth() / 2 - popupWidth / 2;
        float popupPosY = (float) Window.getHeight() / 2 - popupHeight / 2;
        ImGui.setNextWindowPos(popupPosX, popupPosY, ImGuiCond.Always);

        if (ImGui.beginPopupModal("Create new scene", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize)) {
            sceneName = NiceImGui.inputText("", sceneName, "Enter scene name", 0, "NewsceneName");

            if (NiceImGui.drawButton("Create", new ButtonColor(COLOR_DarkBlue, COLOR_Blue, COLOR_DarkBlue), new Vector2f(100, 30))) {
                if (!sceneName.isEmpty()) {
                    if (createNewScene(sceneName)) {
                        isOpen = false;
                        Window.get().changeCurrentScene(sceneName, true, true);
                    }
                }
            }

            ImGui.sameLine();

            if (NiceImGui.drawButton("Cancel", new ButtonColor(COLOR_DarkRed, COLOR_Red, COLOR_DarkRed), new Vector2f(100, 30))) {
                isOpen = false;
                if (showOpenSceneWindow) {
                    OpenSceneWindow.open(true);
                }
            }

            ImGui.endPopup();
        }
    }

    private static boolean createNewScene(String sceneName) {
        if (!isValidName(sceneName)) {
            return false;
        }

        File folder = new File("data\\" + sceneName);

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            if (success) {
                JOptionPane.showMessageDialog(null, "Create scene '" + sceneName + "' successful",
                        "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
                createFile(sceneName);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Error when create scene!\nCheck if the scene name has any special characters? ",
                        "CREATE scene FAIL", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "'" + sceneName + "' is existed!",
                    "INVALID NAME", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static void createFile(String sceneName) {
        try {
            FileWriter writer = new FileWriter("data\\" + sceneName + "\\" + "level.txt");
            writer.write("[]");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileWriter writer = new FileWriter("data\\" + sceneName + "\\" + "prefabs.txt");
            writer.write("[]");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileWriter writer = new FileWriter("data\\" + sceneName + "\\" + "spritesheet.txt");
            writer.write("");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValidName(String name) {
        if (name.startsWith(" ") || name.endsWith(" ")) {
            JOptionPane.showMessageDialog(null, "scene name cannot contain leading and trailing spaces",
                    "INVALID NAME", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> scenes = new ArrayList<>();
        File directory = new File("data");
        File[] filesList = directory.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                if (file.isDirectory()) {
                    scenes.add(file.getName());
                }
            }
        }
        if (scenes.contains(SceneUtils.CURRENT_SCENE)) {
            int currsceneIndex = scenes.indexOf(SceneUtils.CURRENT_SCENE);
            scenes.set(currsceneIndex, scenes.get(0));
            scenes.set(0, SceneUtils.CURRENT_SCENE);
        }

        if (scenes.contains(name)) {
            JOptionPane.showMessageDialog(null, "'" + sceneName + "' is existed!",
                    "INVALID NAME", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean isOpen() {
        return isOpen;
    }
}
