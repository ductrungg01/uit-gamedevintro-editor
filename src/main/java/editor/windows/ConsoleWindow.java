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

        ImGui.beginTabBar("consoleItems");

        if (debugLogs.size() > MAX_DEBUGLOG_SIZE) {
            debugLogs.remove(0);
        }

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

        ImGui.endTabBar();
        firstFrame = false;
        ImGui.end();
    }
}
