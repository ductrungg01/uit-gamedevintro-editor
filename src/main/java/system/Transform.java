package system;

import components.Component;
import components.INonAddableComponent;
import editor.NiceImGui;
import imgui.ImGui;
import org.joml.Vector2f;

import java.text.DecimalFormat;

public class Transform extends Component implements INonAddableComponent {
    //region Fields
    public Vector2f position;
    public Vector2f scale;
    public float rotation = 0.0f;
    public int zIndex;
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
    public void imgui() {
        if (!this.gameObject.isPrefab) {
            NiceImGui.drawVec2Control("Position", this.position, "Position of transform " + this.gameObject.hashCode());
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
