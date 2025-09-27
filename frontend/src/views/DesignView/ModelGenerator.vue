<template>
    <div class="room-generator">
        <div class="imgGenerator">
            <div class="imgContainer">
                <ImageUpload @change="handleFile" placeholder="上传模型图" />
            </div>
            <el-select v-model="value" placeholder="选择风格" style="width: 100%; margin-top: 10px;">
                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="info" style="width: 100%; height: 30px; margin-top: 15px; border-radius: 4px;">
                生成模型
            </el-button>
        </div>
        <div class="textGenerator">
            <div class="title">
                自定义模型参数
            </div>
            <el-form :model="textForm" label-width="auto" style="max-width: 600px">
                <el-form-item label="描述" prop="text">
                    <el-input v-model="textForm.text" style="width: 100%;" :rows="2" type="textarea"
                        placeholder="请输入想要的模型描述" />
                </el-form-item>
                <el-form-item label="长(米)">
                    <el-input v-model="textForm.long" type="number" />
                </el-form-item>
                <el-form-item label="宽(米)">
                    <el-input v-model="textForm.width" type="number" />
                </el-form-item>
                <el-form-item label="高(米)">
                    <el-input v-model="textForm.height" type="number" />
                </el-form-item>
                <el-form-item label="风格">
                    <el-select v-model="textForm.style" placeholder="选择风格" style="width: 100%;">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-form-item>
            </el-form>
            <el-button type="info" style="margin-top: -15px; width: 100%; height: 30px; border-radius: 4px;">
                生成模型
            </el-button>
            <div class="recommened">
                <el-link type="primary">获取推荐尺寸</el-link>
            </div>
        </div>
    </div>
</template>

<script setup>
import instance from '../../utils/request';
import { onMounted, reactive, ref } from 'vue';
import ImageUpload from '../../component/ImageUpload.vue';

const value = ref('');
const options = ref([
    {
        value: 'option1',
        label: 'Option 1'
    },
    {
        value: 'option2',
        label: 'Option 2'
    },
    {
        value: 'option3',
        label: 'Option 3'
    }
]);

const textForm = reactive({
    text: '',
    long: 0,
    width: 0,
    height: 0,
    style: ''
});

onMounted(() => {
    instance.get('/api/room/getRoomType').then(res => {
        if (res.data.code === 200) {
            options.value = res.data.data.map(item => ({
                value: item,
                label: item
            }));
        }
    });
});

</script>

<style scoped lang="less">
.room-generator {
    border-radius: 8px;

    .imgGenerator {
        width: 100%;
        height: 240px;
        border-radius: 8px;
        border-bottom: 3px solid #e5e5e5;

        .imgContainer {
            width: 100%;
            height: 120px;
            background-color: #e5e5e5;
            text-align: center;
            line-height: 120px;
            border-radius: 8px;
        }
    }

    .textGenerator {
        width: 100%;
        height: 160px;
        border-radius: 8px;
        padding-top: 15px;
        text-align: center;
        display: flex;
        flex-direction: column;
        justify-content: start;
        align-items: center;
        gap: 20px;
        .recommened {
            margin-top: -10px;
            width: 100%;
            height: 30px;
            border-radius: 4px;
            display: flex;
            justify-content: right;
        }
    }
}
</style>