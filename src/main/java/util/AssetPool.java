package util;

import system.Spritesheet;
import renderer.Shader;
import renderer.Texture;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class AssetPool {
    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();

    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        if (AssetPool.shaders.containsKey(file.getAbsolutePath())) {
            return AssetPool.shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    public static Texture getTexture(String resourceName) {
        File file = new File(resourceName);
        String filePath = file.getPath().replace("\\", "/");
        if (AssetPool.textures.containsKey(filePath)) {
            return AssetPool.textures.get(filePath);
        } else {
            Texture texture = new Texture();
            texture.init(resourceName);
            AssetPool.textures.put(filePath, texture);
            return texture;
        }
    }
}
