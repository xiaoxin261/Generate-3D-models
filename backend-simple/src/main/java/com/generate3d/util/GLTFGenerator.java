package com.generate3d.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * GLTF格式生成工具类
 */
@Slf4j
public class GLTFGenerator {
    
    /**
     * 生成简单立方体的GLTF数据
     */
    public static String generateCube(double size, String color, String description) {
        JSONObject gltf = new JSONObject();
        
        // Asset信息
        JSONObject asset = new JSONObject();
        asset.put("version", "2.0");
        asset.put("generator", "Generate3D-Backend");
        gltf.put("asset", asset);
        
        // Scene
        gltf.put("scene", 0);
        JSONArray scenes = new JSONArray();
        JSONObject scene = new JSONObject();
        scene.put("nodes", new int[]{0});
        scenes.add(scene);
        gltf.put("scenes", scenes);
        
        // Nodes
        JSONArray nodes = new JSONArray();
        JSONObject node = new JSONObject();
        node.put("mesh", 0);
        nodes.add(node);
        gltf.put("nodes", nodes);
        
        // Meshes
        JSONArray meshes = new JSONArray();
        JSONObject mesh = new JSONObject();
        JSONArray primitives = new JSONArray();
        JSONObject primitive = new JSONObject();
        
        JSONObject attributes = new JSONObject();
        attributes.put("POSITION", 0);
        attributes.put("NORMAL", 1);
        attributes.put("TEXCOORD_0", 2);
        primitive.put("attributes", attributes);
        primitive.put("indices", 3);
        primitive.put("material", 0);
        
        primitives.add(primitive);
        mesh.put("primitives", primitives);
        meshes.add(mesh);
        gltf.put("meshes", meshes);
        
        // Materials
        JSONArray materials = new JSONArray();
        JSONObject material = new JSONObject();
        material.put("name", "CubeMaterial");
        
        JSONObject pbrMetallicRoughness = new JSONObject();
        pbrMetallicRoughness.put("baseColorFactor", hexToRgba(color));
        pbrMetallicRoughness.put("metallicFactor", 0.0);
        pbrMetallicRoughness.put("roughnessFactor", 0.5);
        material.put("pbrMetallicRoughness", pbrMetallicRoughness);
        
        materials.add(material);
        gltf.put("materials", materials);
        
        // Accessors
        JSONArray accessors = new JSONArray();
        
        // Position accessor
        JSONObject positionAccessor = new JSONObject();
        positionAccessor.put("bufferView", 0);
        positionAccessor.put("componentType", 5126); // FLOAT
        positionAccessor.put("count", 8);
        positionAccessor.put("type", "VEC3");
        positionAccessor.put("max", new double[]{size, size, size});
        positionAccessor.put("min", new double[]{-size, -size, -size});
        accessors.add(positionAccessor);
        
        // Normal accessor
        JSONObject normalAccessor = new JSONObject();
        normalAccessor.put("bufferView", 1);
        normalAccessor.put("componentType", 5126); // FLOAT
        normalAccessor.put("count", 8);
        normalAccessor.put("type", "VEC3");
        accessors.add(normalAccessor);
        
        // Texture coordinate accessor
        JSONObject texCoordAccessor = new JSONObject();
        texCoordAccessor.put("bufferView", 2);
        texCoordAccessor.put("componentType", 5126); // FLOAT
        texCoordAccessor.put("count", 8);
        texCoordAccessor.put("type", "VEC2");
        accessors.add(texCoordAccessor);
        
        // Indices accessor
        JSONObject indicesAccessor = new JSONObject();
        indicesAccessor.put("bufferView", 3);
        indicesAccessor.put("componentType", 5123); // UNSIGNED_SHORT
        indicesAccessor.put("count", 36);
        indicesAccessor.put("type", "SCALAR");
        accessors.add(indicesAccessor);
        
        gltf.put("accessors", accessors);
        
        // Buffer Views
        JSONArray bufferViews = new JSONArray();
        
        // Position buffer view
        JSONObject positionBufferView = new JSONObject();
        positionBufferView.put("buffer", 0);
        positionBufferView.put("byteOffset", 0);
        positionBufferView.put("byteLength", 96); // 8 vertices * 3 components * 4 bytes
        bufferViews.add(positionBufferView);
        
        // Normal buffer view
        JSONObject normalBufferView = new JSONObject();
        normalBufferView.put("buffer", 0);
        normalBufferView.put("byteOffset", 96);
        normalBufferView.put("byteLength", 96);
        bufferViews.add(normalBufferView);
        
        // Texture coordinate buffer view
        JSONObject texCoordBufferView = new JSONObject();
        texCoordBufferView.put("buffer", 0);
        texCoordBufferView.put("byteOffset", 192);
        texCoordBufferView.put("byteLength", 64); // 8 vertices * 2 components * 4 bytes
        bufferViews.add(texCoordBufferView);
        
        // Indices buffer view
        JSONObject indicesBufferView = new JSONObject();
        indicesBufferView.put("buffer", 0);
        indicesBufferView.put("byteOffset", 256);
        indicesBufferView.put("byteLength", 72); // 36 indices * 2 bytes
        bufferViews.add(indicesBufferView);
        
        gltf.put("bufferViews", bufferViews);
        
        // Buffers
        JSONArray buffers = new JSONArray();
        JSONObject buffer = new JSONObject();
        
        // 生成立方体数据
        byte[] bufferData = generateCubeBufferData(size);
        String base64Data = Base64.getEncoder().encodeToString(bufferData);
        
        buffer.put("uri", "data:application/octet-stream;base64," + base64Data);
        buffer.put("byteLength", bufferData.length);
        buffers.add(buffer);
        gltf.put("buffers", buffers);
        
        // 添加自定义属性
        if (description != null) {
            JSONObject extras = new JSONObject();
            extras.put("description", description);
            gltf.put("extras", extras);
        }
        
        return gltf.toJSONString();
    }
    
