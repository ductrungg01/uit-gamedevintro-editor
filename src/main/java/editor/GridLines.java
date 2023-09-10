package editor;

import components.Component;
import components.INonAddableComponent;
import system.Camera;
import system.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.GridLineDraw;
import util.Settings;

import java.awt.*;

public class GridLines extends Component implements INonAddableComponent {
    //region Override methods
    @Override
    public void editorUpdate(float dt) {
        Camera camera = Window.getScene().camera();

        Vector2f cameraPos = camera.position;
        Vector2f projectionSize = camera.getProjectionSize();

        Vector2f screenSize = new Vector2f(Camera.screenSize);
        float startX = 0;
        float endX = screenSize.x;
        float startY = 0;
        float endY = -screenSize.y;

        Vector3f color = new Vector3f(0, 1, 0);
        GridLineDraw.addLine2D(new Vector2f(startX,  startY), new Vector2f(startX,  endY), color);
        GridLineDraw.addLine2D(new Vector2f(endX,  startY), new Vector2f(endX,  endY), color);
        GridLineDraw.addLine2D(new Vector2f( startX, startY), new Vector2f( endX, startY), color);
        GridLineDraw.addLine2D(new Vector2f( startX,  endY), new Vector2f( endX, endY), color);
    }
    //endregion
}
