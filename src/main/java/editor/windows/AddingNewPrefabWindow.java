package editor.windows;

import components.Sprite;
import components.SpriteRenderer;
import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import system.GameObject;
import system.Window;
import util.FileUtils;
import util.JMath;

import javax.swing.plaf.SplitPaneUI;
import java.text.DecimalFormat;
import java.util.Vector;

import static editor.uihelper.NiceShortCall.*;
import static editor.uihelper.NiceShortCall.COLOR_Red;
import static org.joml.Math.clamp;

public class AddingNewPrefabWindow {
    private static AddingNewPrefabWindow instance = null;
    private boolean isOpen = false;
    private Vector2f topLeftCoord = new Vector2f();
    //endregion
    private Vector2f bottomRightCoord = new Vector2f();
    private Sprite sprite;
    //region Singleton
    private AddingNewPrefabWindow() {
    }

    public static AddingNewPrefabWindow getInstance() {
        if (instance == null) {
            instance = new AddingNewPrefabWindow();
        }

        return instance;
    }

    public void open(Sprite sprite) {
        this.isOpen = true;
        this.sprite = sprite;
        this.topLeftCoord = new Vector2f(0, 0);
        this.bottomRightCoord = new Vector2f(sprite.getWidth(), sprite.getHeight());
    }

    public void imgui() {
        if (!this.isOpen) return;

        String popupId = "Adding new Prefab";

        ImGui.openPopup(popupId);

        float popupWidth = Window.getWidth() * 0.9f;
        float popupHeight = Window.getHeight() * 0.9f;
        ImGui.setNextWindowSize(popupWidth, popupHeight);

        float popupPosX = (float) Window.getWidth() / 2 - popupWidth / 2;
        float popupPosY = (float) Window.getHeight() / 2 - popupHeight / 2;
        ImGui.setNextWindowPos(popupPosX, popupPosY, ImGuiCond.Always);

        if (ImGui.beginPopupModal(popupId, new ImBoolean(this.isOpen), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize)) {
            ImGui.columns(3);
            final float SETTING_COLUMN_WIDTH = 450f;
            ImGui.setColumnWidth(0, SETTING_COLUMN_WIDTH);

            //region COORD
            ImGui.text("ENTER THE PREFAB INFORMATION");
            ImGui.newLine();

            ImGui.beginChild("##GameObjectInforAdding", SETTING_COLUMN_WIDTH, 500);
            ImGui.text("Name: ");
            String goName = FileUtils.getFileName(sprite.getTexture().getFilePath());
            ImGui.sameLine();
            ImGui.text(goName);
            NiceImGui.drawVec2Control("Top-Left coord:", this.topLeftCoord, 0, 180, "TopleftCoord");
            NiceImGui.drawVec2Control("Bottom-Right coord:", this.bottomRightCoord, 0, 180, "BottomrightCoord");

            float spriteWidth = sprite.getTexture().getWidth();
            float spriteHeight = sprite.getTexture().getHeight();
            this.topLeftCoord.x = clamp(0, spriteWidth, this.topLeftCoord.x);
            this.topLeftCoord.y = clamp(0, spriteHeight, this.topLeftCoord.y);
            this.bottomRightCoord.x = clamp(topLeftCoord.x, spriteWidth, this.bottomRightCoord.x);
            this.bottomRightCoord.y = clamp(topLeftCoord.y, spriteHeight, this.bottomRightCoord.y);

            Vector2f[] texCoords = new Vector2f[]{
                    new Vector2f(bottomRightCoord.x / spriteWidth, topLeftCoord.y / spriteHeight),
                    new Vector2f(bottomRightCoord.x / spriteWidth, bottomRightCoord.y / spriteHeight),
                    new Vector2f(topLeftCoord.x / spriteWidth, bottomRightCoord.y / spriteHeight),
                    new Vector2f(topLeftCoord.x / spriteWidth, topLeftCoord.y / spriteHeight)
            };

            ImGui.newLine();

            if (NiceImGui.drawButton("NEW PREFAB",
                    new ButtonColor(COLOR_DarkBlue, COLOR_Blue, COLOR_Blue),
                    new Vector2f(SETTING_COLUMN_WIDTH, 50f))) {
                Sprite newSpr = new Sprite(sprite.getTexture());
                newSpr.setTexCoords(texCoords);
                GameObject newGo = new GameObject(goName, newSpr);
                newGo.getComponent(SpriteRenderer.class).convertToScale();
                newGo.setAsPrefab();
                close();
            }

            if (NiceImGui.drawButton("CANCEL",
                    new ButtonColor(COLOR_DarkRed, COLOR_Red, COLOR_Red),
                    new Vector2f(SETTING_COLUMN_WIDTH, 30f))) {
                close();
            }

            ImGui.endChild();
            //endregion

            ImGui.nextColumn();

            final float SPRITE_PREVIEW_MINIMUM_SIZE_X = 500f;
            final float SPRITE_PREVIEW_MINIMUM_SIZE_Y = 500f;
            ImGui.setColumnWidth(1, 550);

            //region Original Sprite
            ImGui.textColored(COLOR_Green.x, COLOR_Green.y, COLOR_Green.z, COLOR_Green.w, "Original Sprite");
            float imageSizeX = sprite.getTexture().getWidth();
            float imageSizeY = sprite.getTexture().getHeight();

            float offset = Math.min(SPRITE_PREVIEW_MINIMUM_SIZE_X / imageSizeX,
                    SPRITE_PREVIEW_MINIMUM_SIZE_Y / imageSizeY);
            float sizeToShowImageX = imageSizeX * offset;
            float sizeToShowImageY = imageSizeY * offset;

            float cursorPosX1 = ImGui.getCursorScreenPosX();
            float cursorPosY1 = ImGui.getCursorScreenPosY();
            ImGui.image(sprite.getTexId(), sizeToShowImageX, sizeToShowImageY);
            float cursorPosX2 = ImGui.getCursorScreenPosX();
            float cursorPosY2 = ImGui.getCursorScreenPosY();

            drawToEasyPreview(new Vector2f(cursorPosX1, cursorPosY1), new Vector2f(sizeToShowImageX, sizeToShowImageY));

            ImGui.setCursorScreenPos(cursorPosX2, cursorPosY2);

            //endregion

            ImGui.nextColumn();

            //region SPRITE Preview
            ImGui.textColored(COLOR_Green.x, COLOR_Green.y, COLOR_Green.z, COLOR_Green.w, "Sprite selected preview");
            imageSizeX = this.bottomRightCoord.x - this.topLeftCoord.x;
            imageSizeY = this.bottomRightCoord.y - this.topLeftCoord.y;

            offset = Math.min(SPRITE_PREVIEW_MINIMUM_SIZE_X / imageSizeX,
                    SPRITE_PREVIEW_MINIMUM_SIZE_Y / imageSizeY);
            sizeToShowImageX = imageSizeX * offset;
            sizeToShowImageY = imageSizeY * offset;

            ImGui.image(sprite.getTexId(), sizeToShowImageX, sizeToShowImageY,
                    texCoords[3].x, texCoords[3].y, texCoords[1].x, texCoords[1].y);
            //endregion

            ImGui.columns(1);
            ImGui.endPopup();
        }
    }

