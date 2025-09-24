import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js';
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js';
import { GLTFExporter } from 'three/examples/jsm/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/examples/jsm/exporters/OBJExporter.js';
import * as TWEEN from '@tweenjs/tween.js';

class ThreeJSManager {
  constructor() {
    // Three.js对象
    this.scene = null;
    this.camera = null;
    this.renderer = null;
    this.controls = null;
    this.roomMesh = null;
    this.draggingObject = null;
    this.draggingPlane = null;
    this.raycaster = new THREE.Raycaster();
    this.mouse = new THREE.Vector2();
    this.isDragging = false;
    this.currentOperation = 'drag'; // drag, rotate, scale
    this.animationId = null;

    // 容器引用
    this.container = null;

    // 模型列表和粒子系统
    this.sceneModels = [];
    this.particles = [];

    // 事件回调
    this.onModelPlaced = null;
  }

  // 初始化3D场景
  initScene(container, onModelPlaced = null) {
    this.container = container;
    this.onModelPlaced = onModelPlaced;

    if (!container) return;

    // 创建场景
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0xf0f0f0);

    // 创建相机
    this.camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    this.camera.position.set(5, 5, 5);
    this.camera.lookAt(0, 0, 0);

    // 添加光源
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    this.scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(5, 10, 7.5);
    this.scene.add(directionalLight);

    // 创建渲染器
    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setSize(container.clientWidth, container.clientHeight);
    container.appendChild(this.renderer.domElement);

    // 添加轨道控制器
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.05;

    // 添加坐标轴辅助线
    const axesHelper = new THREE.AxesHelper(10);
    this.scene.add(axesHelper);

    // 添加地面网格
    const gridHelper = new THREE.GridHelper(10, 10);
    this.scene.add(gridHelper);

