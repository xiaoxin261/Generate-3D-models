<template>
    <div class="room-generator">
        <h2>房间参数设置</h2>
        <div class="form-group">
            <label>长度 (m)</label>
            <input v-model.number="roomParams.length" type="number" min="1" max="20" step="0.5">
        </div>
        <div class="form-group">
            <label>宽度 (m)</label>
            <input v-model.number="roomParams.width" type="number" min="1" max="20" step="0.5">
        </div>
        <div class="form-group">
            <label>高度 (m)</label>
            <input v-model.number="roomParams.height" type="number" min="1" max="10" step="0.5">
        </div>
        <div class="form-group">
            <label>风格</label>
            <select v-model="roomParams.style">
                <option value="modern">现代简约</option>
                <option value="classic">经典欧式</option>
                <option value="industrial">工业风</option>
                <option value="japanese">日式</option>
            </select>
        </div>
        <button @click="generateRoom" class="generate-btn">生成房间</button>
    </div>
</template>

<script setup>
import { reactive } from 'vue';

// 房间参数
const roomParams = reactive({
    length: 5,
    width: 4,
    height: 3,
    style: 'modern'
});

// 生成房间
const generateRoom = () => {
    console.log('生成房间参数:', roomParams);
    // 这里可以添加实际的房间生成逻辑
    
};
</script>

<style scoped>
.room-generator {
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>