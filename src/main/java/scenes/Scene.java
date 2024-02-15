package scenes;

import components.*;
import editor.*;
import org.joml.Vector2f;
import org.lwjgl.system.CallbackI;
import renderer.Renderer;
import renderer.Texture;
import system.*;
import util.CustomFileUtils;
import util.FileUtils;
import util.ProjectUtils;
import util.SceneUtils;

import java.io.*;
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

    List<String> assets = new ArrayList<>();

    void resetSceneInformation(){
        assets = new ArrayList<>();

        for (GameObject go: Window.getScene().getGameObjects()) {
            if (!go.isSpecialObject)
                go.destroy();
        }

        GameObject.platforms.clear();
        Prefab.PrefabLists.clear();

        ProjectUtils.sprites.clear();
    }

    void parseSceneFile(){
        if (filePath == null || filePath.fileAbsolutePath().isEmpty()) return;

        resetSceneInformation();

        int section = SceneConstants.SCENE_FILE_SECTION_UNKNOWN;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath.fileAbsolutePath()));

            String line;

            while ((line = reader.readLine()) != null) {
                line.trim();

                if (line.startsWith("#")) continue;

                if (line.equals("[ASSETS]")) { section = SceneConstants.SCENE_FILE_SECTION_ASSETS; continue;}
                if (line.equals("[OBJECTS]")) { section = SceneConstants.SCENE_FILE_SECTION_OBJECTS; continue;}
                if (line.startsWith("[")) { section = SceneConstants.SCENE_FILE_SECTION_UNKNOWN; continue; }

                switch (section) {
                    case SceneConstants.SCENE_FILE_SECTION_UNKNOWN -> System.out.println("[ERROR] Scene section : ");
                    case SceneConstants.SCENE_FILE_SECTION_ASSETS -> parseAssetsFile(line);
                    case SceneConstants.SCENE_FILE_SECTION_OBJECTS -> parseObjectsFile(line);
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

    //region Parse
    Sprite defaultSprite = FileUtils.getDefaultSprite();

    void createNewPrefab(String absoluteFilePath) {
        int lastSlashIndex = absoluteFilePath.lastIndexOf("\\");
        String newPrefabName = absoluteFilePath.substring(lastSlashIndex + 1);
        newPrefabName = FileUtils.getFileNameWithoutExtension(newPrefabName);
        newPrefabName = newPrefabName.toUpperCase();

        Prefab go = new Prefab(newPrefabName, defaultSprite);
        go.isPrefab = true;
        go.addComponent(new StateMachine());
        go.assetFilePath = absoluteFilePath;

        Prefab.PrefabLists.add(go);
    }

    boolean isExistedPrefab(String absoluteFilePath) {
        for (Prefab prefab: Prefab.PrefabLists) {
            if (prefab.assetFilePath.equals(absoluteFilePath)) {
                return true;
            }
        }
        return false;
    }

    void parseAssetsFile(final String file){
        if (file.isEmpty()) return;

        assets.add(file);
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

    void parseObjectsFile(String line){
        if (line.isEmpty()) return;

        String[] s = line.split("\\s+");
        if (s.length < 3) return;

        int objectID = Integer.parseInt(s[0]);
        float x = Float.parseFloat(s[1]);
        float y = -Float.parseFloat(s[2]);

        String objectName = "";
        switch (objectID){
            case OBJECT_ID.MARIO -> objectName = "MARIO";
            case OBJECT_ID.BRICK -> objectName = "BRICK";
            case OBJECT_ID.GOOMBA -> objectName = "GOOMBA";
            case OBJECT_ID.KOOPAS -> objectName = "KOOPAS";
            case OBJECT_ID.COIN -> objectName = "COIN";
            case OBJECT_ID.PLATFORM -> {
                float cell_w = Float.parseFloat(s[3]);
                float cell_h = Float.parseFloat(s[4]);
                int length = Integer.parseInt(s[5]);
                int spr_begin_id = Integer.parseInt(s[6]);
                int spr_middle_id = Integer.parseInt(s[7]);
                int spr_end_id = Integer.parseInt(s[8]);

                new Platform(x, y, cell_w, cell_h, length, spr_begin_id, spr_middle_id, spr_end_id);

                return;
            }
            case OBJECT_ID.PORTAL -> {
                float w = Float.parseFloat(s[3]) - x;
                float h = Float.parseFloat(s[4]) + y;
                String nextScene = ProjectUtils.getSceneName(Integer.parseInt(s[5]));

                Texture texture = new Texture();
                texture.init("./system-assets/images/Portal.png");
                Sprite spr = new Sprite(texture);
                GameObject newPortal = new GameObject("PORTAL", spr);
                PortalComponent portalComponent = new PortalComponent(nextScene, w, h);
                newPortal.transform.position = new Vector2f(x, y);
                newPortal.transform.zIndex = 5;
                newPortal.addComponent(portalComponent);

                Window.getScene().addGameObjectToScene(newPortal);
                return;
            }
        }

        Prefab prefab = Prefab.getPrefabByName(objectName);
        GameObject newGo = prefab.generateChildGameObject();

        newGo.transform.position = new Vector2f(x, y);

        addGameObjectToScene(newGo);
    }

    void parseSpriteFile(String line){
        if (line.isEmpty()) return;

        String[] tokens = line.split("\\s+");

        if (tokens.length < 3) return;

        int spriteId = Integer.parseInt(tokens[0]);
        Vector2f left_top_pos = new Vector2f(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        Vector2f right_bottom_pos = new Vector2f(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
        int textureId = Integer.parseInt(tokens[5]);

        ProjectUtils.addSpite(spriteId, left_top_pos, right_bottom_pos, textureId);

        GameObject prefab = Prefab.PrefabLists.get(Prefab.PrefabLists.size() - 1);
        if (prefab.getComponent(SpriteRenderer.class).getSprite() == defaultSprite) {
            prefab.getComponent(SpriteRenderer.class).setSprite(ProjectUtils.getSprite(spriteId));
        }
    }

    void parseAnimationFile(String line){
        if (line.isEmpty()) return;

        GameObject prefab = Prefab.PrefabLists.get(Prefab.PrefabLists.size() - 1);
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

    //region export scene
    public void exportScene(){
        if (SceneUtils.CURRENT_SCENE.isEmpty()) return;

        String fileName = "./export/" + SceneUtils.CURRENT_SCENE + ".txt";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);

            String s = "";
            s += "#\n[ASSETS]\n";
            for (String asset: assets) {
                s += asset + "\n";
            }

            s += "#\n[OBJECTS]\n";
            for (GameObject go: Window.getScene().getGameObjects()) {
                if (go.isPlatform) continue;

                int objID = -1;

                if (go.name.equals("MARIO")) objID = 0;
                if (go.name.equals("BRICK")) objID = 1;
                if (go.name.equals("GOOMBA")) objID = 2;
                if (go.name.equals("KOOPAS")) objID = 3;
                if (go.name.equals("COIN")) objID = 4;
                if (go.name.equals("PORTAL")) objID = 50;

                if (objID == -1) continue;

                float x = go.transform.position.x;
                float y = -go.transform.position.y;

                if (objID != 50)
                    s += objID + "\t" +  x + "\t" + y + "\n";
                else {
                    PortalComponent portalComponent = go.getComponent(PortalComponent.class);
                    float w = x + portalComponent.scale.x;
                    float h = y + portalComponent.scale.y;
                    int nextSceneId = ProjectUtils.getSceneId(portalComponent.nextScene );
                    s += objID + "\t" + x + "\t" + y + "\t" + w + "\t" + h + "\t" + nextSceneId + "\n";
                }
            }

            for (Map.Entry<Integer, List<GameObject>> entry : GameObject.platforms.entrySet()) {
                List<GameObject> platformList = entry.getValue();
                for (GameObject go: platformList) {
                    if (go.name.contains("begin")){
                        PlatformInfor infor = go.getComponent(PlatformInfor.class);

                        float x = go.transform.position.x;
                        float y = -go.transform.position.y;
                        float cell_w = infor.cell_width;
                        float cell_h = infor.cell_height;
                        int length = platformList.size();
                        int spr_begin_id = infor.spr_begin_id;
                        int spr_middle_id = infor.spr_middle_id;
                        int spr_end_id = infor.spr_end_id;

                        s += String.format("5\t%f\t%f\t%f\t%f\t%d\t%d\t%d\t%d\n",
                                x, y, cell_w, cell_h, length, spr_begin_id, spr_middle_id, spr_end_id);
                        break;
                    }
                }

            }

            writer.write(s);

            writer.close();

            Debug.Log("Export scene '" + fileName + "' SUCCESSFULLY!" );
        } catch (IOException e) {
            e.printStackTrace();
            Debug.Log("Export scene '" + fileName + "' ERROR: " + e.getMessage() );
        }
    }

    //endregion

    //endregion
}
