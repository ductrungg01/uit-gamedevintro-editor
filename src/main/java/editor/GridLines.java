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
        Vector3f color = new Vector3f(0.9f, 0.9f, 0.9f);

        //region draw grid line
        float firstX = ((int) (cameraPos.x / Settings.GRID_WIDTH)) * Settings.GRID_WIDTH;
        float firstY = ((int) (cameraPos.y / Settings.GRID_HEIGHT)) * Settings.GRID_HEIGHT;

        int numVtLines = (int) (projectionSize.x * camera.getZoom() / Settings.GRID_WIDTH) + 2;
        int numHzLines = (int) (projectionSize.y * camera.getZoom() / Settings.GRID_HEIGHT) + 2;

        float width = (int) (projectionSize.x * camera.getZoom()) + Settings.GRID_WIDTH * 5;
        float height = (int) (projectionSize.y * camera.getZoom()) + Settings.GRID_HEIGHT * 5;

        int maxLines = Math.max(numHzLines, numVtLines);
        for (int i = 0; i < maxLines; i++) {
            float x = firstX + (Settings.GRID_WIDTH * i);
            float y = firstY + (Settings.GRID_HEIGHT * i);

            if (i < numVtLines) {
                GridLineDraw.addLine2D(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color);
            }

            if (i < numHzLines) {
                GridLineDraw.addLine2D(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color);
            }
        }
        // endregion

        //region draw border
        float startX = 0;
        float endX = screenSize.x;
        float startY = 0;
        float endY = -screenSize.y;
        color = new Vector3f(0, 1, 0);

        GridLineDraw.addLine2D(new Vector2f(startX,  startY), new Vector2f(startX,  endY), color);
        GridLineDraw.addLine2D(new Vector2f(endX,  startY), new Vector2f(endX,  endY), color);
        GridLineDraw.addLine2D(new Vector2f( startX, startY), new Vector2f( endX, startY), color);
        GridLineDraw.addLine2D(new Vector2f( startX,  endY), new Vector2f( endX, endY), color);
        //endregion
    }
    //endregion
}
