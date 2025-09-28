<template>
  <div class="three-scene">
    <div class="scene-controls">
      <button @click="exportScene">导出场景</button>
      <select v-model="exportFormat">
        <option value="gltf">GLTF</option>
        <option value="obj">OBJ</option>
        <option value="stl">STL</option>
      </select>
    </div>
    <div class="scene-container" ref="sceneRef"></div>

    <!-- 1. 模型信息与控制面板 (合并并优化) -->
    <div v-if="selectedModel" id="model-info" class="model-info">
      <h3>选中模型：{{ selectedModel.name }}</h3>
      <p>ID: {{ selectedModel.id }}</p>
      <p>位置: ({{ selectedModel.position.x.toFixed(2) }}, {{ selectedModel.position.y.toFixed(2) }}, {{ selectedModel.position.z.toFixed(2) }})</p>
      <p>尺寸: W {{ selectedModel.size.width.toFixed(2) }} × H {{ selectedModel.size.height.toFixed(2) }} × D {{ selectedModel.size.depth.toFixed(2) }}</p>

      <!-- 2. 缩放控制 -->
      <div class="transform-control-group">
        <h4>缩放</h4>
        <div class="uniform-control">
          <label>均匀缩放:</label>
          <input type="number" v-model.number="uniformScale" step="0.1" min="0.1" @input="applyUniformScale">
        </div>
        <div class="non-uniform-control">
          <label>X:</label>
          <input type="number" v-model.number="scale.x" step="0.1" min="0.1" @input="applyNonUniformScale">
          <label>Y:</label>
          <input type="number" v-model.number="scale.y" step="0.1" min="0.1" @input="applyNonUniformScale">
          <label>Z:</label>
          <input type="number" v-model.number="scale.z" step="0.1" min="0.1" @input="applyNonUniformScale">
        </div>
      </div>

      <!-- 3. 旋转控制 -->
      <div class="transform-control-group">
        <h4>旋转 (角度)</h4>
        <div class="non-uniform-control">
          <label>X (俯仰):</label>
          <input type="number" v-model.number="rotation.x" step="1" @input="applyRotation">
          <label>Y (偏航):</label>
          <input type="number" v-model.number="rotation.y" step="1" @input="applyRotation">
          <label>Z (滚转):</label>
          <input type="number" v-model.number="rotation.z" step="1" @input="applyRotation">
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import ThreeJSManager from '../../utils/ThreeJSManager.js';
import * as THREE from 'three'; // 引入THREE，用于角度转换

// 定义props
const props = defineProps({
  roomParams: {
    type: Object,
    default: () => ({ length: 5, width: 5, height: 3, style: 'modern' })
  },
  currentDragModel: {
    type: Object,
    default: null
  },
  roomModelUrl: {
    type: String,
    default: null
  }
});

// 定义emits
const emit = defineEmits(['modelPlaced', 'export-models']);

// 引用
const sceneRef = ref(null);

// 响应式数据
const exportFormat = ref('gltf');
const selectedModel = ref(null); // 4. 存储当前选中的模型信息
const uniformScale = ref(1.0);   // 5. 用于双向绑定的均匀缩放值
const scale = ref({ x: 1.0, y: 1.0, z: 1.0 }); // 6. 用于双向绑定的非均匀缩放值
const rotation = ref({ x: 0, y: 0, z: 0 }); // 7. 用于双向绑定的旋转值 (角度制)

// 创建ThreeJSManager实例
const threeManager = new ThreeJSManager();

// 初始化3D场景
const initScene = () => {
  const container = sceneRef.value;
  if (!container) return;

  threeManager.initScene(container, (modelData) => {
    emit('modelPlaced', modelData);
  });

  // 8. 监听模型点击事件，更新UI状态
  threeManager.setOnModelClicked((model) => {
    selectedModel.value = model;

    if (model) {
      // 9. 当模型被选中时，同步其当前的变换数据到UI输入框
      const transform = threeManager.getSelectedModelTransform();
      if (transform) {
        uniformScale.value = transform.scale.x; // 假设初始是均匀缩放
        scale.value = { ...transform.scale };
        rotation.value = { ...transform.rotation };
      }
    }
  });
};

