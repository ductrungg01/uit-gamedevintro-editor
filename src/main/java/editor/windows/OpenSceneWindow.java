package editor.windows;

import editor.MessageBox;
import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import system.Window;
import util.FileUtils;
import util.SceneUtils;
import util.Settings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class OpenSceneWindow {
    private static boolean isOpen = false;
    private static List<String> scenes = new ArrayList<>();
    private static boolean allowToCancel = false;
    private static String sceneNeedToRename = "";
    private static String search = "";

    public static void open(boolean allowToCancel) {
        isOpen = true;
        getListScene();
        OpenSceneWindow.allowToCancel = allowToCancel;
        search = "";
    }

    public static void imgui() {
        if (!isOpen) return;

        ImGui.openPopup("Open scene");
        float popupWidth = Window.getWidth() * 0.3f;
        float popupHeight = Window.getHeight() * 0.7f;
        ImGui.setNextWindowSize(popupWidth, popupHeight);

        float popupPosX = (float) Window.getWidth() / 2 - popupWidth / 2;
        float popupPosY = (float) Window.getHeight() / 2 - popupHeight / 2;
        ImGui.setNextWindowPos(popupPosX, popupPosY, ImGuiCond.Always);

        if (ImGui.beginPopupModal("Open scene", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize)) {
            ImGui.text("Select a scene below or");
            ImGui.sameLine();
            if (NiceImGui.drawButton("Create new scene", new ButtonColor(COLOR_DarkGreen, COLOR_Green, COLOR_DarkGreen), new Vector2f(200, 30))) {
                CreateNewSceneWindow.open(true);
                isOpen = false;
            }
            ImGui.separator();

            search = NiceImGui.inputText("Search: ", search, "Enter scene name to search", 0, "SearchSceneFromOpenSceneWindow");

            Vector4f textColor = Settings.NAME_COLOR;
            ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "SCENE LIST");

            ImGui.beginChild("SceneList", ImGui.getContentRegionMaxX() * 0.99f, ImGui.getContentRegionMaxY() * 0.75f, true);
            for (int i = 0; i < scenes.size(); i++) {
                String p = scenes.get(i);

                if (search.isEmpty() || p.toLowerCase().contains(search.toLowerCase())) {
                    boolean needToBreakFlag = false;

                    ImGui.pushID("scene" + p);

                    if (p.equals(sceneNeedToRename)) {
                        boolean changeCurrentSceneFlag = p.equals(SceneUtils.CURRENT_SCENE);

                        String[] newName = NiceImGui.inputTextNoLabel(p);
                        if (newName[0].equals("true")) {
                            if (!newName[1].equals(p)) {
                                if (scenes.contains(newName[1])) {
                                    JOptionPane.showMessageDialog(null, "The scene name '" + newName[1] + "' is existed!",
                                            "ERROR", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    File srcFile = new File("data\\" + p);
                                    File desFile = new File("data\\" + newName[1]);
                                    boolean rename = srcFile.renameTo(desFile);
                                    if (!rename) {
                                        MessageBox.setShowMsb(true);
                                        MessageBox.setTypeOfMsb(MessageBox.TypeOfMsb.CREATE_FILE_SUCCESS);
                                        MessageBox.setMsbText("Rename failed");
                                    } else {
                                        if (changeCurrentSceneFlag) {
                                            Window.get().changeCurrentScene(newName[1], false, false);
                                        }
                                    }

                                    needToBreakFlag = true;
                                    getListScene();
                                }
                            }
                        }

                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Enter to confirm rename");
                            ImGui.endTooltip();
                        }
                    } else {
                        String buttonTitle = (p.equals(SceneUtils.CURRENT_SCENE) ? p + " (Current scene)" : p);

                        NiceImGui.buttonFullWidthLeftTextAndHaveIcon("Pcene " + p, buttonTitle,
                                FileUtils.getIcon(FileUtils.ICON_NAME.PROJECT),
                                new ButtonColor(COLOR_DarkBlue, COLOR_Blue, COLOR_DarkBlue),
                                new Vector4f(0, 0, 0, 0));
                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Double-click to open this scene!");
                            ImGui.text("Right-click to more option");
                            ImGui.endTooltip();

                            if (ImGui.isMouseDoubleClicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                                Window.get().changeCurrentScene(p, true, true);
                                isOpen = false;
                            }

                            if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_RIGHT)) {
                                ImGui.openPopup("Item popup");
                            }
                        }

                        if (ImGui.beginPopup("Item popup")) {
                            if (ImGui.menuItem("Rename scene")) {
                                sceneNeedToRename = p;
                            }
                            if (ImGui.menuItem("Open in Explorer")) {
                                try {
                                    File file = new File("data\\" + p);
                                    String command = "explorer.exe \"" + file.getAbsolutePath() + "\"";

                                    Runtime.getRuntime().exec(command);
                                } catch (IOException e) {
                                    MessageBox.setContext(true, MessageBox.TypeOfMsb.ERROR, "Error! Can't open scene");
                                }
                            }
                            if (ImGui.menuItem("Delete this scene")) {
                                int response = JOptionPane.showConfirmDialog(null,
                                        "DELETE scene '" + p + "'?\nScene will be removed permanent\nYou cannot undo this action!",
                                        "DELETE SCENE", JOptionPane.YES_NO_OPTION);
                                if (response == JOptionPane.YES_OPTION) {
                                    if (p.equals(SceneUtils.CURRENT_SCENE)) {
                                        Window.get().changeCurrentScene("", false, false);
                                        OpenSceneWindow.open(false);
                                    }

                                    try {
                                        File file = new File("data\\" + p);
                                        if (file.exists()) {
                                            String[] command = {"cmd", "/c", "rmdir", "/s", "/q", file.getAbsolutePath()};
                                            Process process = Runtime.getRuntime().exec(command);

                                            int exitCode = process.waitFor();
                                            if (exitCode == 0) {
                                                System.out.println("Scene '" + p + "' is removed");
                                            } else {
                                                System.out.println("Error when remove scene '" + p + "'");
                                            }
                                        } else {
                                            System.out.println("Cannot find scene '" + p + "'");
                                        }
                                    } catch (IOException e) {
                                        System.out.println(e.getMessage());
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                    needToBreakFlag = true;
                                    getListScene();
                                }
                            }

                            ImGui.endPopup();
                        }
                    }

                    ImGui.popID();

                    if (needToBreakFlag) break;
                }
            }

            //check click, check if click outside rename input, use hover any
            if (ImGui.isMouseClicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                if (!ImGui.isAnyItemHovered()) {
                    sceneNeedToRename = "";
                }
            }
            ImGui.endChild();
            if (allowToCancel) {
                if (NiceImGui.drawButton("Cancel", new ButtonColor(COLOR_DarkRed, COLOR_Red, COLOR_DarkRed), new Vector2f(100, 30))) {
                    isOpen = false;
                }
            }

            ImGui.endPopup();
        }
    }

    private static void getListScene() {
        scenes.clear();
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
            int currSceneIndex = scenes.indexOf(SceneUtils.CURRENT_SCENE);
            scenes.set(currSceneIndex, scenes.get(0));
            scenes.set(0, SceneUtils.CURRENT_SCENE);
        }
    }

    public static boolean isOpen() {
        return isOpen;
    }
}
