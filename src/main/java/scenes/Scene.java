package scenes;

import components.*;
import editor.*;
import org.joml.Vector2f;
import org.lwjgl.system.CallbackI;
import renderer.Renderer;
import system.*;
import util.CustomFileUtils;
import util.FileUtils;
import util.ProjectUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Scene {
    // region Fields
    private Renderer renderer;
    private Camera camera;
    private boolean isRunning;
    private List<GameObject> gameObjects;
    private List<GameObject> pendingObjects;
    private SceneInitializer sceneInitializer;
    private MouseControls mouseControls = new MouseControls();
    private KeyControls keyControls = new KeyControls();
    private CustomFileUtils filePath;
    //endregion

    //region Constructors
    public Scene(SceneInitializer sceneInitializer) {
        this.sceneInitializer = sceneInitializer;
        this.renderer = new Renderer();
        this.gameObjects = new ArrayList<>();
        this.pendingObjects = new ArrayList<>();
        this.isRunning = false;
    }
    //endregion

    //region Properties
    public List<GameObject> getGameObjects() {
        return this.gameObjects;
    }

    public GameObject getGameObject(int gameObjectId) {
        Optional<GameObject> result = this.gameObjects.stream()
                .filter(gameObject -> gameObject.getUid() == gameObjectId)
                .findFirst();

        return result.orElse(null);
    }
    //endregion

    //region Methods
    public void init() {
        this.filePath = ProjectUtils.getCurrentScenePath();
        this.camera = new Camera(new Vector2f(-40, -Camera.screenSize.y - 30));
        this.camera.setDefaultZoom();
        this.sceneInitializer.loadResources(this);
        this.sceneInitializer.init(this);
    }

    public void start() {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            go.start();
            this.renderer.add(go);
        }
        isRunning = true;
    }

    public void addGameObjectToScene(GameObject go) {
        if (!isRunning) {
            gameObjects.add(go);
        } else {
            pendingObjects.add(go);
        }
    }

    public void removeAllGameObjectInScene() {
        this.gameObjects.clear();
    }

    public void destroy() {
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

    public void editorUpdate(float dt) {
        this.camera.adjustProjection();
        mouseControls.editorUpdate(dt);
        keyControls.editorUpdate(dt);

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);

            go.editorUpdate(dt);

            if (go.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(go);
                i--;
            }
        }

        for (GameObject go : pendingObjects) {
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
        }
        pendingObjects.clear();
    }

    public void update(float dt) {
        this.camera.adjustProjection();

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            go.update(dt);

            if (go.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(go);
                i--;
            }
        }

        for (GameObject go : pendingObjects) {
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
        }
        pendingObjects.clear();
    }

    public void render() {
        this.renderer.render();
    }

    public Camera camera() {
        return this.camera;
    }

    public MouseControls getMouseControls() {
        return this.mouseControls;
    }

    public KeyControls getKeyControls() {
        return this.keyControls;
    }

    public void imgui() {
        this.sceneInitializer.imgui();
    }

    public void load(){
        parseSceneFile();
    }

    private static class SceneConstants {
        public static final int SCENE_FILE_SECTION_UNKNOWN = -1;
        public static final int SCENE_FILE_SECTION_ASSETS = 1;
        public static final int SCENE_FILE_SECTION_OBJECTS = 2;
    }

    private static class OBJECT_ID {
        public static final  int NONE = -10000;
        public static final int MARIO = 0;
        public static final int BRICK = 1;
        public static final int GOOMBA = 2;
        public static final int KOOPAS = 3;
        public static final int COIN = 4;
        public static final int PLATFORM = 5;
        public static final int PORTAL = 50;

    }

    void parseSceneFile(){
        if (filePath == null || filePath.fileAbsolutePath().isEmpty()) return;

        int section = SceneConstants.SCENE_FILE_SECTION_UNKNOWN;
        int object_section = OBJECT_ID.NONE;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath.fileAbsolutePath()));

            String line;

            while ((line = reader.readLine()) != null) {
                line.trim();

                if (line.startsWith("#")) {
                    if (section == SceneConstants.SCENE_FILE_SECTION_OBJECTS) {
                        line.substring(1, line.length());
                        line.trim();
                        line = line.toUpperCase();

                        if (line.equals("MARIO")) object_section = OBJECT_ID.MARIO;
                        else if (line.equals("BRICKS")) object_section = OBJECT_ID.BRICK;
                        else if (line.equals("GOOMBA")) object_section = OBJECT_ID.GOOMBA;
                        else if (line.equals("KOOPAS")) object_section = OBJECT_ID.KOOPAS;
                        else if (line.equals("COIN")) object_section = OBJECT_ID.COIN;
                        else if (line.equals("PLATFORM")) object_section = OBJECT_ID.PLATFORM;
                        else if (line.equals("PORTAL")) object_section = OBJECT_ID.PORTAL;
                        else {
                            System.out.println("[ERROR] Unknown asset : " + line);
                        }
                    }
                    continue;
                }

                if (line.equals("[ASSETS]")) { section = SceneConstants.SCENE_FILE_SECTION_ASSETS; continue;}
                if (line.equals("[OBJECTS]")) { section = SceneConstants.SCENE_FILE_SECTION_OBJECTS; continue;}
                if (line.startsWith("[")) { section = SceneConstants.SCENE_FILE_SECTION_UNKNOWN; continue; }

                switch (section) {
                    case SceneConstants.SCENE_FILE_SECTION_UNKNOWN -> System.out.println("[ERROR] Scene section : ");
                    case SceneConstants.SCENE_FILE_SECTION_ASSETS -> parseAssetsFile(line);
                    case SceneConstants.SCENE_FILE_SECTION_OBJECTS -> parseObjectsFile(line, object_section);
                }

            }

            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static class AssetConstants {
        public static final int ASSET_FILE_SECTION_UNKNOWN = -1;
        public static final int ASSET_FILE_SECTION_SPRITES = 1;
        public static final int ASSET_FILE_SECTION_ANIMATIONS = 2;
    }

    Sprite defaultSprite = FileUtils.getDefaultSprite();

    void createNewPrefab(String absoluteFilePath) {
        int lastSlashIndex = absoluteFilePath.lastIndexOf("\\");
        String newPrefabName = absoluteFilePath.substring(lastSlashIndex + 1);
        newPrefabName = FileUtils.getFileNameWithoutExtension(newPrefabName);
        newPrefabName = newPrefabName.toUpperCase();

        GameObject go = new GameObject(newPrefabName, defaultSprite);
        go.isPrefab = true;
        go.addComponent(new StateMachine());
        go.assetFilePath = absoluteFilePath;

        GameObject.PrefabLists.add(go);
    }

    boolean isExistedPrefab(String absoluteFilePath) {
        for (GameObject go: GameObject.PrefabLists) {
            if (go.assetFilePath.equals(absoluteFilePath)) {
                return true;
            }
        }
        return false;
    }

    void parseAssetsFile(final String file){
        if (file.isEmpty()) return;

        String absoluteFilePath = filePath.fileParentDirectory() + file;

        if (isExistedPrefab(absoluteFilePath)) return;
        createNewPrefab(absoluteFilePath);

        int section = AssetConstants.ASSET_FILE_SECTION_UNKNOWN;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(absoluteFilePath));

            String line;

            while ((line = reader.readLine()) != null) {
                line.trim();

                if (line.startsWith("#")) continue;;

                if (line.equals("[SPRITES]")) { section = AssetConstants.ASSET_FILE_SECTION_SPRITES; continue;}
                if (line.equals("[ANIMATIONS]")) {section = AssetConstants.ASSET_FILE_SECTION_ANIMATIONS; continue;}
                if (line.startsWith("[")) {section = AssetConstants.ASSET_FILE_SECTION_UNKNOWN; continue;}

                switch (section){
                    case AssetConstants.ASSET_FILE_SECTION_UNKNOWN -> System.out.println("[ERROR] Asset section : ");
                    case AssetConstants.ASSET_FILE_SECTION_SPRITES -> parseSpriteFile(line);
                    case AssetConstants.ASSET_FILE_SECTION_ANIMATIONS -> parseAnimationFile(line);
                }
            }

            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void parseObjectsFile(String line, int ObjectID){

    }

    void parseSpriteFile(String line){
        if (line.isEmpty()) return;

        String[] tokens = line.split("\\s+");
        int spriteId = Integer.parseInt(tokens[0]);
        Vector2f left_top_pos = new Vector2f(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        Vector2f right_bottom_pos = new Vector2f(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
        int textureId = Integer.parseInt(tokens[5]);

        ProjectUtils.addSpite(spriteId, left_top_pos, right_bottom_pos, textureId);

        GameObject prefab = GameObject.PrefabLists.get(GameObject.PrefabLists.size() - 1);
        if (prefab.getComponent(SpriteRenderer.class).getSprite() == defaultSprite) {
            prefab.getComponent(SpriteRenderer.class).setSprite(ProjectUtils.getSprite(spriteId));
        }
    }

    void parseAnimationFile(String line){
        if (line.isEmpty()) return;

        GameObject prefab = GameObject.PrefabLists.get(GameObject.PrefabLists.size() - 1);
        StateMachine stateMachine = prefab.getComponent(StateMachine.class);

        String[] s = line.split("\\s+");

        AnimationState animationState = new AnimationState();
        animationState.title = s[0];
        animationState.setLoop(true);

        for (int i = 1; i < s.length; i += 2){
            int spriteId = Integer.parseInt(s[i]);
            float time = Integer.parseInt(s[i + 1]);
            time /= 1000f;
            Sprite sprite = ProjectUtils.getSprite(spriteId);
            animationState.addFrame(sprite, time);
        }

        stateMachine.addState(animationState);
    }
    //endregion
}