// 10. 应用均匀缩放
const applyUniformScale = () => {
  if (!selectedModel.value) return;
  threeManager.setModelScale(uniformScale.value, true);
  // 同步非均匀缩放的输入框显示
  scale.value = { x: uniformScale.value, y: uniformScale.value, z: uniformScale.value };
};

// 11. 应用非均匀缩放
const applyNonUniformScale = () => {
  if (!selectedModel.value) return;
  threeManager.setModelScale(scale.value, false);
  // 同步均匀缩放的输入框显示（取平均值）
  uniformScale.value = (scale.value.x + scale.value.y + scale.value.z) / 3;
};

// 12. 应用旋转
const applyRotation = () => {
  if (!selectedModel.value) return;
  threeManager.setModelRotation(rotation.value, true);
};

const generateRoom = () => {
  threeManager.generateRoom(props.roomParams);
};

const loadRoomModel = (modelUrl, mtlUrl, scale = 10) => {
  threeManager.loadRoomModel(modelUrl, mtlUrl, scale);
};

const loadSimpleModel = (modelUrl, scale = 1, mtlUrl = null) => {
  const model = {
    id: `model-${Date.now()}`, // 使用时间戳确保ID唯一
    name: '普通模型',
    modelUrl: modelUrl,
    mtlUrl: mtlUrl,
    format: modelUrl.toLowerCase().includes('.obj') ? 'obj' :
      modelUrl.toLowerCase().includes('.gltf') || modelUrl.toLowerCase().includes('.glb') ? 'gltf' :
        modelUrl.toLowerCase().includes('.stl') ? 'stl' : 'obj'
  };
  threeManager.loadAndPlaceModel(model, scale);
};

const handleDragModelChange = () => {
  if (props.currentDragModel) {
    const scale = props.currentDragModel.scale || 1;
    threeManager.loadAndPlaceModel(props.currentDragModel, scale);
  }
};

const addParticlesEffect = () => {
  // 注意：你的ThreeJSManager中没有addParticleEffect方法，需要先实现
  // threeManager.addParticleEffect(new THREE.Vector3(0, 1, 0));
};

const exportScene = () => {
  threeManager.exportScene(exportFormat.value, () => {
    emit('export-models');
  });
};

defineExpose({
  addParticlesEffect,
  loadRoomModel,
  loadSimpleModel
});

// 生命周期钩子
onMounted(() => {
  initScene();
  // generateRoom(); // 如果你想默认生成一个房间
});

onBeforeUnmount(() => {
  threeManager.cleanupScene();
});

// 监听props变化
watch(() => props.roomParams, generateRoom, { deep: true });
watch(() => props.currentDragModel, handleDragModelChange);
</script>

<style scoped lang="less">
@headerHeight: 60px;
.three-scene {
  width: 100%;
  height: 100%;
}

.scene-container {
  height: calc(100vh - @headerHeight);
  width: 100%;
  background-color: #f5f5f5;
}

.scene-controls {
  position: fixed;
  left: 50%;
  top: 0;
  transform: translateX(-50%);
  z-index: 100;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  background-color: #fff;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.model-info {
  position: fixed;
  top: @headerHeight;
  right: 20px;
  z-index: 100;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 300px;
  font-size: 14px;
  line-height: 1.6;

  h3 {
    margin-top: 0;
    color: #333;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
  }

  p {
    margin: 6px 0;
    color: #666;
  }

  .transform-control-group {
    margin-top: 15px;
    padding-top: 10px;
    border-top: 1px solid #eee;

    h4 {
      margin: 10px 0 8px 0;
      color: #444;
      font-size: 14px;
    }

    .uniform-control, .non-uniform-control {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      flex-wrap: wrap;

      label {
        margin-right: 8px;
        width: 80px;
        font-weight: 500;
        color: #555;
      }

      input {
        width: 60px;
        padding: 4px 6px;
        margin-right: 10px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
      }
    }

    .non-uniform-control {
      label {
        width: 40px;
      }
    }
  }
}
</style>