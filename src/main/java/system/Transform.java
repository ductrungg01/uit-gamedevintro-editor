package system;

import components.Component;
import components.INonAddableComponent;
import editor.Debug;
import editor.NiceImGui;
import editor.windows.InspectorWindow;
import imgui.ImGui;
import org.joml.Vector2f;

import java.text.DecimalFormat;
import java.util.List;

public class Transform extends Component implements INonAddableComponent {
    //region Fields
    public Vector2f position;
    public Vector2f scale;
    public float rotation = 0.0f;
    public int zIndex;

    public Vector2f previousPos = null;
    //endregion

    public Transform() {
        init(new Vector2f(), new Vector2f(0.25f, 0.25f));
    }

    public Transform(Vector2f position, Vector2f scale) {
        init(position, scale);
    }


    public void init(Vector2f position, Vector2f scale) {
        this.position = position;
        this.scale = scale;
        this.zIndex = 0;
    }

    public Transform copy() {
        return new Transform(new Vector2f(this.position), new Vector2f(this.scale));
    }

    public void copy(Transform to) {
        to.position.set(this.position);
        to.scale.set(this.scale);
    }

    @Override
    public void editorUpdate(float dt) {
        if (this.gameObject.isPlatform){
            if (previousPos != null) {
                Vector2f offset = new Vector2f(gameObject.transform.position.x - previousPos.x, gameObject.transform.position.y - previousPos.y);

                if (offset.x != 0 || offset.y != 0) {
                    int platformId = this.gameObject.platformId;
                    for (GameObject go : GameObject.platforms.get(platformId)) {
                        if (go.getUid() == this.gameObject.getUid()) continue;
                        go.transform.position.x += offset.x;
                        go.transform.position.y += offset.y;
                        go.transform.previousPos = new Vector2f(go.transform.position.x, go.transform.position.y);
                    }
                }
            }

            previousPos = new Vector2f(this.gameObject.transform.position.x, this.gameObject.transform.position.y);
        }
    }

    @Override
    public void imgui() {
        if (!this.gameObject.isPrefab) {
            ImGui.text("Gia tri cua position.y dang duoc *-1 de phu hop voi Editor\nKhi export cac gia tri cua Y se duoc dua ve dung gia tri");

            NiceImGui.drawVec2Control("Position", this.position, "Position of transform " + this.gameObject.hashCode());

            ImGui.text("Scale: " + this.scale.x + " : " + this.scale.y);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Transform)) return false;

        Transform t = (Transform) o;
        return t.position.equals(this.position) && t.scale.equals(this.scale) &&
                this.rotation == t.rotation && t.zIndex == this.zIndex;
    }
}
