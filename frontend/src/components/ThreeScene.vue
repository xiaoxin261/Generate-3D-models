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
import { ref, onMounted, onBeforeUnmount, watch, shallowRef } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js';
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js';
import { GLTFExporter } from 'three/examples/jsm/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/examples/jsm/exporters/OBJExporter.js';
import * as TWEEN from '@tweenjs/tween.js';

// 定义props
const props = defineProps({
  roomParams: {
    type: Object,
    default: () => ({ length: 5, width: 5, height: 3, style: 'modern' })
  },
  currentDragModel: {
    type: Object,
    default: null
  }
});

// 定义emits
const emit = defineEmits(['modelPlaced', 'export-models']);

// 引用
const sceneRef = ref(null);

// 响应式数据
const exportFormat = ref('gltf');
const sceneModels = ref([]);
const particles = ref([]);

// Three.js对象 - 不使用响应式
let scene = null;
let camera = null;
let renderer = null;
let controls = null;
let roomMesh = null;
let draggingObject = null;
let draggingPlane = null;
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
let isDragging = false;
let currentOperation = 'drag'; // drag, rotate, scale
let animationId = null;

// 初始化3D场景
const initScene = () => {
  const container = sceneRef.value;
  
  if (!container) return;
  
  // 创建场景
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xf0f0f0);
  
  // 创建相机
  camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
  camera.position.set(5, 5, 5);
  camera.lookAt(0, 0, 0);
  
  // 添加光源
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
  scene.add(ambientLight);
  
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
  directionalLight.position.set(5, 10, 7.5);
  scene.add(directionalLight);
  
  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(container.clientWidth, container.clientHeight);
  container.appendChild(renderer.domElement);
  
  // 添加轨道控制器
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  
  // 添加坐标轴辅助线
  const axesHelper = new THREE.AxesHelper(5);
  scene.add(axesHelper);
  
  // 添加地面网格
  const gridHelper = new THREE.GridHelper(10, 10);
  scene.add(gridHelper);
  
  // 添加拖拽平面
  draggingPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);
  
  // 添加鼠标事件监听
  container.addEventListener('mousedown', onMouseDown);
  container.addEventListener('mousemove', onMouseMove);
  container.addEventListener('mouseup', onMouseUp);
  container.addEventListener('wheel', onWheel);
  
  // 开始渲染循环
  animate();
  
  // 监听窗口大小变化
  window.addEventListener('resize', onWindowResize);
};

// 渲染循环
const animate = () => {
  animationId = requestAnimationFrame(animate);
  
  // 更新控制器
  controls.update();
  
  // 更新粒子动画
  TWEEN.update();
  
  // 渲染场景
  renderer.render(scene, camera);
};

// 窗口大小变化处理
const onWindowResize = () => {
  const container = sceneRef.value;
  if (!container || !camera || !renderer) return;
  
  camera.aspect = container.clientWidth / container.clientHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(container.clientWidth, container.clientHeight);
};

// 生成房间
const generateRoom = () => {
  if (!scene) return;
  
  // 移除旧房间
  if (roomMesh) {
    scene.remove(roomMesh);
    roomMesh.geometry.dispose();
    roomMesh.material.dispose();
  }
  
  const { length, width, height } = props.roomParams;
  
  // 创建房间几何体（带洞的立方体）
  const roomGeometry = new THREE.BoxGeometry(length, height, width);
  
  // 创建材质
  let roomMaterial;
  
  switch (props.roomParams.style) {
    case 'modern':
      roomMaterial = new THREE.MeshStandardMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.2,
        side: THREE.DoubleSide
      });
      break;
    case 'minimalist':
      roomMaterial = new THREE.MeshStandardMaterial({
        color: 0xf5f5f5,
        transparent: true,
        opacity: 0.2,
        side: THREE.DoubleSide
      });
      break;
    case 'industrial':
      roomMaterial = new THREE.MeshStandardMaterial({
        color: 0xe0e0e0,
        transparent: true,
        opacity: 0.2,
        side: THREE.DoubleSide
      });
      break;
    default:
      roomMaterial = new THREE.MeshStandardMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.2,
        side: THREE.DoubleSide
      });
  }
  
  // 创建房间网格
  roomMesh = new THREE.Mesh(roomGeometry, roomMaterial);
  scene.add(roomMesh);
  
  // 调整相机位置以适应房间大小
  const maxDimension = Math.max(length, width, height);
  camera.position.set(maxDimension * 1.5, maxDimension * 1.5, maxDimension * 1.5);
  camera.lookAt(0, height / 2, 0);
  
  // 更新控制器
  controls.update();
};

// 处理拖拽模型变化
const handleDragModelChange = () => {
  if (props.currentDragModel) {
    loadAndPlaceModel(props.currentDragModel);
  }
};

