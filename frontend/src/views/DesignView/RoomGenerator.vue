<template>
    <div class="room-generator" v-loading.fullscreen.lock="fullscreenLoading">
        <div class="imgGenerator">
            <div class="imgContainer">
                <ImageUpload @change="handleFile" placeholder="上传户型图" />
            </div>
            <el-select v-model="imageStyle" placeholder="选择户型风格" style="width: 100%; margin-top: 10px;">
                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="info" style="width: 100%; height: 30px; margin-top: 15px; border-radius: 4px;" @click="generateRoom(1)">
                生成房间
            </el-button>
        </div>
        <div class="textGenerator">
            <div class="title">
                自定义房间参数
            </div>
            <el-form :model="textForm" :rules="rules" label-width="auto" style="max-width: 600px">
                <el-form-item label="描述" prop="text">
                    <el-input v-model="textForm.text" style="width: 100%;" :rows="2" type="textarea"
                        :placeholder="placeholder" />
                </el-form-item>
                <el-form-item label="长">
                    <el-input v-model="textForm.long" :step="inputForm.step" :min="inputForm.min" :max="inputForm.max" type="number" />
                </el-form-item>
                <el-form-item label="宽">
                    <el-input v-model="textForm.width" :step="inputForm.step" :min="inputForm.min" :max="inputForm.max" type="number" />
                </el-form-item>
                <el-form-item label="高">
                    <el-input v-model="textForm.height" :step="inputForm.step" :min="inputForm.min" :max="inputForm.max" type="number" />
                </el-form-item>
                <el-form-item label="风格">
                    <el-select v-model="textForm.style" placeholder="选择风格" style="width: 100%;">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-form-item>
            </el-form>
            <el-button type="info" style="margin-top: -15px; width: 100%; height: 30px; border-radius: 4px;" @click="generateRoom(2)">
                生成房间
            </el-button>
            <div class="recommened">
                <el-link type="primary" @click="getRecommendSize">获取推荐尺寸</el-link>
            </div>
        </div>
    </div>
</template>

<script setup>
import ImageUpload from '../../component/ImageUpload.vue';
import { onMounted, reactive, ref } from 'vue';
import { getModelDimensionRecommendation, getModelStyles, generateModelFromImage, generateModel,getFormConfig } from '../../api/model';

const fullscreenLoading = ref(true);
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
    long: 5,
    width: 5,
    height: 5,
    style: ''
});

const inputForm = ref({
    step: 0.1,
    min: 0.1,
    max: 100.0,
    unit: 'm'
});

const placeholder = ref('请描述您想要生成的房间模型...');

const imageStyle = ref('');

let rawFile = null

function handleFile(file) {
  rawFile = file   // 只存不上传
}

// 校验规则
const rules = reactive({
    text: [
        { required: true, message: '请输入房间描述', trigger: 'blur' }
    ],
    long: [
        { required: false, message: '请输入长', trigger: 'blur' }
    ],
    width: [
        { required: false, message: '请输入宽', trigger: 'blur' }
    ],
    height: [
        { required: false, message: '请输入高', trigger: 'blur' }
    ],
    style: [
        { required: false, message: '请选择风格', trigger: 'change' }
    ]
});

async function initForm() {
    await getFormConfig().then(res => {
        inputForm.value.step = res.data.length.step;
        inputForm.value.min = res.data.length.min;
        inputForm.value.max = res.data.length.max;
        inputForm.value.unit = res.data.length.unit;
    }).catch(() => {
        placeholder.value = '请输入模型描述';
    });
}

// 获取推荐尺寸
function getRecommendSize() {
    getModelDimensionRecommendation().then(res => {
        textForm.long = res.data.long;
        textForm.width = res.data.width;
        textForm.height = res.data.height;
    }).catch(() => {
        ElMessage.error('获取尺寸失败');
    });
}

// 生成房间
async function generateRoom(type) {
    switch (type) {
        case 1:
            if (!rawFile) {
                ElMessage.error('请上传户型图');
                return;
            }
            if (!imageStyle.value) {
                ElMessage.error('请选择户型风格');
                return;
            }
            // 上传户型图
            const formData = new FormData();
            formData.append('image', rawFile);
            formData.append('style', imageStyle.value);
            console.log(formData)
            // 调用生成房间接口
            await generateModelFromImage(formData).then(res => {
                if (res.success === true) {
                    ElMessage.success('房间模型生成中,请稍等');
                } else {
                    ElMessage.error(res.msg || '房间生成失败');
                }
            }).catch(() => {
                ElMessage.error('房间生成失败');
            });
            break;
        case 2:
            if (!textForm.text) {
                ElMessage.error('请输入房间描述');
                return;
            }
            if (!textForm.style) {
                ElMessage.error('请选择房间风格');
                return;
            }
            // 生成房间
            await generateModel(textForm);
            ElMessage.success('房间模型生成中,请稍等');
            break;
        default:
            break;
    }
}



onMounted(() => {
    getModelStyles().then(res => {
        options.value = res.data.map(item => ({
            value: item,
            label: item
        }));
    }).catch(() => {
        ElMessage.error('获取风格失败');
    })
    initForm();
    fullscreenLoading.value = false;
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