package util;

import components.Sprite;
import editor.windows.ImportProjectDataWindow;
import editor.windows.ScenesWindow;
import org.joml.Vector2f;
import renderer.Texture;
import scenes.EditorSceneInitializer;
import system.GameObject;
import system.Prefab;
import system.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectUtils {
    // region Fields
    private static class GameConstants {
        public static final int GAME_FILE_SECTION_UNKNOWN = -1;
        public static final int GAME_FILE_SECTION_SETTINGS = 1;
        public static final int GAME_FILE_SECTION_SCENES = 2;
        public static final int GAME_FILE_SECTION_TEXTURES = 3;
    }

    private static CustomFileUtils projectFile = new CustomFileUtils("");
    public static Vector2f screenSize = new Vector2f();

    public static final int NULL_SCENE_ID = -1000000000;

    private static int startScene = NULL_SCENE_ID;
    private static int currentScene = NULL_SCENE_ID;

    public static Map<Integer, String> scenes = new HashMap<>();
    public static Map<Integer, String> textures = new HashMap<>();
    public static Map<Integer, Sprite> sprites = new HashMap<>();
    // endregion

    // region Properties
    public static CustomFileUtils getProjectFile() { return projectFile; }

    public static void setProjectFile(CustomFileUtils newProjectFile) {
        projectFile = newProjectFile;
    }

    public static void setProjectFile(String newProjectFile) {
        projectFile = new CustomFileUtils(newProjectFile);
    }

    public static CustomFileUtils getScenePath(int index){
        if (ProjectUtils.scenes.containsKey(index)){
            String path = ProjectUtils.scenes.get(index);
            return new CustomFileUtils(path);
        } else {
            System.out.println("[ERROR], Cannot find the scene index = " + index);
            return null;
        }
    }
    public static String getSceneName(int index){
        return FileUtils.getFileNameWithoutExtension(getScenePath(index).fileName());
    }
    public static int getSceneId(String path){
        for (Map.Entry<Integer, String> entry : scenes.entrySet()) {
            if (entry.getValue().contains(path)) {
                return entry.getKey();
            }
        }
        return NULL_SCENE_ID;
    }
    public static List<String> convertToList(Map<Integer, String> mp){
        List<String> convertedList = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : mp.entrySet()) {
            convertedList.add(entry.getValue());
        }

        return convertedList;
    }
    public static CustomFileUtils getCurrentScenePath(){
        if (currentScene == NULL_SCENE_ID) return null;

        return getScenePath(currentScene);
    }
    public static void changeCurrentSceneId(String newSceneName){
        currentScene = getSceneId(newSceneName);
    }
    public static Texture getTexture(int id){
        if (!textures.containsKey(id)) {
            return null;
        }

        Texture texture = new Texture();
        texture.init(textures.get(id));
        AssetPool.getTexture(texture.getFilePath());
        return texture;
    }
    public static Sprite getSprite(int id){
        if (!sprites.containsKey(id)) {
            return null;
        }

        return sprites.get(id);
    }
    public static int getSpriteId(Sprite sprite){
        for (Map.Entry<Integer, Sprite> entry : sprites.entrySet()) {
            if (entry.getValue() == sprite) return entry.getKey();
        }

        return -100000;
    }
    public static void addSpite(int id, Vector2f left_top_pos, Vector2f right_bottom_pos, int textureId){
        if (sprites.containsKey(id)){
            System.out.println("[ERROR] Sprite has ID = " + id + " is exited already");
            return;
        }

        Texture texture = getTexture(textureId);
        if (texture == null) {
            System.out.println("[ERROR] Cannot find texture have ID = " + textureId );
            return;
        }

        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        Sprite sprite = new Sprite(texture);
        Vector2f[] texCoords = {
                new Vector2f(right_bottom_pos.x / textureWidth, left_top_pos.y / textureHeight),
                new Vector2f(right_bottom_pos.x / textureWidth, right_bottom_pos.y / textureHeight),
                new Vector2f(left_top_pos.x / textureWidth, right_bottom_pos.y / textureHeight),
                new Vector2f(left_top_pos.x / textureWidth, left_top_pos.y / textureHeight),
        };
        sprite.setTexCoords(texCoords);

        sprites.put(id, sprite);
    }
    // endregion

    // region Methods
    private static void resetProjectValue(){
        startScene = NULL_SCENE_ID;
        screenSize = new Vector2f();
        scenes = new HashMap<>();
        textures = new HashMap<>();
        sprites = new HashMap<>();
        Prefab.PrefabLists.clear();
    }

    public static void removeFileInExportFolder() throws IOException {
        String directoryPath = "./export/";
        Path directory = Paths.get(directoryPath);
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(directoryPath + " | Cannot find this directory");
        }
        Files.list(directory)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void loadProject() {
        resetProjectValue();

        parseProjectFile();

        System.out.println("LOAD PROJECT DONE!!!");

        ImportProjectDataWindow.close();

        Window.changeScene(new EditorSceneInitializer());

        Window.get().changeCurrentScene(getSceneName(startScene));
    }

    //region Parse project file
    private static void parseProjectFile(){
        int section = GameConstants.GAME_FILE_SECTION_UNKNOWN;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(projectFile.fileAbsolutePath()));

            String line;

            while ((line = reader.readLine()) != null) {
                line.trim();

                if (line.startsWith("#")) continue;

                if (line.equals("[SETTINGS]")) {section = GameConstants.GAME_FILE_SECTION_SETTINGS; continue;}
                if (line.equals("[SCENES]")) {section = GameConstants.GAME_FILE_SECTION_SCENES; continue;}
                if (line.equals("[TEXTURES]")) {section = GameConstants.GAME_FILE_SECTION_TEXTURES; continue;}
                if (line.startsWith("[")) {section = GameConstants.GAME_FILE_SECTION_UNKNOWN; continue;}

                switch (section){
                    case GameConstants.GAME_FILE_SECTION_UNKNOWN -> System.out.println("UNKNOWN SECTION: ");
                    case GameConstants.GAME_FILE_SECTION_SETTINGS -> parseSection_SETTINGS(line);
                    case GameConstants.GAME_FILE_SECTION_SCENES -> parseSection_SCENES(line);
                    case GameConstants.GAME_FILE_SECTION_TEXTURES -> parseSection_TEXTURES(line);
                }
            }

            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void parseSection_SETTINGS(String line) {
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) return;

        if (tokens[0].equals("start")) {
            startScene = Integer.parseInt(tokens[1]);
            currentScene = startScene;
        } else if (tokens[0].equals("width")) {
            screenSize.x = Integer.parseInt(tokens[1]);
        } else if (tokens[0].equals("height")) {
            screenSize.y = Integer.parseInt(tokens[1]);
        }
    }

    private static void parseSection_SCENES(String line) {
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) return;

        int index = Integer.parseInt(tokens[0]);
        String path = projectFile.fileParentDirectory() + tokens[1];

        scenes.put(index, path);
    }

    private static void parseSection_TEXTURES(String line){
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) return;

        int index = Integer.parseInt(tokens[0]);
        String path = projectFile.fileParentDirectory() + tokens[1];

        textures.put(index, path);
    }
    // endregion

    // endregion
}
