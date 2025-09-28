<template>
  <div class="three-scene">
    <div class="scene-controls">
      <button @click="toggle2d3dMode(false)" :class="{ active: !is2dMode }">3D视角</button>
      <button @click="toggle2d3dMode(true)" :class="{ active: is2dMode }">2D俯瞰</button>
      <button @click="toggleAutoRotate" :class="{ active: isAutoRotate }">
        {{ isAutoRotate ? '停止漫游' : '自动漫游' }}
      </button>
    </div>
    <div class="scene-container" ref="sceneRef"></div>
    <div class="info-container">
      <div class="infoBox">
        <div class="modelName">
          模型信息
        </div>
        <div v-if="selectedModel">
          <div class="line"><span>选中模型：</span>
            <p>{{ selectedModel.name }}</p>
          </div>
          <div class="line"><span>位置：</span>
            <p>({{ selectedModel.position.x.toFixed(1) }}, {{ selectedModel.position.y.toFixed(1) }}, {{
              selectedModel.position.z.toFixed(1) }})</p>
          </div>
          <div class="line"><span>尺寸：</span>
            <p>W {{ selectedModel.size.width.toFixed(1) }} × H {{ selectedModel.size.height.toFixed(1) }} × D {{
              selectedModel.size.depth.toFixed(1) }}</p>
          </div>
        </div>
        <div class="noselected" v-else>选中模型后显示</div>
      </div>
      <div class="operationBox">
        <div class="modelName">
          编辑模型
        </div>
        <!-- Element Form 布局：保留X/Y/Z独立缩放（长宽高），去掉非均匀缩放标签 -->
        <el-form v-if="selectedModel" :model="formData" label-width="80px" size="small" class="transform-form">
          <!-- 缩放控制区域：仅保留“均匀缩放”和“X/Y/Z轴独立缩放” -->
          <el-form-item label="缩放" class="form-section-label">
            <!-- 1. 均匀缩放 -->
            <el-form-item label="均匀缩放">
              <el-input v-model.number="uniformScale" type="number" step="0.1" min="0.1" @input="applyUniformScale"
                size="small" placeholder="等比缩放" />
            </el-form-item>
            <!-- 2. X/Y/Z轴独立缩放（对应长宽高）：去掉“非均匀缩放”标签，直接展示轴控制 -->
            <div class="axis-group">
              <el-form-item label="X (宽)" class="axis-item">
                <el-input v-model.number="scale.x" type="number" step="0.1" min="0.1" @input="applyNonUniformScale"
                  size="small" />
              </el-form-item>
              <el-form-item label="Y (高)" class="axis-item">
                <el-input v-model.number="scale.y" type="number" step="0.1" min="0.1" @input="applyNonUniformScale"
                  size="small" />
              </el-form-item>
              <el-form-item label="Z (深)" class="axis-item">
                <el-input v-model.number="scale.z" type="number" step="0.1" min="0.1" @input="applyNonUniformScale"
                  size="small" />
              </el-form-item>
            </div>
          </el-form-item>

          <el-form-item label="旋转" class="form-section-label">
            <el-form-item label="X (俯仰)">
              <el-input v-model.number="rotation.x" type="number" step="1" @input="applyRotation" size="small" />
            </el-form-item>
            <el-form-item label="Y (偏航)">
              <el-input v-model.number="rotation.y" type="number" step="1" @input="applyRotation" size="small" />
            </el-form-item>
            <el-form-item label="Z (滚转)">
              <el-input v-model.number="rotation.z" type="number" step="1" @input="applyRotation" size="small" />
            </el-form-item>
          </el-form-item>
        </el-form>
        <div v-else class="noselected">选中模型后显示</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import ThreeJSManager from '../../utils/ThreeJSManager.js';
import { ElForm, ElFormItem, ElInput } from 'element-plus';
import 'element-plus/dist/index.css';

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

// 响应式数据（核心逻辑不变）
const exportFormat = ref('gltf');
const selectedModel = ref(null);
const uniformScale = ref(1.0); // 均匀缩放
const scale = ref({ x: 1.0, y: 1.0, z: 1.0 }); // X=宽/Y=高/Z=深，独立缩放
const rotation = ref({ x: 0, y: 0, z: 0 });
const formData = ref({}); // el-form 空模型（满足语法要求）

// 3D管理器与视角/漫游状态（不变）
const threeManager = new ThreeJSManager();
const is2dMode = ref(false);
const isAutoRotate = ref(false);

