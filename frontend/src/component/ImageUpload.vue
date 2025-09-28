<template>
    <!-- 单图上传 -->
    <el-upload class="avatar-uploader" action="#" :show-file-list="false" :before-upload="beforeUpload"
        :http-request="dummyRequest">
        <img v-if="imageUrl" :src="imageUrl" class="avatar" />
        <span v-else>
            {{ props.placeholder }}
        </span>
    </el-upload>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

/* =====  props / emit  ===== */
const props = defineProps({
    imageUrl: { type: String, default: '' },
    placeholder: { type: String, default: '点击上传' }
})

const emit = defineEmits(['update:imageUrl', 'change'])

/* =====  内部状态  ===== */
const innerUrl = ref(props.imageUrl)

watch(() => props.imageUrl, val => { innerUrl.value = val || '' })

/* =====  校验  ===== */
const MAX_SIZE = 2 * 1024 * 1024
const WHITE_LIST = ['image/jpeg', 'image/jpg', 'image/png']

function beforeUpload(rawFile) {
    if (!WHITE_LIST.includes(rawFile.type)) {
        ElMessage.error('仅支持 JPG / PNG 格式')
        return false
    }
    if (rawFile.size > MAX_SIZE) {
        ElMessage.error('图片大小不能超过 2MB')
        return false
    }
    return true
}

/* =====  上传逻辑（只做预览）  ===== */
function dummyRequest(options) {
    const file = options.file
    const url = URL.createObjectURL(file)

    innerUrl.value = url
    emit('update:imageUrl', url) // 支持 v-model
    emit('change', file)         // 把 File 抛出去给父组件上传
    options.onSuccess(null)
}
</script>

<style scoped>
.avatar-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 100%;
    height: 100%;
}

.avatar-uploader:hover {
    border-color: #409eff;
}

.avatar {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    text-align: center;
}
</style>