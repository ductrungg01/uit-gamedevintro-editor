package components;

import editor.ReferenceType;
import editor.NiceImGui;
import imgui.ImGui;
import system.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;
import renderer.Texture;
import util.AssetPool;
import util.FileUtils;

import java.text.DecimalFormat;

import static editor.uihelper.NiceShortCall.COLOR_Green;

public class SpriteRenderer extends Component implements INonAddableComponent {
    //region Fields
    private Vector4f color = new Vector4f(1, 1, 1, 1);
    private Sprite sprite = new Sprite();

    private transient Transform lastTransform;
    private transient boolean isDirty = true;
    //endregion

    //region Override methods

    /**
     * Start is called before the first frame update
     */
    @Override
    public void start() {
        this.lastTransform = gameObject.transform.copy();
    }

    /**
     * Update is called once per frame
     *
     * @param dt : The interval in seconds from the last frame to the current one
     */
    @Override
    public void update(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            isDirty = true;
        }
    }

    @Override
    public void editorUpdate(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            isDirty = true;
        }
    }

    @Override
    public void imgui() {
        if (this.gameObject.isPrefab()) {
            Sprite tmp = (Sprite) NiceImGui.ReferenceButtonGO(this.gameObject,
                    "Sprite",
                    ReferenceType.SPRITE,
                    sprite,
                    new float[2],
                    "Sprite of SpriteRenderer " + this.gameObject.hashCode());

            if (!tmp.equal(sprite)) {
                setSprite(tmp);
            }
        } else {
            ImGui.textColored(COLOR_Green.x, COLOR_Green.y, COLOR_Green.z, COLOR_Green.w,
                    FileUtils.getShorterName(sprite.getTexture().getFilePath()));
            //region coord
            if (sprite != null) {
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
                ImGui.text("Top-Left coord: (" + df.format(topLeftCoord.x) + " : " + df.format(topLeftCoord.y) + ")");
                ImGui.text("Bottom-Right coord: (" + df.format(bottomRightCoord.x) + " : " + df.format(bottomRightCoord.y) + ")");
            }
        }

        NiceImGui.showImage(this.sprite, new Vector2f(100, 100));
    }

    public void convertToScale() {
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

        this.gameObject.transform.scale.x = Float.parseFloat(df.format(bottomRightCoord.x - topLeftCoord.x));
        this.gameObject.transform.scale.y = Float.parseFloat(df.format(bottomRightCoord.y - topLeftCoord.y));
    }

    //endregion

    //region Properties
    public void setDirty() {
        this.isDirty = true;
    }

    public Vector4f getColor() {
        return new Vector4f(this.color);
    }

    public void setColor(Vector4f color) {
        if (!this.color.equals(color)) {
            this.isDirty = true;
            this.color.set(color);
        }
    }

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public void setTexture(Texture texture) {
        this.sprite.setTexture(texture);
    }

    public Vector2f[] getTexCoords() {
        return sprite.getTexCoords();
    }

    public Sprite getSprite() {
        return this.sprite;
    }

    public void setSprite(Sprite sprite) {
        if (sprite.getTexId() == -1) {
            sprite.setTexture(AssetPool.getTexture(sprite.getTexture().getFilePath()));
        }

        this.sprite = sprite;
        this.isDirty = true;
        convertToScale();
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void setClean() {
        this.isDirty = false;
    }
    //endregion
}