// 初始化3D场景（不变）
const initScene = () => {
  const container = sceneRef.value;
  if (!container) return;

  threeManager.initScene(container, (modelData) => {
    emit('modelPlaced', modelData);
  });

  threeManager.setOnModelClicked((model) => {
    selectedModel.value = model;
    if (model) {
      const transform = threeManager.getSelectedModelTransform();
      if (transform) {
        uniformScale.value = transform.scale.x;
        scale.value = { ...transform.scale };
        rotation.value = { ...transform.rotation };
      }
    }
  });
  setTimeout(() => {
    threeManager.saveDefaultCameraState();
  }, 500);
};

// 视角切换与漫游（不变）
const toggle2d3dMode = (is2d) => {
  is2dMode.value = is2d;
  threeManager.toggle2d3dMode(is2d);
  if (isAutoRotate.value) toggleAutoRotate();
};
const toggleAutoRotate = () => {
  isAutoRotate.value = !isAutoRotate.value;
  threeManager.toggleAutoRotate(isAutoRotate.value);
  if (is2dMode.value && isAutoRotate.value) toggle2d3dMode(false);
};

// 缩放逻辑（不变：均匀缩放同步X/Y/Z，独立缩放同步均匀值）
const applyUniformScale = () => {
  if (!selectedModel.value) return;
  threeManager.setModelScale(uniformScale.value, true);
  scale.value = { x: uniformScale.value, y: uniformScale.value, z: uniformScale.value };
};
const applyNonUniformScale = () => {
  if (!selectedModel.value) return;
  threeManager.setModelScale(scale.value, false);
  uniformScale.value = (scale.value.x + scale.value.y + scale.value.z) / 3;
};

// 旋转逻辑（不变）
const applyRotation = () => {
  if (!selectedModel.value) return;
  threeManager.setModelRotation(rotation.value, true);
};

// 模型加载与生命周期（不变）
const generateRoom = () => threeManager.generateRoom(props.roomParams);
const loadRoomModel = (modelUrl, mtlUrl, scale = 10) => threeManager.loadRoomModel(modelUrl, mtlUrl, scale);
const loadSimpleModel = (modelUrl, scale = 1, mtlUrl = null) => {
  const model = {
    id: `model-${Date.now()}`,
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
  if (props.currentDragModel) threeManager.loadAndPlaceModel(props.currentDragModel, props.currentDragModel.scale || 1);
};
const addParticlesEffect = () => { };
const exportScene = () => threeManager.exportScene(exportFormat.value, () => emit('export-models'));

// 暴露方法与生命周期（不变）
defineExpose({ addParticlesEffect, loadRoomModel, loadSimpleModel, exportScene });
onMounted(() => initScene());
onBeforeUnmount(() => {
  threeManager.toggleAutoRotate(false);
  threeManager.cleanupScene();
});
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

.info-container {
  position: fixed;
  top: @headerHeight;
  right: 0;
  width: 250px;
  height: calc(100vh - @headerHeight);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .infoBox {
    position: relative;
    width: 100%;
    height: 20%;
    background-color: #fff;
    border-radius: 16px;
    padding: 20px;

    .modelName {
      margin-bottom: 20px;
      text-align: center;
    }

    .line {
      margin-bottom: 10px;
      font-size: 14px;

      p {
        display: inline-block;
        opacity: 0.5;
      }
    }


  }

  .operationBox {
    position: relative;
    width: 100%;
    height: 79%;
    background-color: #fff;
    border-radius: 16px;
    padding: 20px;

    .modelName {
      margin-bottom: 20px;
      text-align: center;
    }

    .transform-form {
      width: 100%;

      .form-section-label {
        padding: 10px 0;
        margin-bottom: 5px;
        font-weight: 500;
        color: #444;
        border-top: 1px solid #eee;

        &:first-of-type {
          border-top: none;
        }
      }

      .axis-group {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 5px;
      }

      .axis-item {
        flex: 1;
        min-width: 80px;

        .el-form-item__label {
          width: 50px !important;
          padding-right: 5px;
        }

        .el-input {
          width: 100% !important;
        }
      }

      /* 统一表单项样式 */
      .el-form-item {
        margin-bottom: 8px;

        .el-input {
          width: 100% !important;
        }

        .el-form-item__label {
          padding-right: 10px;
        }
      }
    }
  }
}

.scene-controls {
  position: fixed;
  left: 20%;
  bottom: 0;
  transform: translateX();
  z-index: 100;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);

  button {
    padding: 6px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background-color: #fff;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    &.active {
      background-color: #409eff;
      color: #fff;
      border-color: #409eff;
    }

    &+button {
      margin-left: 8px;
    }
  }
}

.noselected {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translateX(-50%) translateY(-50%);
  opacity: 0.5;
}
</style>