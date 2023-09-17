package editor.windows;

import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import org.joml.Vector2f;
import system.MouseListener;
import system.Window;
import util.Settings;

import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.COLOR_Green;
import static editor.uihelper.NiceShortCall.COLOR_Red;

public class ConsoleWindow {
    static boolean scrollToBottom_debug = true;
    static boolean scrollToBottom_success = true;
    static boolean scrollToBottom_error = true;
    static boolean firstFrame = true;
    private static ConsoleWindow instance = null;

    //endregion
    private final int MAX_DEBUGLOG_SIZE = 200;
    public List<String> debugLogs = new ArrayList<>();
    public List<String> successLogs = new ArrayList<>();
    public List<String> errorLogs = new ArrayList<>();

    //region Singleton
    private ConsoleWindow() {
    }

    public static ConsoleWindow getInstance() {
        if (instance == null) {
            instance = new ConsoleWindow();
        }

        return instance;
    }

    public void imgui() {
        ImGui.setNextWindowSizeConstraints(Settings.MIN_WIDTH_GROUP_WIDGET, Settings.MIN_HEIGHT_GROUP_WIDGET, Window.getWidth(), Window.getHeight());

        ImGui.begin("Console");

        if (NiceImGui.drawButton("Clear", new ButtonColor())) {
            debugLogs.clear();
            successLogs.clear();
            errorLogs.clear();
        }

        ImGui.beginTabBar("consoleItems");

        //region Remove out of range logs
        if (debugLogs.size() > MAX_DEBUGLOG_SIZE) {
            debugLogs.remove(0);
        }

        if (errorLogs.size() > MAX_DEBUGLOG_SIZE) {
            errorLogs.remove(0);
        }

        if (successLogs.size() > MAX_DEBUGLOG_SIZE) {
            successLogs.remove(0);
        }
        //endregion

        if (ImGui.beginTabItem("Debug")) {
            for (int i = 0; i < debugLogs.size(); i++) {
                ImGui.text(debugLogs.get(i));
            }

            if (ImGui.getScrollY() < ImGui.getScrollMaxY()) {
                scrollToBottom_debug = false;
            } else {
                scrollToBottom_debug = true;
            }

            if (scrollToBottom_debug || firstFrame) {
                ImGui.setScrollHereY(1.0f);
            }
            ImGui.endTabItem();
        }

        if (ImGui.beginTabItem("SuccessLog")) {
            for (int i = 0; i < successLogs.size(); i++) {
                ImGui.textColored(COLOR_Green.x, COLOR_Green.y, COLOR_Green.z, COLOR_Green.w, successLogs.get(i));
            }

            if (ImGui.getScrollY() < ImGui.getScrollMaxY()) {
                scrollToBottom_success = false;
            } else {
                scrollToBottom_success = true;
            }

            if (scrollToBottom_success || firstFrame) {
                ImGui.setScrollHereY(1.0f);
            }
            ImGui.endTabItem();
        }

        if (ImGui.beginTabItem("ErrorLog")) {
            for (int i = 0; i < errorLogs.size(); i++) {
                ImGui.textColored(COLOR_Red.x, COLOR_Red.y, COLOR_Red.z, COLOR_Red.w, errorLogs.get(i));
            }

            if (ImGui.getScrollY() < ImGui.getScrollMaxY()) {
                scrollToBottom_error = false;
            } else {
                scrollToBottom_error = true;
            }

            if (scrollToBottom_error || firstFrame) {
                ImGui.setScrollHereY(1.0f);
            }
            ImGui.endTabItem();
        }

        ImGui.endTabBar();
        firstFrame = false;
        ImGui.end();
    }
}
