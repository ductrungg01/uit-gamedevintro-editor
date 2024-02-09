package editor.windows;

import imgui.ImGui;
import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import observers.events.EventType;
import system.GameObject;
import system.Window;

import static observers.events.EventType.Export;
import static observers.events.EventType.Import;

public class MenuBar implements Observer {
    //region Methods
    public void imgui() {
        ImGui.beginMenuBar();

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Export", "Ctrl+E")) {
                EventSystem.notify(null, new Event(Export));
            }
            if (ImGui.menuItem("Import", "Ctrl+I")) {
                EventSystem.notify(null, new Event(Import));
            }
            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    @Override
    public void onNotify(GameObject object, Event event) {
        // TODO: impl
        switch (event.type){
            case Export:
                break;
            case Import:
                break;
        }
    }
    //endregion
}