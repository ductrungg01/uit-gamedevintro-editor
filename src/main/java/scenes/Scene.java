package scenes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.*;
import deserializers.ComponentDeserializer;
import deserializers.GameObjectDeserializer;
import deserializers.PrefabDeserializer;
import editor.*;
import editor.windows.AssetsWindow;
import editor.windows.OpenSceneWindow;
import editor.windows.SceneHierarchyWindow;
import editor.windows.SceneList;
import imgui.ImGui;
import org.joml.Vector2f;
import physics2d.Physics2D;
import renderer.Renderer;
import system.*;
import util.FileUtils;
import util.SceneUtils;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class Scene {
    //region Fields
    final String LEVEL_PATH = "level.txt";
    final String PREFAB_PATH = "prefabs.txt";
    //region Fields
    private Renderer renderer;
    private Camera camera;
    private boolean isRunning;
    private List<GameObject> gameObjects;
    private List<GameObject> pendingObjects;
    private Physics2D physics2D;
    //endregion
    private SceneInitializer sceneInitializer;
    private MouseControls mouseControls = new MouseControls();
    private KeyControls keyControls = new KeyControls();
    //endregion

    //region Constructors
    public Scene(SceneInitializer sceneInitializer) {
        this.sceneInitializer = sceneInitializer;
        this.physics2D = new Physics2D();
        this.renderer = new Renderer();
        this.gameObjects = new ArrayList<>();
        this.pendingObjects = new ArrayList<>();
        this.isRunning = false;
    }
    //endregion

    //region Properties
    public Physics2D getPhysics() {
        return this.physics2D;
    }

    public List<GameObject> getGameObjects() {
        return this.gameObjects;
    }

    public GameObject getGameObject(int gameObjectId) {
        Optional<GameObject> result = this.gameObjects.stream()
                .filter(gameObject -> gameObject.getUid() == gameObjectId)
                .findFirst();

        return result.orElse(null);
    }

    public GameObject getGameObject(String gameObjectName) {
        Optional<GameObject> result = this.gameObjects.stream()
                .filter(gameObject -> gameObject.name.equals(gameObjectName))
                .findFirst();

        return result.orElse(null);
    }
    //endregion

    //region Methods
    public void init() {
        this.camera = new Camera(new Vector2f(-40, -Camera.screenSize.y - 30));
        this.camera.setDefaultZoom();
        this.sceneInitializer.loadResources(this);
        this.sceneInitializer.init(this);
    }

    /**
     * Start is called before the first frame update
     */
    public void start() {

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            go.start();
            this.renderer.add(go);
            this.physics2D.add(go);
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

    public <T extends Component> GameObject findGameObjectWith(Class<T> clazz) {
        for (GameObject go : gameObjects) {
            if (go.getComponent(clazz) != null) {
                return go;
            }
        }

        return null;
    }

    public <T extends Component> List<GameObject> findAllGameObjectWith(Class<T> clazz) {
        List<GameObject> gameObjectList = new ArrayList<>();

        for (GameObject go : gameObjects) {
            if (go.getComponent(clazz) != null) {
                gameObjectList.add(go);
            }
        }

        return gameObjectList;
    }

    public List<GameObject> findAllGameObjectWithTag(String tag) {
        List<GameObject> gameObjectList = new ArrayList<>();

        for (GameObject go : this.gameObjects) {
            if (go.compareTag(tag)) {
                gameObjectList.add(go);
            }
        }

        return gameObjectList;
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
                this.physics2D.destroyGameObject(go);
                i--;
            }
        }

        for (GameObject go : pendingObjects) {
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
            this.physics2D.add(go);
        }
        pendingObjects.clear();
    }

    /**
     * Update is called once per frame
     *
     * @param dt : The interval in seconds from the last frame to the current one
     */
    public void update(float dt) {
        this.camera.adjustProjection();
        this.physics2D.update(dt);

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            go.update(dt);

            if (go.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(go);
                this.physics2D.destroyGameObject(go);
                i--;
            }
        }

        for (GameObject go : pendingObjects) {
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
            this.physics2D.add(go);
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
    //endregion

    //region Save and Load
    public GameObject getPortalPrefab() {
        GameObject go = new GameObject("Portal", new Sprite("system-assets/images/Portal.png"));
        go.tag = "Portal";
        go.getComponent(SpriteRenderer.class).convertToScale();
        go.transform.position = new Vector2f();
        go.isPrefab = true;
        go.parentId = "";
        go.generatePrefabId();
        go.addComponent(new PortalComponent());
        go.start();
        return go;
    }

    public void save(boolean isShowMessage) {
        Window.getImguiLayer().getInspectorWindow().clearSelected();
        SceneHierarchyWindow.clearSelectedGameObject();
        if (SceneUtils.CURRENT_SCENE.isEmpty()) return;
        String level_path = "data\\" + SceneUtils.CURRENT_SCENE + "\\" + LEVEL_PATH;
        String prefab_path = "data\\" + PREFAB_PATH;
        String portal_path = "data\\portal.txt";

        GameObject portal_prefab = null;

        //region Save Game Object
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        try {
            FileWriter writer = new FileWriter(level_path);

            List<GameObject> objsToSerialize = new ArrayList<>();
            for (GameObject obj : this.gameObjects) {
                if (obj.doSerialization()) {
                    objsToSerialize.add(obj);
                }
            }

            writer.write(gson.toJson(objsToSerialize));
            writer.close();
            if (isShowMessage) {
                //MessageBox.setContext(true, MessageBox.TypeOfMsb.NORMAL_MESSAGE, "Save scene '" + SceneUtils.CURRENT_SCENE + "' successfully");
                Debug.Log("Save scene '" + SceneUtils.CURRENT_SCENE + "' successfully", LogType.Success);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isShowMessage) {
                //MessageBox.setContext(true, MessageBox.TypeOfMsb.NORMAL_MESSAGE, "Save failed");
                Debug.Log("Save scene '" + SceneUtils.CURRENT_SCENE + "' FAIL", LogType.Error);
            }
        }
        //endregion

        //region Save Prefabs
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new PrefabDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        try {
            FileWriter writer = new FileWriter(prefab_path);

            List<GameObject> objsToSerialize = GameObject.PrefabLists;

            for (int i = 0; i < objsToSerialize.size(); i++) {
                if (objsToSerialize.get(i).tag.equals("Portal")) {
                    portal_prefab = objsToSerialize.get(i);
                    objsToSerialize.remove(i);
                    break;
                }
            }

            writer.write(gson.toJson(objsToSerialize));
            writer.close();
            if (isShowMessage) {
                //MessageBox.setContext(true, MessageBox.TypeOfMsb.NORMAL_MESSAGE, "Save scene '" + SceneUtils.CURRENT_SCENE + "' successfully");
                Debug.Log("Save prefab successfully", LogType.Success);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isShowMessage) {
                Debug.Log("Save prefab FAIL", LogType.Error);
                //MessageBox.setContext(true, MessageBox.TypeOfMsb.NORMAL_MESSAGE, "Save failed");
            }
        }
        //endregion

        //region Portal
        if (portal_prefab == null) {
            portal_prefab = getPortalPrefab();
        }

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new PrefabDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        try {
            FileWriter writer = new FileWriter(portal_path);
            List<GameObject> objsToSerialize = new ArrayList<>();
            objsToSerialize.add(portal_prefab);

            writer.write(gson.toJson(objsToSerialize));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        try {
            String prefab_list_path = "Scene Manager\\prefab_list.txt";
            String scene_path = "Scene Manager\\scene_" + SceneUtils.CURRENT_SCENE + ".txt";
            String project_define_path = "Scene Manager\\project_define.txt";
            FileWriter writerScene = new FileWriter(scene_path);
            FileWriter writerPrefabList = new FileWriter(prefab_list_path);
            FileWriter writerProject = new FileWriter(project_define_path);

            //region Project Info
            int startSceneId = 2; //get
            int portalObjectId = 50;
            writerProject.write("[SETTINGS]\n");
            writerProject.write("start\t" + startSceneId + "\n");
            writerProject.write("width\t" + (int) Math.ceil(Camera.screenSize.x) + "\n");
            writerProject.write("height\t" + (int) Math.ceil(Camera.screenSize.y) + "\n");

            writerProject.write("\n#id\tfile\n");
            writerProject.write("[SCENES]\n");
            for (int i = 0; i < SceneList.scenes.size(); i++) {
                writerProject.write(i + "\t" + SceneList.scenes.get(i) + "\n");
            }

            writerProject.write("\n#id\tfile\n");
            writerProject.write("[TEXTURES]\n");
            File[] listOfFiles = new File(AssetsWindow.ROOT_FOLDER).listFiles();
            List<String> listTexturesFile = new ArrayList<>();
            int textureIndex = 0;
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile() && FileUtils.isImageFile(listOfFiles[i])) {
                    listTexturesFile.add(FileUtils.getFileName(listOfFiles[i].getPath()));
                    writerProject.write(textureIndex * 10 + "\ttextures\\" + listTexturesFile.get(listTexturesFile.size() - 1) + "\n");
                    textureIndex++;
                }
            }

            //endregion

            //region Game Info
            String sceneAssets = "# list of object assets to load in this scene\n[ASSETS]\n";
            List<String> prefabNameList = new ArrayList<>();
            List<String> listFileUse = new ArrayList<>();

            //save prefab
            for (int i = 0; i < GameObject.PrefabLists.size(); i++) {
                GameObject prefab = GameObject.PrefabLists.get(i);
                prefabNameList.add(prefab.name);
                writerPrefabList.write(i + "\t" + prefabNameList.get(i) + "\n");
                FileWriter writerPrefabInfo = new FileWriter("Scene Manager\\go_" + prefabNameList.get(i) + ".txt");
                String spiteList = "# id\tleft\ttop\tright\tbottom\ttexture_id\n[SPRITES]";
                String animationList = "# ani_id\tsprite1_id\ttime1\tsprite2_id\ttime2\t...\n[ANIMATIONS]";

                StateMachine stateMachine = prefab.getComponent(StateMachine.class);
                if (stateMachine == null) continue;
                for (int stateIndex = 0; stateIndex < stateMachine.getStates().size(); stateIndex++) {
                    AnimationState state = stateMachine.getStates().get(stateIndex);
                    spiteList += "\n#" + state.title.toUpperCase() + "\n";
                    int animationId = (i + 1) * 10000 + stateIndex * 1000 + 100;
                    animationList += "\n#" + state.title.toUpperCase() + "\n" + animationId;
                    for (int frameIndex = 0; frameIndex < state.animationFrames.size(); frameIndex++) {
                        Sprite sprite = state.animationFrames.get(frameIndex).sprite;
                        String textureFile = FileUtils.getFileName(sprite.getTexture().getFilePath());
                        if (textureFile.equals("Default Sprite.png")) continue;
                        if (listFileUse.indexOf(textureFile) == -1) listFileUse.add(textureFile);
                        int spriteId = (i + 1) * 10000 + stateIndex * 1000 + frameIndex;

                        animationList += "\t" + spriteId + "\t" + (int) state.animationFrames.get(frameIndex).frameTime;

                        //region get position sprite
                        Vector2f[] texCoords = sprite.getTexCoords();
                        float img_size_width = sprite.getTexture().getWidth();
                        float img_size_height = sprite.getTexture().getHeight();
                        DecimalFormat df = new DecimalFormat("#.##");
                        Vector2f topLeftCoord = new Vector2f(
                                texCoords[3].x * img_size_width,
                                texCoords[3].y * img_size_height
                        );
                        Vector2f bottomRightCoord = new Vector2f(
                                texCoords[1].x * img_size_width,
                                texCoords[1].y * img_size_height
                        );
                        //endregion

                        spiteList += spriteId + "\t"
                                + df.format(topLeftCoord.x) + " " + df.format(topLeftCoord.y)
                                + " " + df.format(bottomRightCoord.x) + " " + df.format(bottomRightCoord.y) + " " + listTexturesFile.indexOf(textureFile) + "\n";
                    }
                    animationList += "\n";
                }
                writerPrefabInfo.write(spiteList + "\n");
                writerPrefabInfo.write(animationList + "\n");
                writerPrefabInfo.close();
            }

            // sort game object
            for (int i = 1; i < this.gameObjects.size() - 1; i++) {
                for (int j = i + 1; j < this.gameObjects.size(); j++) {
                    if (this.gameObjects.get(i).name.compareTo(this.gameObjects.get(j).name) > 0) {
                        Collections.swap(this.gameObjects, i, j);
                    }
                }
            }

            for (int i = 0; i < listFileUse.size(); i++) {
                sceneAssets += listFileUse.get(i) + "\n";
            }
            writerScene.write(sceneAssets);

            //game object
            writerScene.write("\n[OBJECTS]\n");
            writerScene.write("# type\tx\ty\textra_settings per object type\n");
            String lastObjectName = "";
            for (GameObject obj : this.gameObjects) {
                if (obj.doSerialization() && !obj.name.equals("Portal")) {
                    if (!lastObjectName.equals(obj.name)) {
                        writerScene.write("\n#" + (!obj.tag.equals("") ? obj.tag.toUpperCase() : obj.name.toUpperCase()) + "\n");
                    }
                    lastObjectName = obj.name;
                    Vector2f position = obj.getComponent(Transform.class).position;
                    position.x = (int) position.x;
                    position.y = (int) (position.y * -1);
                    writerScene.write(prefabNameList.indexOf(obj.name) + "\t" + (int) Math.ceil(position.x) + " " + (int) Math.ceil(position.y) + "\n");
                }
            }
            //endregion

            writerScene.write("\n#PORTAL\n");
            for (GameObject obj : this.gameObjects) {
                if (obj.doSerialization() && obj.name.equals("Portal")) {
                    Vector2f position = obj.getComponent(Transform.class).position;
                    position.y = position.y * -1;
                    writerScene.write(portalObjectId + "\t" + (int) Math.ceil(position.x) + " " + (int) Math.ceil(position.y) + " ");
                    int nextSceneId = SceneList.scenes.indexOf(obj.getComponent(PortalComponent.class).nextScene);
                    writerScene.write(nextSceneId + "\n");
                }
            }

            writerPrefabList.close();
            writerScene.close();
            writerProject.close();
            Debug.Log("Convert scene '" + SceneUtils.CURRENT_SCENE + "' successfully", LogType.Success);
        } catch (IOException e) {
            e.printStackTrace();
            if (isShowMessage) {
                Debug.Log("Convert scene '" + SceneUtils.CURRENT_SCENE + "' FAIL", LogType.Error);
            }
        }
    }

    public void load() {
        if (SceneUtils.CURRENT_SCENE.isEmpty()) return;
        File folder = new File("data\\" + SceneUtils.CURRENT_SCENE);
        if (!folder.exists()) {
            JOptionPane.showMessageDialog(null, "Cannot find the previous scene (" + SceneUtils.CURRENT_SCENE + ")",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            Window.get().changeCurrentScene("", false, false);
            OpenSceneWindow.open(false);
            return;
        }

        String level_path = "data\\" + SceneUtils.CURRENT_SCENE + "\\" + LEVEL_PATH;
        String prefab_path = "data\\" + PREFAB_PATH;
        String portal_path = "data\\portal.txt";

        GameObject.PrefabLists.clear();

        //region Load Game object
        int maxGoId = -1;
        int maxCompId = -1;
        GameObject.setCurrentMaxUid(0);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        String inFile = "";

        try {
            inFile = new String(Files.readAllBytes(Paths.get(level_path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
            for (int i = 0; i < objs.length; i++) {
                GameObject go = objs[i];

                addGameObjectToScene(go);

                for (Component c : go.getAllComponents()) {
                    if (c.getUid() > maxCompId) {
                        maxCompId = c.getUid();
                    }
                }

                if (go.getUid() > maxGoId) {
                    maxGoId = go.getUid();
                }
            }

            maxGoId++;
            maxCompId++;

            GameObject.init(maxGoId);
            Component.init(maxCompId);
        }

        for (GameObject g : this.gameObjects) {
            g.refreshTexture();
        }
        //endregion

        //region Load Portal
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new PrefabDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        inFile = "";

        try {
            inFile = new String(Files.readAllBytes(Paths.get(portal_path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
            for (int i = 0; i < objs.length; i++) {
                GameObject prefab = objs[i];

                GameObject.PrefabLists.add(prefab);

                for (Component c : prefab.getAllComponents()) {
                    if (c.getUid() > maxCompId) {
                        maxCompId = c.getUid();
                    }
                }

                prefab.refreshTexture();

                if (prefab.getUid() > maxGoId) {
                    maxGoId = prefab.getUid();
                }
            }

            maxGoId++;
            maxCompId++;

            GameObject.init(maxGoId);
            Component.init(maxCompId);
        }
        //endregion

        //region Load Prefabs
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new PrefabDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        inFile = "";

        try {
            inFile = new String(Files.readAllBytes(Paths.get(prefab_path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
            for (int i = 0; i < objs.length; i++) {
                GameObject prefab = objs[i];

                GameObject.PrefabLists.add(prefab);

                for (Component c : prefab.getAllComponents()) {
                    if (c.getUid() > maxCompId) {
                        maxCompId = c.getUid();
                    }
                }

                prefab.refreshTexture();

                if (prefab.getUid() > maxGoId) {
                    maxGoId = prefab.getUid();
                }
            }

            maxGoId++;
            maxCompId++;

            GameObject.init(maxGoId);
            Component.init(maxCompId);
        }
        //endregion
    }
    //endregion
}
