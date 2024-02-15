package components;

import editor.Debug;
import editor.NiceImGui;
import imgui.flag.ImGuiComboFlags;
import org.joml.Vector2f;
import util.ProjectUtils;
import util.SceneUtils;
import util.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortalComponent extends Component{

    public String nextScene = "";
    public Vector2f scale = new Vector2f();

    public PortalComponent(String nextScene, float w, float h){
        this.nextScene = nextScene;
        scale = new Vector2f(w, h);
    }

    @Override
    public void editorUpdate(float dt) {
        updateScale();
    }

    @Override
    public void imgui() {
        nextScene = NiceImGui.comboBox("Next scene:", nextScene, ImGuiComboFlags.None,
                getAllSceneOption(), "portalComp of" + this.gameObject);
        NiceImGui.drawVec2Control("Size", this.scale, "Size of portal " + this.gameObject.hashCode());
    }

    private void updateScale(){
        float x = scale.x / Settings.GRID_WIDTH;
        float y = scale.y / Settings.GRID_HEIGHT;

        this.gameObject.transform.scale = new Vector2f(x * Settings.GRID_WIDTH, y * Settings.GRID_HEIGHT);
    }

    private List<String> getAllSceneOption(){
        List<String> scenes = new ArrayList<>();
        Map<Integer, String> map = ProjectUtils.scenes;

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            scenes.add(entry.getValue());
        }

        return scenes;
    }
}
