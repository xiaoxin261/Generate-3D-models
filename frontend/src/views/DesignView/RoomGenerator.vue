<template>
    <div class="room-generator">
        <div class="imgGenerator">
            <div class="imgContainer">
                <div class="imgUpdateBox">
                    图生
                </div>
                <div class="imgUpdateBox">
                    图生
                </div>
            </div>
            
        </div>
        <div class="textGenerator">
            <h2>房间参数</h2>
        </div>
    </div>
</template>

<script setup>
import instance from '../../utils/request';

</script>

<style scoped>
.room-generator {
    border-radius: 8px;
    .imgGenerator {
        width: 100%;
        height: 300px;
        border-radius: 8px;
        border-bottom: 1px solid #e5e5e5;
        .imgContainer{
            width: 100%;
            height: 35%;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 20px;
            .imgUpdateBox{
                width: 45%;
                height: 100%;
                border-radius: 8px;
                background-color: #e5e5e5;
                display: flex;
                justify-content: center;
                align-items: center;
            }
        }
    }
}
</style>