// 加载并放置模型
const loadAndPlaceModel = async (modelData) => {
  try {
    // 为了演示，我们使用简单的几何体代替实际模型
    // 在实际应用中，这里应该加载从API获取的模型文件
    const geometry = new THREE.BoxGeometry(1, 1, 1);
    const material = new THREE.MeshStandardMaterial({
      color: Math.random() * 0xffffff
    });
    const model = new THREE.Mesh(geometry, material);
    
    // 设置模型位置
    model.position.set(0, 0.5, 0);
    
    // 添加到场景
    scene.add(model);
    sceneModels.value.push({
      id: modelData.id || Date.now(),
      name: modelData.name || '模型',
      mesh: model
    });
    
    // 添加粒子动效
    addParticleEffect(model.position);
    
    // 触发放置事件
    emit('modelPlaced', modelData);
  } catch (error) {
    console.error('加载模型失败:', error);
  }
};

// 添加粒子动效
const addParticleEffect = (position) => {
  const particleCount = 50;
  const particlesGeometry = new THREE.BufferGeometry();
  const particlesPosition = new Float32Array(particleCount * 3);
  const particlesColor = new Float32Array(particleCount * 3);
  
  // 初始化粒子位置和颜色
  for (let i = 0; i < particleCount; i++) {
    const i3 = i * 3;
    
    // 在模型位置周围随机分布
    particlesPosition[i3] = position.x + (Math.random() - 0.5) * 0.5;
    particlesPosition[i3 + 1] = position.y + (Math.random() - 0.5) * 0.5;
    particlesPosition[i3 + 2] = position.z + (Math.random() - 0.5) * 0.5;
    
    // 随机颜色
    particlesColor[i3] = Math.random();
    particlesColor[i3 + 1] = Math.random();
    particlesColor[i3 + 2] = Math.random();
  }
  
  particlesGeometry.setAttribute('position', new THREE.BufferAttribute(particlesPosition, 3));
  particlesGeometry.setAttribute('color', new THREE.BufferAttribute(particlesColor, 3));
  
  // 创建粒子材质
  const particlesMaterial = new THREE.PointsMaterial({
    size: 0.05,
    vertexColors: true,
    transparent: true,
    opacity: 0.8
  });
  
  // 创建粒子系统
  const particles = new THREE.Points(particlesGeometry, particlesMaterial);
  scene.add(particles);
  
  // 保存粒子系统引用
  particles.value.push({
    mesh: particles,
    startTime: Date.now()
  });
  
  // 为每个粒子创建动画
  const positions = particlesGeometry.attributes.position.array;
  const originalPositions = [...positions];
  
  for (let i = 0; i < particleCount; i++) {
    const i3 = i * 3;
    const targetX = originalPositions[i3] + (Math.random() - 0.5) * 2;
    const targetY = originalPositions[i3 + 1] + Math.random() * 2;
    const targetZ = originalPositions[i3 + 2] + (Math.random() - 0.5) * 2;
    
    new TWEEN.Tween({
      x: originalPositions[i3],
      y: originalPositions[i3 + 1],
      z: originalPositions[i3 + 2],
      opacity: 1
    })
      .to({
        x: targetX,
        y: targetY,
        z: targetZ,
        opacity: 0
      }, 2000)
      .onUpdate((coords) => {
        positions[i3] = coords.x;
        positions[i3 + 1] = coords.y;
        positions[i3 + 2] = coords.z;
        particlesMaterial.opacity = coords.opacity;
        particlesGeometry.attributes.position.needsUpdate = true;
      })
      .onComplete(() => {
        // 动画结束后清理粒子
        if (scene) {
          scene.remove(particles);
          particlesGeometry.dispose();
          particlesMaterial.dispose();
          particles.value = particles.value.filter(p => p.mesh !== particles);
        }
      })
      .start();
  }
};

// 鼠标按下事件
const onMouseDown = (event) => {
  updateMousePosition(event);
  raycaster.setFromCamera(mouse, camera);
  
  // 检查是否点击了模型
  const intersects = raycaster.intersectObjects(
    sceneModels.value.map(m => m.mesh)
  );
  
  if (intersects.length > 0) {
    isDragging = true;
    draggingObject = intersects[0].object;
    controls.enabled = false;
  }
};

// 鼠标移动事件
const onMouseMove = (event) => {
  if (!isDragging || !draggingObject) return;
  
  updateMousePosition(event);
  raycaster.setFromCamera(mouse, camera);
  
  // 计算拖拽平面与光线的交点
  const intersects = raycaster.intersectPlane(draggingPlane, new THREE.Vector3());
  
  if (intersects) {
    draggingObject.position.copy(intersects);
    // 确保模型底部接触地面
    draggingObject.position.y = draggingObject.position.y + 0.5;
  }
};

// 鼠标释放事件
const onMouseUp = () => {
  isDragging = false;
  draggingObject = null;
  controls.enabled = true;
};

// 鼠标滚轮事件（用于缩放）
const onWheel = (event) => {
  event.preventDefault();
  
  // 简单实现：根据滚轮方向调整相机位置
  const delta = event.deltaY > 0 ? 0.1 : -0.1;
  const direction = new THREE.Vector3().subVectors(camera.position, camera.target || new THREE.Vector3(0, 1, 0));
  const length = direction.length();
  
  if ((delta < 0 || length > 1) && (delta > 0 || length < 15)) {
    camera.position.addScaledVector(direction.normalize(), delta);
  }
};

