<template>
  <div class="model-generator">
    <h2>生成3D模型</h2>
    <div class="generate-form">
      <div class="form-group">
        <label>生成方式</label>
        <select v-model="generationMethod" @change="resetInputs">
          <option value="text">文本描述</option>
          <option value="image">图片上传</option>
        </select>
      </div>
      
      <div v-if="generationMethod === 'text'" class="form-group">
        <label>文本描述</label>
        <textarea v-model="prompt" placeholder="请输入3D模型的描述..." rows="3"></textarea>
      </div>
      
      <div v-else class="form-group">
        <label>图片上传</label>
        <div class="image-uploader">
          <input
            type="file"
            accept="image/*"
            @change="handleImageUpload"
            ref="fileInput"
            style="display: none"
          />
          <button type="button" @click="fileInput?.click()" class="upload-btn">
            选择图片
          </button>
          <div v-if="imagePreview" class="image-preview">
            <img :src="imagePreview" alt="预览图" />
            <button type="button" @click="removeImage" class="remove-btn">
              移除
            </button>
          </div>
        </div>
      </div>
      
      <div class="form-group">
        <label>PBR材质</label>
        <div class="checkbox-group">
          <input
            type="checkbox"
            id="enablePBR"
            v-model="enablePBR"
          />
          <label for="enablePBR">启用PBR材质</label>
        </div>
      </div>
      
      <div class="form-group">
        <label>输出格式</label>
        <select v-model="resultFormat">
          <option value="OBJ">OBJ</option>
          <option value="GLB">GLB</option>
          <option value="STL">STL</option>
        </select>
      </div>
      
      <div class="form-group">
        <button 
          type="button" 
          @click="generateModel"
          :disabled="isGenerating"
          class="generate-btn"
        >
          {{ isGenerating ? '生成中...' : '生成模型' }}
        </button>
      </div>
    </div>

    <div>已生成模型</div>
    
    <div v-if="isGenerating" class="progress-container">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      <span class="progress-text">{{ progress }}%</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['generate']);

// 响应式数据
const generationMethod = ref('text');
const prompt = ref('');
const imageFile = ref(null);
const imagePreview = ref('');
const enablePBR = ref(false);
const resultFormat = ref('OBJ');
const isGenerating = ref(false);
const progress = ref(0);

// 引用
const fileInput = ref(null);

// 重置输入
const resetInputs = () => {
  if (generationMethod.value === 'text') {
    prompt.value = '';
  } else {
    imageFile.value = null;
    imagePreview.value = '';
  }
};

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    imageFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      imagePreview.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

// 移除图片
const removeImage = () => {
  imageFile.value = null;
  imagePreview.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

// 生成模型
const generateModel = async () => {
  if (isGenerating.value) return;
  
  if (generationMethod.value === 'text' && !prompt.value.trim()) {
    alert('请输入文本描述');
    return;
  }
  
  if (generationMethod.value === 'image' && !imageFile.value) {
    alert('请上传图片');
    return;
  }
  
  isGenerating.value = true;
  progress.value = 0;
  
  // 模拟进度更新
  const progressInterval = setInterval(() => {
    progress.value += Math.random() * 10;
    if (progress.value >= 90) {
      clearInterval(progressInterval);
    }
  }, 500);
  
  try {
    // 准备参数
    const params = {
      prompt: generationMethod.value === 'text' ? prompt.value : '',
      imageFile: generationMethod.value === 'image' ? imageFile.value : null,
      enablePBR: enablePBR.value,
      resultFormat: resultFormat.value
    };
    
    // 触发生成事件
    emit('generate', params);
    
    // 模拟生成完成
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    progress.value = 100;
    
    // 重置表单
    resetInputs();
    
  } catch (error) {
    console.error('生成模型失败:', error);
    alert('生成模型失败，请重试');
  } finally {
    isGenerating.value = false;
    clearInterval(progressInterval);
    
    // 重置进度条
    setTimeout(() => {
      progress.value = 0;
    }, 1000);
  }
};
</script>

<style scoped>
.model-generator {
  width: 100%;
}

.generate-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-end;
}

.form-group {
  flex: 1 0 200px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #666;
}

input[type="number"],
select,
textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  resize: vertical;
  min-height: 80px;
}

.image-uploader {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-btn {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.upload-btn:hover {
  background-color: #e8e8e8;
}

.image-preview {
  position: relative;
  max-width: 200px;
  max-height: 200px;
  overflow: hidden;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
}

.remove-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 5px 10px;
  background-color: rgba(244, 67, 54, 0.8);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.remove-btn:hover {
  background-color: rgba(211, 47, 47, 0.9);
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.generate-btn {
  padding: 10px 20px;
  background-color: #4285f4;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.generate-btn:hover:not(:disabled) {
  background-color: #3367d6;
}

.generate-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.progress-container {
  margin-top: 20px;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #f5f5f5;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background-color: #4285f4;
  transition: width 0.3s ease;
}

.progress-text {
  display: block;
  text-align: center;
  margin-top: 5px;
  font-size: 14px;
  color: #666;
}
</style>