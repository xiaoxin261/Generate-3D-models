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
    <!-- 模型信息显示区域 -->
    <div id="model-info" class="model-info"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import ThreeJSManager from '../../utils/ThreeJSManager.js';

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

  threeManager.setOnModelClicked((selectedModel) => {
  if (selectedModel) {
    // 点击到模型：打印/使用模型参数
    console.log('选中的模型信息：', {
      id: selectedModel.id, // 模型ID
      name: selectedModel.name, // 模型名称
      position: selectedModel.position, // 模型位置（x,y,z）
      rotation: selectedModel.rotation, // 模型旋转（x,y,z）
      scale: selectedModel.scale, // 模型缩放（x,y,z）
      size: selectedModel.size, // 模型尺寸（width/height/depth）
      originalSize: { // 模型原始尺寸（排除缩放）
        width: selectedModel.size.originalWidth,
        height: selectedModel.size.originalHeight,
        depth: selectedModel.size.originalDepth
      }
    });

    // 示例：在页面中显示模型尺寸
    const infoEl = document.getElementById('model-info');
    infoEl.innerHTML = `
      <h3>选中模型：${selectedModel.name}</h3>
      <p>宽度：${selectedModel.size.width} 单位</p>
      <p>高度：${selectedModel.size.height} 单位</p>
      <p>深度：${selectedModel.size.depth} 单位</p>
      <p>原始宽度：${selectedModel.size.originalWidth} 单位</p>
    `;
  } else {
    // 点击空白区域：清空选中状态
    console.log('未选中任何模型');
    document.getElementById('model-info').innerHTML = '未选中模型';
  }
});
};

// 生成房间
const generateRoom = () => {
  threeManager.generateRoom(props.roomParams);
};

// 房间模型加载
const loadRoomModel = (modelUrl, mtlUrl, scale = 10) => {
  threeManager.loadRoomModel(modelUrl, mtlUrl, scale);
};

// 普通模型加载
const loadSimpleModel = (modelUrl, scale = 1, mtlUrl = null) => {
  const model = {
    id: 'simple-model',
    name: '普通模型',
    modelUrl: modelUrl,
    mtlUrl: mtlUrl, // 添加MTL文件路径
    format: modelUrl.toLowerCase().includes('.obj') ? 'obj' :
      modelUrl.toLowerCase().includes('.gltf') || modelUrl.toLowerCase().includes('.glb') ? 'gltf' :
        modelUrl.toLowerCase().includes('.stl') ? 'stl' : 'obj'
  };
  threeManager.loadAndPlaceModel(model, scale);
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
  loadRoomModel,
  loadSimpleModel
});

// 生命周期钩子
onMounted(() => {
  initScene();
  // generateRoom(10);
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
  transform: translateX(-50%);
  z-index: 100;
  padding: 10px 10px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  background-color: #fff;
  border-radius: 0 0 8px 8px;
}

.model-info {
  position: fixed;
  top: @headerHeight;
  right: 0;
  z-index: 100;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>