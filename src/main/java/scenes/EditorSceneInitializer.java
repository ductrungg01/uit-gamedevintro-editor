package scenes;

import components.*;
import editor.GridLines;
import editor.gizmo.GizmoSystem;
import system.*;
import util.AssetPool;
import util.FileUtils;


public class EditorSceneInitializer extends SceneInitializer {
    private GameObject levelEditorStuff;

    public EditorSceneInitializer() {

    }

    @Override
    public void init(Scene scene) {
        Spritesheet gizmos = FileUtils.getGizmosSprSheet();

        levelEditorStuff = new GameObject("LevelEditorSceneInitializer");
        levelEditorStuff.isSpecialObject = true;
        levelEditorStuff.transform.zIndex = -20;
        levelEditorStuff.addComponent(new GridLines());
        levelEditorStuff.addComponent(new EditorCamera(scene.camera()));
        levelEditorStuff.addComponent(new GizmoSystem(gizmos));
        scene.addGameObjectToScene(levelEditorStuff);
    }

    @Override
    public void loadResources(Scene scene) {
        AssetPool.getShader("system-assets/shaders/default.glsl");
    }

    @Override
    public void imgui() {
    }
}
