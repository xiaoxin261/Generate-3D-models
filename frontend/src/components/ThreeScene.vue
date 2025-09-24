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
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import ThreeJSManager from '../utils/ThreeJSManager.js';

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

// 创建ThreeJSManager实例
const threeManager = new ThreeJSManager();

// 初始化3D场景
const initScene = () => {
  const container = sceneRef.value;
  if (!container) return;
  
  threeManager.initScene(container, (modelData) => {
    emit('modelPlaced', modelData);
  });
};

// 生成房间
const generateRoom = () => {
  threeManager.generateRoom(props.roomParams);
};

// 房间模型加载
const loadRoomModel = (modelUrl, scale = 1) => {
  threeManager.loadRoomModel(modelUrl, scale);
};

// 处理拖拽模型变化
const handleDragModelChange = () => {
  if (props.currentDragModel) {
    // 从模型数据中获取scale参数，如果没有则使用默认值1
    const scale = props.currentDragModel.scale || 1;
    threeManager.loadAndPlaceModel(props.currentDragModel, scale);
  }
};

// 添加粒子动效
const addParticlesEffect = () => {
  threeManager.addParticleEffect(new THREE.Vector3(0, 1, 0));
};

// 导出场景
const exportScene = () => {
  threeManager.exportScene(exportFormat.value, () => {
    emit('export-models');
  });
};

// 暴露给父组件的方法
defineExpose({
  addParticlesEffect,
  loadRoomModel
});

// 生命周期钩子
onMounted(() => {
  initScene();
  generateRoom();
});

onBeforeUnmount(() => {
  threeManager.cleanupScene();
});

// 监听props变化
watch(() => props.roomParams, generateRoom, { deep: true });
watch(() => props.currentDragModel, handleDragModelChange);
</script>

<style scoped>
.three-scene {
  width: 100%;
  height: 100%;
}

.scene-container {
  height: 100vh;
  width: 100%;
  background-color: #f5f5f5;
}

.scene-controls{
  position: fixed;   /* 固定定位 */
  top: 0;
  left: 50%;         /* 先移到中间 */
  transform: translateX(-50%);
  z-index: 100;
  padding: 10px 10px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  background-color: #fff;
  border-radius: 0 0 8px 8px;
}
</style>