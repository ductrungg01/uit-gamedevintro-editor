package components;

import imgui.ImGui;

public class PlatformInfor extends Component {
    public int id, length, spr_begin_id, spr_middle_id, spr_end_id;
    public float cell_width, cell_height;

    public PlatformInfor(int id, float cell_width, float cell_height, int length, int spr_begin_id, int spr_middle_id, int spr_end_id) {
        this.id = id;

        this.cell_width = cell_width;
        this.cell_height = cell_height;
        this.length = length;
        this.spr_begin_id = spr_begin_id;
        this.spr_middle_id = spr_middle_id;
        this.spr_end_id = spr_end_id;
    }

    @Override
    public void imgui() {
        ImGui.text("ID: " + id);
        ImGui.text("Cell width: " + cell_width);
        ImGui.text("Cell height: "+ cell_height);
        ImGui.text("Length: " + this.length);
        ImGui.text("Sprite_begin: " + this.spr_begin_id);
        ImGui.text("Sprite_middle: " + this.spr_middle_id);
        ImGui.text("Sprite_end: " + this.spr_end_id);
    }
}
