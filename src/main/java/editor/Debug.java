package editor;

import editor.windows.ConsoleWindow;

public class Debug {

    public static <T> void Log(T log) {
        ConsoleWindow.getInstance().debugLogs.add(log.toString());
    }
    public static <T> void Log(T log, LogType type) {
        if (type == LogType.Debug){
            ConsoleWindow.getInstance().debugLogs.add(log.toString());
        } else if (type == LogType.Success) {
            ConsoleWindow.getInstance().successLogs.add(log.toString());
        } else if (type == LogType.Error){
            ConsoleWindow.getInstance().errorLogs.add(log.toString());
        }
    }


    public static void Clear() {
        ConsoleWindow.getInstance().debugLogs.clear();
    }
}
