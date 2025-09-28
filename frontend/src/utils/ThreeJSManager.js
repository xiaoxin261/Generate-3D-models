import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { DragControls } from 'three/examples/jsm/controls/DragControls.js';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js';
import { MTLLoader } from 'three/examples/jsm/loaders/MTLLoader.js';
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
    this.dragControls = null;
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

    // 新增：视角切换相关
    this.is2dMode = false; // 是否为2D俯瞰模式
    this.defaultCameraPos = new THREE.Vector3(5, 5, 5); // 3D模式默认相机位置
    this.defaultCameraTarget = new THREE.Vector3(0, 2, 0); // 3D模式默认相机目标点
    this.orthoCamera = null; // 2D模式使用的正交相机（可选，用透视相机也可实现俯瞰）

    // 新增：自动旋转漫游相关
    this.isAutoRotate = false; // 是否开启自动旋转
    this.autoRotateSpeed = 0.005; // 自动旋转速度（弧度/帧）
    this.autoRotateTimer = null; // 自动旋转定时器
    this.rotateTarget = new THREE.Vector3(0, 2, 0); // 自动旋转围绕的目标点（默认模型中心）
    // 模型列表
    this.sceneModels = [];

    this.selectedModel = null; // 存储当前选中的模型（含mesh和参数）

    // 事件回调
    this.onModelClicked = null; // 点击模型后的回调函数（外部可传入）
    this.onModelPlaced = null;
    this.onModelDragged = null;
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
    this.camera.lookAt(0, 5, 0);

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

    // // 添加坐标轴辅助线
    // const axesHelper = new THREE.AxesHelper(10);
    // this.scene.add(axesHelper);

    // 添加地面网格
    const gridHelper = new THREE.GridHelper(100, 100);
    this.scene.add(gridHelper);

    // 添加拖拽平面
    this.draggingPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);

    // 初始化DragControls
    this._initDragControls();

    // 添加其他必要的鼠标事件监听
    this._bindEvents();

    // 开始渲染循环
    this.animate();
  }

  // 初始化DragControls
  _initDragControls() {
    // 从sceneModels中获取所有mesh对象
    const draggableObjects = this.sceneModels.map(m => m.mesh);

    // 创建DragControls实例
    this.dragControls = new DragControls(draggableObjects, this.camera, this.renderer.domElement);

    // 配置DragControls
    this.dragControls.enabled = true;
    this.dragControls.ignoreRaycastObjects = [this.roomMesh];

    // 添加拖拽事件监听
    this.dragControls.addEventListener('dragstart', (event) => {
      // 当开始拖拽时禁用轨道控制器
      this.controls.enabled = false;
      this.isDragging = true;
      this.draggingObject = event.object;
    });

    this.dragControls.addEventListener('drag', (event) => {
      // 确保模型始终保持在地面上
      if (event.object) {
        const box = new THREE.Box3().setFromObject(event.object);
        const footY = box.min.y;

        // // 如果模型底部低于地面，调整它的位置
        // if (footY < 0) {
        //   event.object.position.y += Math.abs(footY);
        // }
      }

      // 触拖动事件回调
      if (this.onModelDragged) {
        this.onModelDragged(event.object);
      }
    });

    this.dragControls.addEventListener('dragend', (event) => {
      // 当拖拽结束时重新启用轨道控制器
      this.controls.enabled = true;
      this.isDragging = false;
      this.draggingObject = null;
    });
  }

  // 更新DragControls的可拖拽对象列表（支持动态添加模型）
  updateDragControls() {
    if (!this.dragControls) return;

    // 移除旧的DragControls
    this.dragControls.dispose();

    // 创建新的DragControls实例，使用更新后的模型列表
    this._initDragControls();
  }

  /**
   * 外部接口：设置模型点击后的回调函数
   * @param {Function} callback - 回调函数，参数为选中的模型信息（null表示未选中）
   */
  setOnModelClicked(callback) {
    if (typeof callback === 'function') {
      this.onModelClicked = callback;
    } else {
      console.warn('setOnModelClicked: 请传入有效的函数');
    }
  }

  // 绑定事件
  _bindEvents() {
    if (!this.container) return;

    // 我们已经在DragControls中处理了拖拽事件，这里只保留必要的其他事件
    const onWheel = (event) => this.handleWheel(event);
    const onWindowResize = () => this.handleWindowResize();

    this.container.addEventListener('wheel', onWheel);
    window.addEventListener('resize', onWindowResize);

    const onClick = (event) => this.handleModelClick(event);
    this.container.addEventListener('click', onClick);
    // 保存事件处理器引用以便后续清理
    this._eventHandlers = {
      onWheel,
      onWindowResize,
      onClick
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
  loadRoomModel(modelUrl, mtlUrl, scale = 1) {
    if (!modelUrl) return;

    // 创建房间模型数据结构
    const roomModel = {
      id: 'room-model',
      name: '房间模型',
      modelUrl: modelUrl,
      mtlUrl: mtlUrl,
      format: modelUrl.toLowerCase().includes('.obj') ? 'obj' :
        modelUrl.toLowerCase().includes('.gltf') || modelUrl.toLowerCase().includes('.glb') ? 'gltf' :
          modelUrl.toLowerCase().includes('.stl') ? 'stl' : 'obj'
    };

    console.log('加载房间模型:', roomModel);

    // 移除旧的房间模型
    if (this.roomMesh) {
      this.scene.remove(this.roomMesh);
      this.roomMesh.geometry?.dispose?.();
      this.roomMesh.material?.dispose?.();
      this.roomMesh = null;
    }

    // 调用loadAndPlaceModel加载房间模型并传递缩放参数
    this.loadAndPlaceModel(roomModel, scale);
    this.controls.target.set(0, scale / 5, 0);   // 控制“相机轨道中心”
    this.controls.update();              // 改完必须 update
  }

  // 加载并放置模型
  async loadAndPlaceModel(modelData, scale = 1) {
    try {
      // 检查是否有模型URL
      if (modelData.modelUrl) {
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


          if (modelData.mtlUrl) {
            // 如果有MTL文件，先加载材质
            const mtlLoader = new MTLLoader();
            mtlLoader.load(
              modelData.mtlUrl,
              (materials) => {
                materials.preload();
                const objLoader = new OBJLoader();
                objLoader.setMaterials(materials);
                objLoader.load(
                  modelData.modelUrl,
                  (model) => {
                    // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
                    if (modelData.id === 'room-model') {
                      model.position.set(0, 0, 0);
                      this.roomMesh = model; // 保存房间模型引用
                    } else {
                      model.position.set(5, 0, 5);
                    }

                    // 设置模型缩放
                    model.scale.set(scale, scale, scale);

                    // 添加到场景
                    this.scene.add(model);
                    if (modelData.id !== 'room-model') {
                      this.sceneModels.push({
                        id: modelData.id || Date.now(),
                        name: modelData.name || '模型',
                        mesh: model
                      });
                      // 更新DragControls以包含新模型
                      this.updateDragControls();
                    }

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
              },
              (xhr) => {
                console.log((xhr.loaded / xhr.total * 100) + '% 材质已加载');
              },
              (error) => {
                console.error('MTL材质加载失败:', error);
                // 即使材质加载失败，也尝试加载OBJ模型
                const objLoader = new OBJLoader();
                objLoader.load(
                  modelData.modelUrl,
                  (model) => {
                    // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
                    if (modelData.id === 'room-model') {
                      model.position.set(0, 0, 0);
                      this.roomMesh = model; // 保存房间模型引用
                    } else {
                      model.position.set(5, 0, 5);
                    }

                    // 设置模型缩放
                    model.scale.set(scale, scale, scale);

                    // 添加到场景
                    this.scene.add(model);
                    if (modelData.id !== 'room-model') {
                      this.sceneModels.push({
                        id: modelData.id || Date.now(),
                        name: modelData.name || '模型',
                        mesh: model
                      });
                      // 更新DragControls以包含新模型
                      this.updateDragControls();
                    }

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
              }
            );
          } else {
            // 如果没有MTL文件，直接加载OBJ模型
            const objLoader = new OBJLoader();
            objLoader.load(
              modelData.modelUrl,
              (model) => {
                // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
                if (modelData.id === 'room-model') {
                  model.position.set(0, 0, 0);
                  this.roomMesh = model; // 保存房间模型引用
                } else {
                  model.position.set(5, 0, 5);
                }

                // 设置模型缩放
                model.scale.set(scale, scale, scale);

                // 添加到场景
                this.scene.add(model);
                if (modelData.id !== 'room-model') {
                  this.sceneModels.push({
                    id: modelData.id || Date.now(),
                    name: modelData.name || '模型',
                    mesh: model
                  });
                  // 更新DragControls以包含新模型
                  this.updateDragControls();
                }

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
          }
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

          // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
          if (modelData.id === 'room-model') {
            model.position.set(0, 0.5, 0);
            this.roomMesh = model; // 保存房间模型引用
          } else {
            model.position.set(5, 0, 5);
          }

          // 设置模型缩放
          model.scale.set(scale, scale, scale);

          // 添加到场景
          this.scene.add(model);
          if (modelData.id !== 'room-model') {
            this.sceneModels.push({
              id: modelData.id || Date.now(),
              name: modelData.name || '模型',
              mesh: model
            });
            // 更新DragControls以包含新模型
            this.updateDragControls();
          }

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

          // 设置模型位置和缩放 - 房间模型在原点，物品模型在(5, 0, 5)
          if (modelData.id === 'room-model') {
            model.position.set(0, 0.5, 0);
            this.roomMesh = model; // 保存房间模型引用
          } else {
            model.position.set(5, 0, 5);
          }
          model.scale.set(scale, scale, scale); // 使用传入的缩放参数

          // 添加到场景
          this.scene.add(model);
          if (modelData.id !== 'room-model') {
            this.sceneModels.push({
              id: modelData.id || Date.now(),
              name: modelData.name || '模型',
              mesh: model
            });
            // 更新DragControls以包含新模型
            this.updateDragControls();
          }

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

      // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
      if (modelData.id === 'room-model') {
        model.position.set(0, 0.5, 0);
        this.roomMesh = model; // 保存房间模型引用
      } else {
        model.position.set(5, 0, 5);
      }

      // 设置模型缩放
      model.scale.set(scale, scale, scale);

      // 添加到场景
      this.scene.add(model);
      if (modelData.id !== 'room-model') {
        this.sceneModels.push({
          id: modelData.id || Date.now(),
          name: modelData.name || '模型',
          mesh: model
        });
        // 更新DragControls以包含新模型
        this.updateDragControls();
      }

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

      // 设置模型位置 - 房间模型在原点，物品模型在(5, 0, 5)
      if (modelData.id === 'room-model') {
        model.position.set(0, 0.5, 0);
        this.roomMesh = model; // 保存房间模型引用
      } else {
        model.position.set(5, 0, 5);
      }

      // 设置模型缩放
      model.scale.set(scale, scale, scale);

      // 添加到场景
      this.scene.add(model);
      if (modelData.id !== 'room-model') {
        this.sceneModels.push({
          id: modelData.id || Date.now(),
          name: modelData.name || '模型',
          mesh: model
        });
        // 更新DragControls以包含新模型
        this.updateDragControls();
      }

      // 触发放置事件
      if (this.onModelPlaced) {
        this.onModelPlaced(modelData);
      }
    }
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

  /**
 * 处理模型点击：检测点击的模型并计算其长宽高
 * @param {MouseEvent} event - 鼠标点击事件
 */
  handleModelClick(event) {
    if (this.isDragging || !this.scene || !this.camera) return;

    this.updateMousePosition(event);

    this.raycaster.setFromCamera(this.mouse, this.camera);

    const clickableModels = this.sceneModels.map(item => item.mesh);
    if (this.roomMesh) clickableModels.push(this.roomMesh);

    const intersects = this.raycaster.intersectObjects(clickableModels, true);
    if (intersects.length === 0) {
      this.selectedModel = null;
      if (this.onModelClicked) this.onModelClicked(null);
      return;
    }

    const clickedMesh = intersects[0].object;
    const rootMesh = this._findRootModelMesh(clickedMesh);
    const modelSize = this._calculateModelSize(rootMesh);

    const modelInfo = this.sceneModels.find(item => item.mesh === rootMesh) || {
      id: rootMesh.uuid,
      name: rootMesh.name || '未命名模型',
      mesh: rootMesh
    };

    this.selectedModel = {
      ...modelInfo,
      size: modelSize,
      position: rootMesh.position.clone(),
      rotation: rootMesh.rotation.clone(),
      scale: rootMesh.scale.clone()
    };

    // 触发外部回调：将模型参数传递给外部
    if (this.onModelClicked) {
      this.onModelClicked(this.selectedModel);
    }
  }

  /**
   * 辅助方法：找到嵌套模型的最顶层父模型（解决GLTF/OBJ模型多层子mesh问题）
   * @param {THREE.Mesh} mesh - 点击的子mesh
   * @returns {THREE.Mesh} 最顶层的父模型
   */
  _findRootModelMesh(mesh) {
    let parent = mesh;
    while (parent.parent && parent.parent.isMesh) {
      parent = parent.parent;
    }
    if (parent === this.roomMesh) return this.roomMesh;
    return parent;
  }

  /**
   * 辅助方法：计算模型的实际长宽高（考虑模型缩放）
   * @param {THREE.Mesh} mesh - 目标模型
   * @returns {Object} { width: 宽度, height: 高度, depth: 深度 }
   */
  _calculateModelSize(mesh) {
    const boundingBox = new THREE.Box3().setFromObject(mesh);

    const boxSize = new THREE.Vector3();
    boundingBox.getSize(boxSize);

    // 如果需要“模型原始尺寸”（排除缩放），则除以缩放系数
    const originalSize = new THREE.Vector3(
      boxSize.x / mesh.scale.x,
      boxSize.y / mesh.scale.y,
      boxSize.z / mesh.scale.z
    );

    return {
      width: parseFloat(boxSize.x.toFixed(2)), // 实际显示宽度（保留2位小数）
      height: parseFloat(boxSize.y.toFixed(2)), // 实际显示高度
      depth: parseFloat(boxSize.z.toFixed(2)), // 实际显示深度
      originalWidth: parseFloat(originalSize.x.toFixed(2)), // 模型原始宽度（可选）
      originalHeight: parseFloat(originalSize.y.toFixed(2)), // 模型原始高度（可选）
      originalDepth: parseFloat(originalSize.z.toFixed(2)) // 模型原始深度（可选）
    };
  }

  /**
 * 核心方法：设置当前选中模型的缩放（支持均匀/非均匀缩放）
 * @param {number|Object} scale - 缩放参数：
 *   - 若为number：均匀缩放（x=y=z=scale）
 *   - 若为Object：{x: number, y: number, z: number}，分别设置三个轴
 * @param {boolean} [isUniform=true] - 是否强制均匀缩放（可选，默认true）
 */
  setModelScale(scale, isUniform = true) {
    // 1. 校验：无选中模型时返回
    if (!this.selectedModel || !this.selectedModel.mesh) {
      console.warn('请先选中一个模型再设置缩放');
      return;
    }

    const mesh = this.selectedModel.mesh;
    let targetScale = {};

    // 2. 处理均匀缩放（传入单个数值）
    if (typeof scale === 'number') {
      // 校验：缩放值必须>0（避免模型消失或反向）
      if (scale <= 0) {
        console.error('缩放值必须大于0');
        return;
      }
      targetScale = { x: scale, y: scale, z: scale };
    }

    // 3. 处理非均匀缩放（传入{x,y,z}对象）
    else if (typeof scale === 'object' && scale.x !== undefined) {
      // 校验：每个轴的缩放值必须>0
      const valid = [scale.x, scale.y, scale.z].every(val => val > 0);
      if (!valid) {
        console.error('缩放值必须大于0');
        return;
      }
      // 若强制均匀缩放，取三个轴的平均值（可选逻辑）
      targetScale = isUniform
        ? { x: (scale.x + scale.y + scale.z) / 3, y: (scale.x + scale.y + scale.z) / 3, z: (scale.x + scale.y + scale.z) / 3 }
        : scale;
    }

    // 4. 应用缩放并更新选中模型的状态
    mesh.scale.set(targetScale.x, targetScale.y, targetScale.z);
    // 更新selectedModel中的scale（同步UI显示）
    this.selectedModel.scale.copy(mesh.scale);
    // 重新计算包围盒（确保后续点击检测的尺寸正确）
    new THREE.Box3().setFromObject(mesh);
  }

  /**
   * 核心方法：设置当前选中模型的旋转（支持角度输入，内部转弧度）
   * @param {Object} rotation - 旋转参数：{x: number, y: number, z: number}（单位：角度）
   * @param {boolean} [isAbsolute=true] - 是否“绝对设置”（true=直接设为该角度，false=在当前基础上累加）
   */
  setModelRotation(rotation, isAbsolute = true) {
    // 1. 校验：无选中模型或参数不完整时返回
    if (!this.selectedModel || !this.selectedModel.mesh || !rotation.x === undefined) {
      console.warn('请先选中模型，并传入完整的旋转参数（{x,y,z}，单位：角度）');
      return;
    }

    const mesh = this.selectedModel.mesh;
    // 2. 角度转弧度（ThreeJS旋转用弧度制）
    const radX = THREE.MathUtils.degToRad(rotation.x);
    const radY = THREE.MathUtils.degToRad(rotation.y);
    const radZ = THREE.MathUtils.degToRad(rotation.z);

    // 3. 应用旋转（绝对设置或累加）
    if (isAbsolute) {
      // 绝对设置：直接覆盖当前旋转
      mesh.rotation.set(radX, radY, radZ);
    } else {
      // 累加：在当前旋转基础上增加角度
      mesh.rotation.x += radX;
      mesh.rotation.y += radY;
      mesh.rotation.z += radZ;
    }

    // 4. 更新选中模型的状态（同步UI显示）
    this.selectedModel.rotation.copy(mesh.rotation);
    // 重新计算包围盒（确保尺寸检测正确）
    new THREE.Box3().setFromObject(mesh);
  }

  /**
   * 辅助方法：获取当前选中模型的缩放/旋转（用于UI控件初始化显示）
   * @returns {Object|null} 包含当前缩放（角度制旋转）的参数，无选中模型则返回null
   */
  getSelectedModelTransform() {
    if (!this.selectedModel || !this.selectedModel.mesh) return null;

    const mesh = this.selectedModel.mesh;
    return {
      // 缩放：直接返回当前值
      scale: {
        x: parseFloat(mesh.scale.x.toFixed(2)),
        y: parseFloat(mesh.scale.y.toFixed(2)),
        z: parseFloat(mesh.scale.z.toFixed(2))
      },
      // 旋转：转为角度制（用户友好）
      rotation: {
        x: parseFloat(THREE.MathUtils.radToDeg(mesh.rotation.x).toFixed(1)),
        y: parseFloat(THREE.MathUtils.radToDeg(mesh.rotation.y).toFixed(1)),
        z: parseFloat(THREE.MathUtils.radToDeg(mesh.rotation.z).toFixed(1))
      }
    };
  }

  /**
 * 切换2D（俯瞰）/3D（自由）视角
 * @param {boolean} is2d - true=2D俯瞰，false=3D自由视角
 */
  toggle2d3dMode(is2d) {
    if (!this.camera || !this.controls) return;

    this.is2dMode = is2d;
    const targetY = this.roomMesh?.position.y || 2; // 目标点Y轴高度（适配房间模型）

    if (is2d) {
      // 切换到2D俯瞰模式：固定相机在正上方，锁定旋转
      this.camera.position.set(0, 15, 0); // 正上方位置（高度可根据场景调整）
      this.controls.target.set(0, targetY, 0); // 目标点锁定在场景中心
      this.controls.enableRotate = false; // 禁用旋转
      this.controls.enablePan = true; // 允许平移（仅Y轴平移，模拟2D拖动）
      this.controls.panSpeed = 0.5; // 调整平移速度
      // 锁定相机视角：仅允许俯瞰（固定X轴旋转-90度，Y/Z轴旋转0）
      this.camera.rotation.set(-Math.PI / 2, 0, 0);
    } else {
      // 切换到3D自由模式：恢复默认相机位置和控制
      this.camera.position.copy(this.defaultCameraPos);
      this.controls.target.copy(this.defaultCameraTarget);
      this.controls.enableRotate = true; // 恢复旋转
      this.controls.enablePan = true; // 恢复平移
      this.controls.panSpeed = 1; // 恢复默认平移速度
    }

    // 立即更新控制器状态
    this.controls.update();
  }

  /**
   * 保存3D模式下的相机默认位置（切换回3D时恢复）
   */
  saveDefaultCameraState() {
    if (this.camera && this.controls && !this.is2dMode) {
      this.defaultCameraPos.copy(this.camera.position);
      this.defaultCameraTarget.copy(this.controls.target);
    }
  }

  /**
 * 优化：开启自动旋转时，默认以场景中心为目标
 * @param {boolean} enable - true=开启，false=关闭
 */
  toggleAutoRotate(enable) {
    if (!this.camera || !this.controls) return;

    this.isAutoRotate = enable;

    if (enable) {
      // 清除原有定时器，避免重复旋转
      if (this.autoRotateTimer) {
        cancelAnimationFrame(this.autoRotateTimer);
        this.autoRotateTimer = null;
      }
      // 启动旋转（内部会自动获取场景中心）
      this.startAutoRotate();
    } else {
      // 关闭旋转：清除定时器
      if (this.autoRotateTimer) {
        cancelAnimationFrame(this.autoRotateTimer);
        this.autoRotateTimer = null;
      }
    }
  }

  /**
 * 修复：围绕场景中心自动旋转（实时更新中心和距离）
 */
  startAutoRotate() {
    // 初始化时获取场景中心和初始相机距离（基于中心计算）
    let sceneCenter = this.getSceneCenter();
    let cameraDistance = this.camera.position.distanceTo(sceneCenter); // 相机到中心的初始距离

    // 确保距离合理（避免过近或过远，可根据场景调整范围）
    cameraDistance = Math.max(5, Math.min(20, cameraDistance));

    const rotate = () => {
      if (!this.isAutoRotate) return; // 若已关闭，停止循环

      // 实时更新场景中心（应对模型位置变化）
      sceneCenter = this.getSceneCenter();

      // 1. 计算相机新位置（围绕场景中心Y轴旋转）
      // 获取当前相机相对于中心的角度（基于X/Z轴坐标）
      const currentAngle = Math.atan2(
        this.camera.position.x - sceneCenter.x,
        this.camera.position.z - sceneCenter.z
      );
      // 角度增量（控制旋转速度）
      const newAngle = currentAngle + this.autoRotateSpeed;

      // 极坐标转直角坐标：保持距离和高度，仅旋转角度
      this.camera.position.set(
        sceneCenter.x + cameraDistance * Math.sin(newAngle), // X轴位置
        sceneCenter.y + 2, // 相机高度（高于中心2单位，可调整）
        sceneCenter.z + cameraDistance * Math.cos(newAngle)  // Z轴位置
      );

      // 2. 相机始终看向场景中心
      this.camera.lookAt(sceneCenter);
      // 同步OrbitControls的目标点（避免手动操作时偏移）
      this.controls.target.copy(sceneCenter);
      this.controls.update();

      // 循环执行旋转
      this.autoRotateTimer = requestAnimationFrame(rotate);
    };

    // 启动旋转循环
    this.autoRotateTimer = requestAnimationFrame(rotate);
  }

  /**
   * 设置自动旋转速度
   * @param {number} speed - 旋转速度（0.001~0.01 之间较合适，值越大越快）
   */
  setAutoRotateSpeed(speed) {
    this.autoRotateSpeed = Math.max(0.001, Math.min(0.01, speed)); // 限制速度范围
  }

  /**
   * 新增：计算场景中心（优先房间模型，无房间则取所有模型的几何中心）
   * @returns {THREE.Vector3} 场景中心点坐标
   */
  getSceneCenter() {
    // 1. 优先以房间模型（roomMesh）为中心
    if (this.roomMesh) {
      // 计算房间模型的包围盒，取包围盒中心
      const roomBox = new THREE.Box3().setFromObject(this.roomMesh);
      const roomCenter = new THREE.Vector3();
      roomBox.getCenter(roomCenter);
      return roomCenter;
    }

    // 2. 若无房间模型，取所有场景模型（sceneModels）的几何中心
    if (this.sceneModels.length === 0) {
      return new THREE.Vector3(0, 1, 0); // 默认中心（避免空场景报错）
    }

    // 计算所有模型的总包围盒
    const totalBox = new THREE.Box3();
    this.sceneModels.forEach(item => {
      const modelBox = new THREE.Box3().setFromObject(item.mesh);
      totalBox.expandByBox(modelBox); // 合并每个模型的包围盒
    });

    // 取总包围盒的中心
    const center = new THREE.Vector3();
    totalBox.getCenter(center);
    return center;
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

    if (this.autoRotateTimer) {
      cancelAnimationFrame(this.autoRotateTimer);
      this.autoRotateTimer = null;
    }

    // 清理DragControls
    if (this.dragControls) {
      this.dragControls.dispose();
      this.dragControls = null;
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

      // 移除点击事件监听（新增）
      if (this.container && this._eventHandlers?.onClick) {
        this.container.removeEventListener('click', this._eventHandlers.onClick);
      }

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
    this.container = null;
    this._eventHandlers = null;
  }
}

export default ThreeJSManager;