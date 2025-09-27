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
import { ref } from 'vue'
const props = defineProps({
    placeholder: {
        type: String,
        default: '点击上传'
    }
})

// 预览地址
const imageUrl = ref('')

// 大小限制 2MB
const MAX_SIZE = 2 * 1024 * 1024

// 格式白名单
const WHITE_LIST = ['image/jpeg', 'image/jpg', 'image/png']

const emit = defineEmits(['update:modelValue'])

// 校验
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

// 覆盖默认上传：只做预览，不真正发请求
function dummyRequest(options) {
    const file = options.file
    // 生成本地预览地址
    imageUrl.value = URL.createObjectURL(file)
    // 这里拿到原始 File，你可以 emit 给父组件，或者稍后一起 POST
    console.log('拿到 File 对象：', file)
    // 必须调用 resolve，否则 el-upload 内部会卡住
    options.onSuccess()
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