package components;

import org.joml.Vector2f;
import org.w3c.dom.Text;
import renderer.Texture;
import util.AssetPool;

public class Sprite implements INonAddableComponent {
    //region Fields
    private float width, height;

    private Texture texture = null;
    private Vector2f[] texCoords = {
            new Vector2f(1, 0),
            new Vector2f(1, 1),
            new Vector2f(0, 1),
            new Vector2f(0, 0),
    };
    //endregion

    //region Constructors
    public Sprite() {
    }

    public Sprite(Texture texture) {
        setTexture(texture);
        calcWidthAndHeight();
    }

    public Sprite(String TexturePath) {
        setTexture(AssetPool.getTexture(TexturePath));
        calcWidthAndHeight();
    }

    public Sprite copy(){
        Sprite newSpr = new Sprite();
        newSpr.texture = this.texture;
        newSpr.texCoords = this.getTexCoords();
        newSpr.width = this.width;
        newSpr.height = this.height;

        return newSpr;
    }

    public boolean equal(Sprite other){
        return (this.texture == other.texture)
                && (this.texCoords == other.texCoords)
                && (this.width == other.width)
                && (this.height == other.height);
    }
    //endregion

    //region Properties
    public Texture getTexture() {
        return this.texture;
    }

    public Vector2f[] getTexCoords() {
        return this.texCoords;
    }

    public void setTexture(Texture tex) {
        this.texture = tex;
    }

    public void setTexCoords(Vector2f[] texCoords) {
        this.texCoords = texCoords;
        float dx = texCoords[0].x - texCoords[3].x;
        float dy = texCoords[1].y - texCoords[0].y;

        float width = this.texture.getWidth() * dx;
        float height = this.texture.getHeight() * dy;

        setWidth(width);
        setHeight(height);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getTexId() {
        return texture == null ? -1 : texture.getId();
    }

    public void calcWidthAndHeight() {
        float w = this.texture.getWidth();
        float h = this.texture.getHeight();
        setWidth(w);
        setHeight(h);
    }
    //endregion
}
