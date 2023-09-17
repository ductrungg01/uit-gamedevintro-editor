package components;

import editor.NiceImGui;
import imgui.flag.ImGuiComboFlags;
import util.SceneUtils;

public class PortalComponent extends Component{

    public String nextScene = "";

    @Override
    public void imgui() {
         nextScene = NiceImGui.comboBox("Next scene:", nextScene, ImGuiComboFlags.None,
                 SceneUtils.getAllScene(), "portalComp of" + this.gameObject);
    }
}
