package util;

import components.Sprite;
import editor.Debug;
import editor.MessageBox;
import renderer.Texture;
import system.Spritesheet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class FileUtils {

    public enum ICON_NAME {
        FOLDER,
        LEFT_ARROW,
        RIGHT_ARROW,
        JAVA,
        FILE,
        SOUND,
        GAME_OBJECT,
        REMOVE,
        ADD,
        DEMO,
        RESTART,
        PROJECT,
        ENABLE_CONSTRAINED_PROPORTIONS,
        DISABLE_CONSTRAINED_PROPORTIONS,
    }


    public final static Map<ICON_NAME, String> icons = new HashMap<>() {
        {
            put(ICON_NAME.FOLDER, "system-assets/images/folder-icon.png");
            put(ICON_NAME.LEFT_ARROW, "system-assets/images/left-arrow-icon.png");
            put(ICON_NAME.RIGHT_ARROW, "system-assets/images/right-arrow-icon.png");
            put(ICON_NAME.JAVA, "system-assets/images/java-icon.png");
            put(ICON_NAME.FILE, "system-assets/images/file-icon.png");
            put(ICON_NAME.SOUND, "system-assets/images/sound-icon.png");
            put(ICON_NAME.GAME_OBJECT, "system-assets/images/gameobject-icon.png");
            put(ICON_NAME.REMOVE, "system-assets/images/remove-icon.png");
            put(ICON_NAME.ADD, "system-assets/images/add-icon.png");
            put(ICON_NAME.DEMO, "system-assets/images/demo-icon.png");
            put(ICON_NAME.RESTART, "system-assets/images/restart-icon.png");
            put(ICON_NAME.PROJECT, "system-assets/images/project-icon.png");
            put(ICON_NAME.ENABLE_CONSTRAINED_PROPORTIONS, "system-assets/images/EnableConstrainedProportions-icon.png");
            put(ICON_NAME.DISABLE_CONSTRAINED_PROPORTIONS, "system-assets/images/DisableConstrainedProportions-icon.png");
        }
    };
    final static String defaultSprite = "system-assets/images/Default Sprite.png";
    private static List<String> imageExtensions = List.of("jpg", "jpeg", "png");

    public static boolean isImageFile(File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        //, "gif", "bmp", "tiff", "webp"
        for (String ext : imageExtensions) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static void copyFile(File srcFile, File desFile) {
        try {
            Path desPath = Paths.get(desFile.getPath());
            Files.copy(srcFile.toPath(), desPath);
            Debug.Log("Copy file " + srcFile.getName() + " to " + desPath);
            if (srcFile.isDirectory()) {
                File[] listOfFiles = srcFile.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    copyFile(listOfFiles[i], new File(desFile.getPath() + "/" + listOfFiles[i].getName()));
                }
            }
        } catch (Exception e) {
            MessageBox.setContext(true, MessageBox.TypeOfMsb.ERROR, "File already exist");
            Debug.Log("Failed to copy file: " + e.getMessage());
        }

    }

    public static String getShorterName(String fileName) {
        String name = getFileNameWithoutExtension(fileName);
        String ext = getFileExtension(fileName);

        final int MAX_LENGTH_ALLOW = 13;
        if (name.length() > MAX_LENGTH_ALLOW) {
            name = name.substring(0, MAX_LENGTH_ALLOW) + "..";
        }

        return name + "." + ext;
    }

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(0, dotIndex);
        } else {
            return filename;
        }
    }

    public static Sprite getIcon(ICON_NAME iconName) {
        Sprite spr = new Sprite();
        spr.setTexture(AssetPool.getTexture(icons.get(iconName)));
        return spr;
    }

    public static Spritesheet getGizmosSprSheet() {
        Texture texture = AssetPool.getTexture("system-assets/images/gizmos.png");
        Spritesheet spr = new Spritesheet(texture, 24, 48, 3, 0, 0);
        return spr;
    }

    public static Sprite getDefaultSprite() {
        return new Sprite(defaultSprite);
    }
}
