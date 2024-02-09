package system;

import components.*;
import editor.Debug;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;
import util.Settings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static editor.uihelper.NiceShortCall.*;

public class GameObject {
    public static List<GameObject> PrefabLists = new ArrayList<>();
    //region Fields
    private static int ID_COUNTER = 0;
    public String name = "";
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

    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public static GameObject getPrefabById(String prefabId) {
        for (GameObject prefab : GameObject.PrefabLists) {
            if (prefab.prefabId.equals(prefabId)) {
                return prefab;
            }
        }

        return null;
    }

    public static int getCurrentMaxUid() {
        return ID_COUNTER;
    }

    public static void setCurrentMaxUid(int idMax) {
        ID_COUNTER = idMax;
    }

    //region Methods
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

    // A Child object override it's prefab
    public void overrideThePrefab() {
        for (int i = 0; i < GameObject.PrefabLists.size(); i++) {
            GameObject p = GameObject.PrefabLists.get(i);

            if (p.prefabId.equals(this.parentId)) {
                GameObject newPrefab = this.copy();
                newPrefab.isPrefab = true;
                newPrefab.prefabId = this.parentId;

                newPrefab.transform.position = new Vector2f();

                GameObject.PrefabLists.set(i, newPrefab);
                newPrefab.start();

                JOptionPane.showMessageDialog(null, "Override the prefab successful!",
                        "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }


        Debug.Log("Cannot find the prefab!");
    }

    public void removeAsPrefab() {
        for (GameObject go : Window.getScene().getGameObjects()) {
            if (go.parentId.equals(this.prefabId) ||
                    (go.isPrefab && go.prefabId.equals(this.prefabId))) {
                go.parentId = "";
            }
        }

        GameObject.PrefabLists.remove(this);
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

    /**
     * Update is called once per frame
     *
     * @param dt : The interval in seconds from the last frame to the current one
     */
    public void update(float dt) {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).update(dt);
        }
    }
    //endregion

    /**
     * Start is called before the first frame update
     */
    public void start() {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).start();
        }
    }

    public void imgui() {
        if (!this.isPrefab) {
            Vector4f warning_col = COLOR_Yellow;
            String warning_text = "This is the CHILD game object\nYou CANNOT change anything (except position)!\nIf you want to change something, click prefab";
            ImGui.textColored(warning_col.x, warning_col.y, warning_col.z, warning_col.w,
                    warning_text);
        } else {
            Vector4f warning_col = COLOR_Yellow;
            String warning_text = "This is the PREFAB, your change in this prefab will override all child game object!";
            ImGui.textColored(warning_col.x, warning_col.y, warning_col.z, warning_col.w,
                    warning_text);
        }

        ImGui.separator();


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

            if (c instanceof Transform || c instanceof SpriteRenderer) {
                // Because Transform and SpriteRenderer is cannot be removed!!!
                if (ImGui.collapsingHeader(c.getClass().getSimpleName())) {
                    c.imgui();
                }
            } else if (c instanceof StateMachine) {
                ImBoolean removeComponentButton = new ImBoolean(true);

                if (ImGui.collapsingHeader(c.getClass().getSimpleName(), removeComponentButton)) {
                    c.imgui();
                }

                if (!removeComponentButton.get() && this.isPrefab) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "Remove component '" + c.getClass().getSimpleName() + "' from game object '" + this.name + "'?",
                            "REMOVE COMPONENT",
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        components.remove(i);
                        i--;
                    }
                }
            }
        }

        if (this.isPrefab()) {
            this.overrideAllChildGameObject();
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

    public void setAsPrefab() {
        GameObject prefab = this.copy();

        prefab.transform.position = new Vector2f();
        prefab.isPrefab = true;
        prefab.isDead = true;
        prefab.parentId = "";
        prefab.generatePrefabId();
        GameObject.PrefabLists.add(prefab);
        prefab.start();

        this.parentId = prefab.prefabId;
    }

    public void setIsNotPrefab() {
        this.isPrefab = false;
        this.prefabId = "";
        GameObject.PrefabLists.remove(this);
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

    public void overrideAllChildGameObject() {
        List<GameObject> gameObjects = Window.getScene().getGameObjects();

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            if (go.parentId.equals(this.prefabId)) {
                Vector2f oldPosition = go.transform.position;
                String oldName = go.name;

                go.destroy();
                GameObject newGameObject = this.copyFromPrefab();
                newGameObject.transform.position = oldPosition;
                newGameObject.name = oldName;
                Window.getScene().addGameObjectToScene(newGameObject);
            }
        }

//        JOptionPane.showMessageDialog(null, "Override all children successful!",
//                "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
    }

    public void generatePrefabId() {
        do {
            this.prefabId = generateRandomString();
        } while (isExistedId(this.prefabId));
    }

    private boolean isExistedId(String id) {
        for (GameObject go : GameObject.PrefabLists) {
            String prefabId = go.prefabId;
            if (id.equals(prefabId)) {
                return true;
            }
        }
        return false;
    }

    private String generateRandomString() {
        int length = 10;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }
    //endregion
}
