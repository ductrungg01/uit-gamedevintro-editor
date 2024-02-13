package editor.windows;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import system.Window;
import util.ProjectUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImportProjectDataWindow {

    private static boolean isOpen = true;
    private static ImString imString = new ImString("");

    public static void imgui(){
        check();

        if (!isOpen) return;

        ImGui.openPopup("Import");
        float popupWidth = Window.getWidth() * 0.6f;
        float popupHeight = Window.getHeight() * 0.3f;
        ImGui.setNextWindowSize(popupWidth, popupHeight);

        float popupPosX = (float) Window.getWidth() / 2 - popupWidth / 2;
        float popupPosY = (float) Window.getHeight() / 2 - popupHeight / 2;
        ImGui.setNextWindowPos(popupPosX, popupPosY, ImGuiCond.Always);

        if (ImGui.beginPopupModal("Import", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize)){
            ImGui.inputText("", imString, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();

            if (ImGui.button("Find")) {
                openSelectProjectFileDialog();
            }

            if (ImGui.button("Import")) {
                ProjectUtils.loadProject();
            }

            ImGui.endPopup();
        }
    }

    private static void check(){
        if (ProjectUtils.getProjectFile().equals("")){
            open();
        }
    }

    public static void open(){
        isOpen = true;
    }

    public static void close(){
        isOpen = false;
    }

    private static void openSelectProjectFileDialog(){
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();

            imString = new ImString(selectedFilePath);
            ProjectUtils.setProjectFile(selectedFilePath);
        }
    }
}
