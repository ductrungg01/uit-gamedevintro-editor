package editor.windows;

import imgui.ImGui;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import system.Window;

public class MenuBar {
    //region Methods
    public void imgui() {
        ImGui.beginMenuBar();

        if (Window.get().runtimePlaying) {
            ImGui.text("File");
        } else {

            if (ImGui.beginMenu("File")) {

                if (ImGui.menuItem("New Scene", "Ctrl+N")) {
                    CreateNewSceneWindow.open(false);
                }

                if (ImGui.menuItem("Open Scene", "Ctrl+O")) {
                    OpenSceneWindow.open(true);
                }

                if (ImGui.menuItem("Save Current Scene", "Ctrl+S")) {
                    EventSystem.notify(null, new Event(EventType.SaveLevel));
                }
                ImGui.endMenu();
            }
        }
        ImGui.endMenuBar();
    }
    //endregion
}