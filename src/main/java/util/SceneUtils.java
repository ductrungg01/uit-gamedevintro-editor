package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SceneUtils {
    public static String CURRENT_SCENE = "";
    private static final String ROOT_FOLDER = "data";
    public static List<String> getAllScene(){
        List<String> scenes = new ArrayList<>();

        File folder = new File(ROOT_FOLDER);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                scenes.add(listOfFiles[i].getName());
            }
        }

        return scenes;
    }

}