    // 添加拖拽平面
    this.draggingPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);

    // 添加鼠标事件监听
    this._bindEvents();

    // 开始渲染循环
    this.animate();
  }

  // 绑定事件
  _bindEvents() {
    if (!this.container) return;

    const onMouseDown = (event) => this.handleMouseDown(event);
    const onMouseMove = (event) => this.handleMouseMove(event);
    const onMouseUp = () => this.handleMouseUp();
    const onWheel = (event) => this.handleWheel(event);
    const onWindowResize = () => this.handleWindowResize();

    this.container.addEventListener('mousedown', onMouseDown);
    this.container.addEventListener('mousemove', onMouseMove);
    this.container.addEventListener('mouseup', onMouseUp);
    this.container.addEventListener('wheel', onWheel);
    window.addEventListener('resize', onWindowResize);

    // 保存事件处理器引用以便后续清理
    this._eventHandlers = {
      onMouseDown,
      onMouseMove,
      onMouseUp,
      onWheel,
      onWindowResize
    };
  }

  // 渲染循环
  animate() {
    this.animationId = requestAnimationFrame(() => this.animate());

    // 更新控制器
    this.controls.update();

    // 更新粒子动画
    TWEEN.update();

    // 渲染场景
    this.renderer.render(this.scene, this.camera);
  }

  // 窗口大小变化处理
  handleWindowResize() {
    if (!this.container || !this.camera || !this.renderer) return;

    this.camera.aspect = this.container.clientWidth / this.container.clientHeight;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
  }

  // 生成房间
  generateRoom(roomParams) {
    if (!this.scene) return;

    // 移除旧房间
    if (this.roomMesh) {
      this.scene.remove(this.roomMesh);
      this.roomMesh.geometry.dispose();
      this.roomMesh.material.dispose();
    }

    const { length, width, height } = roomParams;

    // 创建房间几何体（带洞的立方体）
    const roomGeometry = new THREE.BoxGeometry(length, height, width);

    // 创建材质
    let roomMaterial;

    switch (roomParams.style) {
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
    this.roomMesh = new THREE.Mesh(roomGeometry, roomMaterial);
    this.scene.add(this.roomMesh);

    // 调整相机位置以适应房间大小
    const maxDimension = Math.max(length, width, height);
    this.camera.position.set(maxDimension * 1.5, maxDimension * 1.5, maxDimension * 1.5);
    this.camera.lookAt(0, height / 2, 0);

    // 更新控制器
    this.controls.update();
  }

  // 房间模型加载
  loadRoomModel(modelUrl, scale = 10) {
    if (!modelUrl) return;

    // 创建房间模型数据结构
    const roomModel = {
      id: 'room-model',
      name: '房间模型',
      modelUrl: modelUrl,
      format: modelUrl.toLowerCase().includes('.obj') ? 'obj' :
        modelUrl.toLowerCase().includes('.gltf') || modelUrl.toLowerCase().includes('.glb') ? 'gltf' :
          modelUrl.toLowerCase().includes('.stl') ? 'stl' : 'obj'
    };

    console.log('加载房间模型:', roomModel);

    // 调用loadAndPlaceModel加载房间模型并传递缩放参数
    this.loadAndPlaceModel(roomModel, scale);
  }

  // 加载并放置模型
  async loadAndPlaceModel(modelData, scale = 1) {
    try {
      // 检查是否有模型URL
      if (modelData.modelUrl) {
        // 移除旧房间
        if (this.roomMesh) {
          this.scene.remove(this.roomMesh);
          this.roomMesh.geometry.dispose();
          this.roomMesh.material.dispose();
        }
        // 根据文件格式选择加载器
        if (modelData.modelUrl.toLowerCase().endsWith('.obj') || modelData.format === 'obj') {
          // 使用OBJLoader加载OBJ模型
          // const loader = new OBJLoader();
          // const model = await new Promise((resolve, reject) => {
          //   loader.load(
          //     modelData.modelUrl,
          //     (object) => resolve(object),
          //     (xhr) => {
          //       console.log((xhr.loaded / xhr.total * 100) + '% 已加载');
          //     },
          //     (error) => reject(error)
          //   );
          // });

          // // 设置模型位置
          // model.position.set(0, 0.5, 0);

          // // 添加到场景
          // this.scene.add(model);
          // this.sceneModels.push({
          //   id: modelData.id || Date.now(),
          //   name: modelData.name || '模型',
          //   mesh: model
          // });

          // // 添加粒子动效
          // this.addParticleEffect(model.position);

          const objLoader = new OBJLoader();
          objLoader.load(
            modelData.modelUrl,
            (object) => {
              // 设置模型位置
              object.position.set(0, 0, 0);

              // 设置模型缩放
              object.scale.set(scale, scale, scale);

              // 添加到场景
              this.scene.add(object);
              this.sceneModels.push({
                id: modelData.id || Date.now(),
                name: modelData.name || '模型',
                mesh: object
              });

              // 添加粒子动效
              this.addParticleEffect(object.position);

              // 触发放置事件
              if (this.onModelPlaced) {
                this.onModelPlaced(modelData);
              }
            },
            (xhr) => {
              console.log((xhr.loaded / xhr.total * 100) + '% 已加载');
            },
            (error) => {
              console.error('OBJ模型加载失败:', error);
            }
          );
          return;
        } else if (modelData.modelUrl.toLowerCase().endsWith('.gltf') || modelData.modelUrl.toLowerCase().endsWith('.glb') || modelData.format === 'gltf') {
          // 使用GLTFLoader加载GLTF/GLB模型
          const loader = new GLTFLoader();
          const gltf = await new Promise((resolve, reject) => {
            loader.load(
              modelData.modelUrl,
              (gltf) => resolve(gltf),
              (xhr) => {
                console.log((xhr.loaded / xhr.total * 100) + '% 已加载');
              },
              (error) => reject(error)
            );
          });

          const model = gltf.scene;

          // 设置模型位置
          model.position.set(0, 0.5, 0);

          // 设置模型缩放
          model.scale.set(scale, scale, scale);

          // 添加到场景
          this.scene.add(model);
          this.sceneModels.push({
            id: modelData.id || Date.now(),
            name: modelData.name || '模型',
            mesh: model
          });

          // 添加粒子动效
          this.addParticleEffect(model.position);

          // 触发放置事件
          if (this.onModelPlaced) {
            this.onModelPlaced(modelData);
          }

          return;
        } else if (modelData.modelUrl.toLowerCase().endsWith('.stl') || modelData.format === 'stl') {
          // 使用STLLoader加载STL模型
          const loader = new STLLoader();
          const geometry = await new Promise((resolve, reject) => {
            loader.load(
              modelData.modelUrl,
              (geometry) => resolve(geometry),
              (xhr) => {
                console.log((xhr.loaded / xhr.total * 100) + '% 已加载');
              },
              (error) => reject(error)
            );
          });

          // 创建材质
          const material = new THREE.MeshStandardMaterial({
            color: 0x777777,
            roughness: 0.5,
            metalness: 0.5
          });

          // 创建网格
          const model = new THREE.Mesh(geometry, material);

          // 设置模型位置和缩放
          model.position.set(0, 0.5, 0);
          model.scale.set(scale, scale, scale); // 使用传入的缩放参数

          // 添加到场景
          this.scene.add(model);
          this.sceneModels.push({
            id: modelData.id || Date.now(),
            name: modelData.name || '模型',
            mesh: model
          });

          // 添加粒子动效
          this.addParticleEffect(model.position);

          // 触发放置事件
          if (this.onModelPlaced) {
            this.onModelPlaced(modelData);
          }

          return;
        }
      }

      // 如果没有有效的URL或不支持的格式，使用默认几何体
      const geometry = new THREE.BoxGeometry(1, 1, 1);
      const material = new THREE.MeshStandardMaterial({
        color: Math.random() * 0xffffff
      });
      const model = new THREE.Mesh(geometry, material);

      // 设置模型位置
      model.position.set(0, 0.5, 0);

      // 设置模型缩放
      model.scale.set(scale, scale, scale);

      // 添加到场景
      this.scene.add(model);
      this.sceneModels.push({
        id: modelData.id || Date.now(),
        name: modelData.name || '模型',
        mesh: model
      });

      // 添加粒子动效
      this.addParticleEffect(model.position);

      // 触发放置事件
      if (this.onModelPlaced) {
        this.onModelPlaced(modelData);
      }
    } catch (error) {
      console.error('加载模型失败:', error);

      // 加载失败时，显示默认模型
      const geometry = new THREE.BoxGeometry(1, 1, 1);
      const material = new THREE.MeshStandardMaterial({
        color: 0xff4444, // 红色表示加载失败
        opacity: 0.8,
        transparent: true
      });
      const model = new THREE.Mesh(geometry, material);

      // 设置模型位置
      model.position.set(0, 0.5, 0);

      // 设置模型缩放
      model.scale.set(scale, scale, scale);

      // 添加到场景
      this.scene.add(model);
      this.sceneModels.push({
        id: modelData.id || Date.now(),
        name: modelData.name || '模型',
        mesh: model
      });

      // 触发放置事件
      if (this.onModelPlaced) {
        this.onModelPlaced(modelData);
      }
    }
  }

  // 添加粒子动效
  addParticleEffect(position) {
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
    this.scene.add(particles);

    // 保存粒子系统引用
    this.particles.push({
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
          if (this.scene) {
            this.scene.remove(particles);
            particlesGeometry.dispose();
            particlesMaterial.dispose();
            this.particles = this.particles.filter(p => p.mesh !== particles);
          }
        })
        .start();
    }
  }

  // 鼠标事件处理
  handleMouseDown(event) {
    this.updateMousePosition(event);
    this.raycaster.setFromCamera(this.mouse, this.camera);

    // 检查是否点击了模型
    const intersects = this.raycaster.intersectObjects(
      this.sceneModels.map(m => m.mesh)
    );

    if (intersects.length > 0) {
      this.isDragging = true;
      this.draggingObject = intersects[0].object;
      this.controls.enabled = false;
    }
  }

  handleMouseMove(event) {
    if (!this.isDragging || !this.draggingObject) return;

    this.updateMousePosition(event);
    this.raycaster.setFromCamera(this.mouse, this.camera);

    // 计算拖拽平面与光线的交点 - 修复版本兼容性问题
    const ray = this.raycaster.ray;
    const intersectPoint = new THREE.Vector3();
    const intersects = ray.intersectPlane(this.draggingPlane, intersectPoint);

    if (intersects) {
      this.draggingObject.position.copy(intersectPoint);
      // 确保模型底部接触地面
      this.draggingObject.position.y = this.draggingObject.position.y + 0.5;
    }
  }

  handleMouseUp() {
    this.isDragging = false;
    this.draggingObject = null;
    this.controls.enabled = true;
  }

  handleWheel(event) {
    event.preventDefault();

    // 简单实现：根据滚轮方向调整相机位置
    const delta = event.deltaY > 0 ? 0.1 : -0.1;
    const direction = new THREE.Vector3().subVectors(this.camera.position, this.camera.target || new THREE.Vector3(0, 1, 0));
    const length = direction.length();

    if ((delta < 0 || length > 1) && (delta > 0 || length < 15)) {
      this.camera.position.addScaledVector(direction.normalize(), delta);
    }
  }

  // 更新鼠标位置
  updateMousePosition(event) {
    if (!this.container) return;

    const rect = this.container.getBoundingClientRect();

    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
  }

  // 导出场景
  exportScene(format, onExportStart = null) {
    const exportableModels = this.sceneModels.map(m => m.mesh);

    if (exportableModels.length === 0) {
      alert('场景中没有可导出的模型');
      return;
    }

    if (onExportStart) {
      onExportStart();
    }

    switch (format) {
      case 'gltf':
        this.exportAsGLTF(exportableModels);
        break;
      case 'obj':
        this.exportAsOBJ(exportableModels);
        break;
      case 'stl':
        this.exportAsSTL(exportableModels);
        break;
      default:
        this.exportAsGLTF(exportableModels);
    }
  }

  // 导出为GLTF格式
  exportAsGLTF(models) {
    const exporter = new GLTFExporter();

    exporter.parse(models, (gltf) => {
      const data = JSON.stringify(gltf);
      const blob = new Blob([data], { type: 'application/json' });
      const url = URL.createObjectURL(blob);

      this.downloadFile(url, 'scene.gltf');
    }, (error) => {
      console.error('导出GLTF失败:', error);
    });
  }

  // 导出为OBJ格式
  exportAsOBJ(models) {
    const exporter = new OBJExporter();
    const data = exporter.parse(models);

    const blob = new Blob([data], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    this.downloadFile(url, 'scene.obj');
  }

  // 导出为STL格式（简化实现）
  exportAsSTL(models) {
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

    this.downloadFile(url, 'scene.stl');
  }

  // 下载文件
  downloadFile(url, filename) {
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
  }

  // 清理场景
  cleanupScene() {
    // 取消动画帧
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }

    // 移除事件监听器
    if (this.container && this._eventHandlers) {
      this.container.removeEventListener('mousedown', this._eventHandlers.onMouseDown);
      this.container.removeEventListener('mousemove', this._eventHandlers.onMouseMove);
      this.container.removeEventListener('mouseup', this._eventHandlers.onMouseUp);
      this.container.removeEventListener('wheel', this._eventHandlers.onWheel);
      window.removeEventListener('resize', this._eventHandlers.onWindowResize);
    }

    // 清理Three.js对象
    if (this.scene) {
      // 清理所有网格
      this.scene.traverse((object) => {
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
      if (this.renderer && this.renderer.domElement && this.renderer.domElement.parentNode) {
        this.renderer.domElement.parentNode.removeChild(this.renderer.domElement);
      }
    }

    // 清空引用
    this.scene = null;
    this.camera = null;
    this.renderer = null;
    this.controls = null;
    this.roomMesh = null;
    this.sceneModels = [];
    this.particles = [];
    this.container = null;
    this._eventHandlers = null;
  }
}

export default ThreeJSManager;