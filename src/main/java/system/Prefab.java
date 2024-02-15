package system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.Sprite;
import components.SpriteRenderer;
import deserializers.ComponentDeserializer;
import deserializers.PrefabDeserializer;
import org.joml.Vector2f;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public class Prefab extends GameObject{

    public static List<Prefab> PrefabLists = new ArrayList<>();

    public Prefab(){
        this.name = "New prefab";
        this.uid = ID_COUNTER++;
    }

    public Prefab(String name){
        this.name = name;

        this.addComponent(new Transform());
        this.transform = this.getComponent(Transform.class);
    }

    public Prefab(String name, Sprite spr){
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

    public Prefab(String name, Sprite spr, Vector2f size) {
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

    public static Prefab getPrefabByName(String name){
        for (Prefab prefab: PrefabLists) {
            if (prefab.name.equals(name)){
                return prefab;
            }
        }

        return null;
    }

    // Prefab create a child game object
    public GameObject copyFromPrefab() {
        // TODO: come up with cleaner solution
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new PrefabDeserializer())
                .enableComplexMapKeySerialization()
                .create();
        String objAsJson = gson.toJson(this);
        GameObject obj = gson.fromJson(objAsJson, GameObject.class);

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

    public GameObject generateChildGameObject() {
        if (!this.isPrefab) return null;
        GameObject newGo = this.copyFromPrefab();

        newGo.isPrefab = false;
        newGo.prefabId = "";
        newGo.isDead = false;
        newGo.parentId = this.prefabId;

        return newGo;
    }
}