    private void drawToEasyPreview(Vector2f TLCursorPos, Vector2f sizeToShowImage) {
        float cursorPosX = ImGui.getCursorScreenPosX();
        float cursorPosY = ImGui.getCursorScreenPosY();

        Vector2f imgBRPos = new Vector2f(cursorPosX + sizeToShowImage.x, cursorPosY);

        ImGui.setCursorScreenPos(TLCursorPos.x, TLCursorPos.y);

        float offsetX = (imgBRPos.x - TLCursorPos.x);
        float offsetY = (imgBRPos.y - TLCursorPos.y);

        final float IMAGE_SIZE_X = this.sprite.getTexture().getWidth();
        final float IMAGE_SIZE_Y = this.sprite.getTexture().getHeight();

        Vector2f topLeftPosToDraw = new Vector2f(
                (this.topLeftCoord.x / IMAGE_SIZE_X) * offsetX + TLCursorPos.x,
                (this.topLeftCoord.y / IMAGE_SIZE_Y) * offsetY + TLCursorPos.y
        );

        Vector2f bottomRightPosToDraw = new Vector2f(
                (this.bottomRightCoord.x / IMAGE_SIZE_X) * offsetX + TLCursorPos.x,
                (this.bottomRightCoord.y / IMAGE_SIZE_Y) * offsetY + TLCursorPos.y
        );

        draw4lines(topLeftPosToDraw, bottomRightPosToDraw);
    }

    private void draw4lines(Vector2f topLeftPos, Vector2f bottomRightPos) {
        ImDrawList drawList = ImGui.getWindowDrawList();

        int color = ImColor.intToColor(255, 0, 0, 255);
        float lineSize = 1f;

        drawList.addLine(topLeftPos.x, topLeftPos.y, bottomRightPos.x, topLeftPos.y, color, lineSize);
        drawList.addLine(bottomRightPos.x, topLeftPos.y, bottomRightPos.x, bottomRightPos.y, color, lineSize);
        drawList.addLine(bottomRightPos.x, bottomRightPos.y, topLeftPos.x, bottomRightPos.y, color, lineSize);
        drawList.addLine(topLeftPos.x, bottomRightPos.y, topLeftPos.x, topLeftPos.y, color, lineSize);
    }

    private void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }
}
