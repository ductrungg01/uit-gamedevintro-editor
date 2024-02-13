package system;

import components.*;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;
import util.Settings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.*;

public class GameObject {
    //region Fields
    public static List<GameObject> PrefabLists = new ArrayList<>();
    private static int ID_COUNTER = 0;
    public String name = "";
    public String assetFilePath = "";
    public boolean isPrefab = false;
    public String prefabId = "";
    public String parentId = "";
    public transient Transform transform;
    private int uid = -1;
    private List<Component> components = new ArrayList<>();
    private boolean doSerialization = false;
    private boolean isDead = false;
    //endregion

    //region Constructors
    public GameObject() {
        this.name = "New GameObject";

        this.uid = ID_COUNTER++;
    }

    public GameObject(String name) {
        this.name = name;

        this.addComponent(new Transform());
        this.transform = this.getComponent(Transform.class);
    }

    public GameObject(String name, Sprite spr) {
        if (spr.getTexId() == -1) {
            String texturePath = spr.getTexture().getFilePath();
            spr.setTexture(AssetPool.getTexture(texturePath));
        }

        Vector2f size = new Vector2f(spr.getWidth(), spr.getHeight());

        this.name = name;

        this.addComponent(new Transform());
        this.transform = this.getComponent(Transform.class);
        this.transform.scale.x = size.x;
        this.transform.scale.y = size.y;
        SpriteRenderer renderer = new SpriteRenderer();
        this.addComponent(renderer);
        renderer.setSprite(spr);

        this.uid = ID_COUNTER++;
    }

    public GameObject(String name, Sprite spr, Vector2f size) {
        if (spr.getTexId() == -1) {
            String texturePath = spr.getTexture().getFilePath();
            spr.setTexture(AssetPool.getTexture(texturePath));
        }

        this.name = name;

        this.addComponent(new Transform());
        this.transform = this.getComponent(Transform.class);
        this.transform.scale.x = size.x;
        this.transform.scale.y = size.y;
        SpriteRenderer renderer = new SpriteRenderer();
        this.addComponent(renderer);
        renderer.setSprite(spr);

        this.uid = ID_COUNTER++;
    }
    //endregion

    //region Methods
    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public GameObject copy() {
        // TODO: Gameobject.copy()
        GameObject obj = new GameObject();

        obj.generateUid();

        for (Component c : obj.getAllComponents()) {
            c.generateId();
        }

        obj.refreshTexture();

        return obj;
    }

    // Prefab create a child game object
    public GameObject copyFromPrefab() {
        // TODO: Prefab.copy()
        GameObject obj = new GameObject();

        obj.generateUid();

        for (Component c : obj.getAllComponents()) {
            c.generateId();
        }

        obj.refreshTexture();

        obj.prefabId = "";
        obj.parentId = this.prefabId;
        obj.isPrefab = false;

        return obj;
    }

    // TODO: This is temporary method before we find out the correctly method
    public void refreshTexture() {
        if (this.getComponent(SpriteRenderer.class) != null) {
            SpriteRenderer spr = this.getComponent(SpriteRenderer.class);
            if (spr != null) {
                spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilePath()));
            }
        }

        if (this.getComponent(StateMachine.class) != null) {
            StateMachine stateMachine = this.getComponent(StateMachine.class);
            stateMachine.refreshTextures();
        }
    }

    public void destroy() {
        isDead = true;
        for (int i = 0; i < components.size(); i++) {
            components.get(i).destroy();
        }
    }

    public void update(float dt) {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).update(dt);
        }
    }
    //endregion

    public void start() {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).start();
        }
    }

    public void imgui() {
        if (!this.isPrefab) {
            ImGui.text("Object's name: ");
            ImGui.sameLine();

            ImGui.textColored(Settings.NAME_COLOR.x, Settings.NAME_COLOR.y, Settings.NAME_COLOR.z, Settings.NAME_COLOR.w,
                    this.name);
        }else {
            ImGui.textColored(Settings.NAME_COLOR.x, Settings.NAME_COLOR.y, Settings.NAME_COLOR.z, Settings.NAME_COLOR.w,
                    "Name: " + this.name);
        }

        ImGui.separator();

        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            if (ImGui.collapsingHeader(c.getClass().getSimpleName())) {
                c.imgui();
            }
        }
    }

    public void editorUpdate(float dt) {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).editorUpdate(dt);
        }
    }

    //region Properties
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: Casting component";
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        c.generateId();
        this.components.add(c);
        c.gameObject = this;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getUid() {
        return this.uid;
    }

    public List<Component> getAllComponents() {
        return this.components;
    }

    public void setNoSerialize() {
        this.doSerialization = false;
    }

    public void setSerialize() {
        this.doSerialization = true;
    }

    public void generateUid() {
        this.uid = ID_COUNTER++;
    }

    public boolean doSerialization() {
        return this.doSerialization;
    }

    public boolean isPrefab() {
        return this.isPrefab;
    }

    public GameObject generateChildGameObject() {
        if (!this.isPrefab) return null;
        GameObject newGo = this.copyFromPrefab();

        newGo.isPrefab = false;
        newGo.prefabId = "";
        newGo.isDead = false;
        newGo.parentId = this.prefabId;

        return newGo;
    }
    //endregion
}
