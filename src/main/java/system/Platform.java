package system;

import components.PlatformInfor;
import components.Sprite;
import org.joml.Vector2f;
import util.ProjectUtils;

import java.util.ArrayList;
import java.util.List;

public class Platform {
    protected static int ID_COUNTER = 0;

    int id, length, spr_begin_id, spr_middle_id, spr_end_id;
    float x, y, cell_width, cell_height;

    public Platform(float x, float y, float cell_width, float cell_height, int length, int spr_begin_id, int spr_middle_id, int spr_end_id) {
        id = ID_COUNTER++;

        this.x = x;
        this.y = y;
        this.cell_width = cell_width;
        this.cell_height = cell_height;
        this.length = length;
        this.spr_begin_id = spr_begin_id;
        this.spr_middle_id = spr_middle_id;
        this.spr_end_id = spr_end_id;

        createBeginObject();
        createMiddleObject();
        createEndObject();
    }

    private void createBeginObject(){
        Sprite spr = ProjectUtils.getSprite(spr_begin_id);
        GameObject go = new GameObject("Platform " + id + " (begin)", spr);
        go.isPlatform = true;
        go.platformId = id;
        go.transform.position = new Vector2f(x, y);
        go.addComponent(new PlatformInfor(id, cell_width, cell_height, length, spr_begin_id, spr_middle_id, spr_end_id));

        List<GameObject> plfs = new ArrayList<>();
        plfs.add(go);

        GameObject.platforms.put(id, plfs);

        Window.getScene().addGameObjectToScene(go);
    }

    private void createMiddleObject(){
        Sprite spr = ProjectUtils.getSprite(spr_middle_id);

        for (int i = 1; i <= length - 2; i++){
            GameObject go = new GameObject("Platform " + id + " (middle) - " + i, spr);
            go.isPlatform = true;
            go.platformId = id;
            go.transform.position = new Vector2f(x + cell_width * i, y);
            go.addComponent(new PlatformInfor(id, cell_width, cell_height, length, spr_begin_id, spr_middle_id, spr_end_id));

            GameObject.platforms.get(id).add(go);

            Window.getScene().addGameObjectToScene(go);
        }
    }

    private void createEndObject(){
        Sprite spr = ProjectUtils.getSprite(spr_end_id);
        GameObject go = new GameObject("Platform " + id + " (end)", spr);
        go.isPlatform = true;
        go.platformId = id;
        go.transform.position = new Vector2f(x + cell_width * (length - 1), y);
        go.addComponent(new PlatformInfor(id, cell_width, cell_height, length, spr_begin_id, spr_middle_id, spr_end_id));

        GameObject.platforms.get(id).add(go);

        Window.getScene().addGameObjectToScene(go);
    }
}
