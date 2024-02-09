package editor;

import editor.windows.ConsoleWindow;

public class Debug {
    public static void Log(String log) {
        ConsoleWindow.getInstance().debugLogs.add(log);
    }
}