// 更新鼠标位置
const updateMousePosition = (event) => {
  const container = sceneRef.value;
  const rect = container.getBoundingClientRect();
  
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
};

// 导出场景
const exportScene = () => {
  const exportableModels = sceneModels.value.map(m => m.mesh);
  
  if (exportableModels.length === 0) {
    alert('场景中没有可导出的模型');
    return;
  }
  
  emit('export-models');
  
  switch (exportFormat.value) {
    case 'gltf':
      exportAsGLTF(exportableModels);
      break;
    case 'obj':
      exportAsOBJ(exportableModels);
      break;
    case 'stl':
      exportAsSTL(exportableModels);
      break;
    default:
      exportAsGLTF(exportableModels);
  }
};

// 导出为GLTF格式
const exportAsGLTF = (models) => {
  const exporter = new GLTFExporter();
  
  exporter.parse(models, (gltf) => {
    const data = JSON.stringify(gltf);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    downloadFile(url, 'scene.gltf');
  }, (error) => {
    console.error('导出GLTF失败:', error);
  });
};

// 导出为OBJ格式
const exportAsOBJ = (models) => {
  const exporter = new OBJExporter();
  const data = exporter.parse(models);
  
  const blob = new Blob([data], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  
  downloadFile(url, 'scene.obj');
};

// 导出为STL格式（简化实现）
const exportAsSTL = (models) => {
  // 创建一个临时的合并几何体
  const mergedGeometry = new THREE.BufferGeometry();
  const positionAttribute = [];
  
  models.forEach(model => {
    if (model.geometry && model.geometry.isBufferGeometry) {
      const positions = model.geometry.attributes.position.array;
      // 应用模型的世界矩阵
      const worldMatrix = new THREE.Matrix4().compose(
        model.position,
        model.quaternion,
        model.scale
      );
      
      // 转换顶点
      for (let i = 0; i < positions.length; i += 3) {
        const vertex = new THREE.Vector3(
          positions[i],
          positions[i + 1],
          positions[i + 2]
        );
        vertex.applyMatrix4(worldMatrix);
        positionAttribute.push(vertex.x, vertex.y, vertex.z);
      }
    }
  });
  
  mergedGeometry.setAttribute('position', new THREE.Float32BufferAttribute(positionAttribute, 3));
  
  // 生成STL数据
  let stlData = 'solid scene\n';
  const positions = mergedGeometry.attributes.position.array;
  
  for (let i = 0; i < positions.length; i += 9) {
    stlData += '  facet normal 0 0 0\n';
    stlData += '    outer loop\n';
    stlData += `      vertex ${positions[i]} ${positions[i + 1]} ${positions[i + 2]}\n`;
    stlData += `      vertex ${positions[i + 3]} ${positions[i + 4]} ${positions[i + 5]}\n`;
    stlData += `      vertex ${positions[i + 6]} ${positions[i + 7]} ${positions[i + 8]}\n`;
    stlData += '    endloop\n';
    stlData += '  endfacet\n';
  }
  
  stlData += 'endsolid scene';
  
  const blob = new Blob([stlData], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  
  downloadFile(url, 'scene.stl');
};

// 下载文件
const downloadFile = (url, filename) => {
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  
  // 清理
  setTimeout(() => {
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }, 100);
};

// 清理场景
const cleanupScene = () => {
  // 取消动画帧
  if (animationId) {
    cancelAnimationFrame(animationId);
  }
  
  // 移除事件监听器
  const container = sceneRef.value;
  if (container) {
    container.removeEventListener('mousedown', onMouseDown);
    container.removeEventListener('mousemove', onMouseMove);
    container.removeEventListener('mouseup', onMouseUp);
    container.removeEventListener('wheel', onWheel);
  }
  
  window.removeEventListener('resize', onWindowResize);
  
  // 清理Three.js对象
  if (scene) {
    // 清理所有网格
    scene.traverse((object) => {
      if (object.isMesh || object.isPoints) {
        if (object.geometry) {
          object.geometry.dispose();
        }
        if (object.material) {
          if (Array.isArray(object.material)) {
            object.material.forEach(material => material.dispose());
          } else {
            object.material.dispose();
          }
        }
      }
    });
    
    // 移除渲染器DOM元素
    if (renderer && renderer.domElement && renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement);
    }
  }
  
  // 清空引用
  scene = null;
  camera = null;
  renderer = null;
  controls = null;
  roomMesh = null;
  sceneModels.value = [];
  particles.value = [];
};

// 暴露给父组件的方法
const addParticlesEffect = () => {
  addParticleEffect(new THREE.Vector3(0, 1, 0));
};

defineExpose({
  addParticlesEffect
});

// 生命周期钩子
onMounted(() => {
  initScene();
  generateRoom();
});

onBeforeUnmount(() => {
  cleanupScene();
});

// 监听props变化
watch(() => props.roomParams, generateRoom, { deep: true });
watch(() => props.currentDragModel, handleDragModelChange);
</script>

<style scoped>
.three-scene {
  position: relative;
  width: 100%;
  height: 100%;
}

.scene-container {
  height: 100vh;
  width: 100%;
  position: absolute;
  left: 0;
  top: 0;
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