    /**
     * 生成球体的GLTF数据
     */
    public static String generateSphere(double radius, int segments, String color, String description) {
        JSONObject gltf = new JSONObject();
        
        // Asset信息
        JSONObject asset = new JSONObject();
        asset.put("version", "2.0");
        asset.put("generator", "Generate3D-Backend");
        gltf.put("asset", asset);
        
        // 基本结构（简化版本，实际应该生成球体顶点）
        gltf.put("scene", 0);
        
        JSONArray scenes = new JSONArray();
        JSONObject scene = new JSONObject();
        scene.put("nodes", new int[]{0});
        scenes.add(scene);
        gltf.put("scenes", scenes);
        
        // 添加自定义属性
        JSONObject extras = new JSONObject();
        extras.put("description", description);
        extras.put("type", "sphere");
        extras.put("radius", radius);
        extras.put("segments", segments);
        gltf.put("extras", extras);
        
        return gltf.toJSONString();
    }
    
    /**
     * 生成平面的GLTF数据
     */
    public static String generatePlane(double width, double height, String color, String description) {
        JSONObject gltf = new JSONObject();
        
        // Asset信息
        JSONObject asset = new JSONObject();
        asset.put("version", "2.0");
        asset.put("generator", "Generate3D-Backend");
        gltf.put("asset", asset);
        
        // 基本结构
        gltf.put("scene", 0);
        
        JSONArray scenes = new JSONArray();
        JSONObject scene = new JSONObject();
        scene.put("nodes", new int[]{0});
        scenes.add(scene);
        gltf.put("scenes", scenes);
        
        // 添加自定义属性
        JSONObject extras = new JSONObject();
        extras.put("description", description);
        extras.put("type", "plane");
        extras.put("width", width);
        extras.put("height", height);
        gltf.put("extras", extras);
        
        return gltf.toJSONString();
    }
    
    /**
     * 根据参数生成3D模型
     */
    public static String generateModel(String type, Map<String, Object> params, String description) {
        String color = (String) params.getOrDefault("color", "#ff0000");
        
        switch (type.toLowerCase()) {
            case "cube":
            case "box":
                double size = getDoubleParam(params, "size", 1.0);
                return generateCube(size, color, description);
                
            case "sphere":
            case "ball":
                double radius = getDoubleParam(params, "radius", 1.0);
                int segments = getIntParam(params, "segments", 32);
                return generateSphere(radius, segments, color, description);
                
            case "plane":
            case "rectangle":
                double width = getDoubleParam(params, "width", 2.0);
                double height = getDoubleParam(params, "height", 2.0);
                return generatePlane(width, height, color, description);
                
            default:
                // 默认生成立方体
                return generateCube(1.0, color, description);
        }
    }
    
