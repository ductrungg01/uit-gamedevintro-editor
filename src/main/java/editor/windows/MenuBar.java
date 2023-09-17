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

                if (ImGui.menuItem("New", "Ctrl+N")) {
                    CreateNewSceneWindow.open(false);
                }

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    OpenSceneWindow.open(true);
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    EventSystem.notify(null, new Event(EventType.SaveLevel));
                }
                ImGui.endMenu();
            }
        }
        ImGui.endMenuBar();
    }
    //endregion
}