    /**
     * 生成立方体的缓冲区数据
     */
    private static byte[] generateCubeBufferData(double size) {
        // 立方体顶点位置
        float[] positions = {
            // 前面
            -(float)size, -(float)size,  (float)size,
             (float)size, -(float)size,  (float)size,
             (float)size,  (float)size,  (float)size,
            -(float)size,  (float)size,  (float)size,
            // 后面
            -(float)size, -(float)size, -(float)size,
             (float)size, -(float)size, -(float)size,
             (float)size,  (float)size, -(float)size,
            -(float)size,  (float)size, -(float)size
        };
        
        // 法向量
        float[] normals = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f
        };
        
        // 纹理坐标
        float[] texCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        };
        
        // 索引
        short[] indices = {
            // 前面
            0, 1, 2, 0, 2, 3,
            // 后面
            4, 6, 5, 4, 7, 6,
            // 左面
            4, 0, 3, 4, 3, 7,
            // 右面
            1, 5, 6, 1, 6, 2,
            // 上面
            3, 2, 6, 3, 6, 7,
            // 下面
            4, 5, 1, 4, 1, 0
        };
        
        // 计算总缓冲区大小
        int totalSize = positions.length * 4 + normals.length * 4 + texCoords.length * 4 + indices.length * 2;
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // 写入位置数据
        for (float pos : positions) {
            buffer.putFloat(pos);
        }
        
        // 写入法向量数据
        for (float normal : normals) {
            buffer.putFloat(normal);
        }
        
        // 写入纹理坐标数据
        for (float texCoord : texCoords) {
            buffer.putFloat(texCoord);
        }
        
        // 写入索引数据
        for (short index : indices) {
            buffer.putShort(index);
        }
        
        return buffer.array();
    }
    
    /**
     * 将十六进制颜色转换为RGBA数组
     */
    private static double[] hexToRgba(String hex) {
        if (hex == null || !hex.startsWith("#") || hex.length() != 7) {
            return new double[]{1.0, 0.0, 0.0, 1.0}; // 默认红色
        }
        
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            
            return new double[]{
                r / 255.0,
                g / 255.0,
                b / 255.0,
                1.0
            };
        } catch (NumberFormatException e) {
            log.warn("无效的颜色格式: {}", hex);
            return new double[]{1.0, 0.0, 0.0, 1.0};
        }
    }
    
    /**
     * 获取双精度参数
     */
    private static double getDoubleParam(Map<String, Object> params, String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("无效的数值参数: {} = {}", key, value);
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取整数参数
     */
    private static int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无效的整数参数: {} = {}", key, value);
            }
        }
        return defaultValue;
    }
    
    /**
     * 验证GLTF数据格式
     */
    public static boolean validateGLTF(String gltfData) {
        try {
            JSONObject gltf = JSON.parseObject(gltfData);
            
            // 检查必需字段
            if (!gltf.containsKey("asset")) {
                return false;
            }
            
            JSONObject asset = gltf.getJSONObject("asset");
            if (!asset.containsKey("version")) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("GLTF数据验证失败", e);
            return false;
        }
    }
    
    /**
     * 提取GLTF模型信息
     */
    public static Map<String, Object> extractModelInfo(String gltfData) {
        Map<String, Object> info = new java.util.HashMap<>();
        
        try {
            JSONObject gltf = JSON.parseObject(gltfData);
            
            // 提取基本信息
            if (gltf.containsKey("asset")) {
                JSONObject asset = gltf.getJSONObject("asset");
                info.put("version", asset.getString("version"));
                info.put("generator", asset.getString("generator"));
            }
            
            // 提取顶点和面数量
            if (gltf.containsKey("accessors")) {
                JSONArray accessors = gltf.getJSONArray("accessors");
                for (int i = 0; i < accessors.size(); i++) {
                    JSONObject accessor = accessors.getJSONObject(i);
                    if ("VEC3".equals(accessor.getString("type"))) {
                        info.put("verticesCount", accessor.getInteger("count"));
                        break;
                    }
                }
            }
            
            // 提取自定义属性
            if (gltf.containsKey("extras")) {
                JSONObject extras = gltf.getJSONObject("extras");
                info.putAll(extras);
            }
            
        } catch (Exception e) {
            log.error("提取GLTF模型信息失败", e);
        }
        
        return info;
    }
}