<script setup>

import LightCard from "@/components/LightCard.vue";
import {Calendar, CollectionTag, EditPen} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {computed, reactive, ref,onMounted} from "vue";
import {get} from "@/net"
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";

const weather = reactive({
  location:{},
  now: {},
  hourly: [],
  success: false
})

const editor = ref(false)

const today = computed(()=>{
  const date = new Date()
  return `${date.getFullYear()} 年 ${date.getMonth() + 1} 月 ${date.getDate()} 日`
})
const ipAddr = ref("");

onMounted(() => {
  get("/api/util/ip", (data) => {
    ipAddr.value = data;
  });
});


navigator.geolocation.getCurrentPosition(position => {
  const longitude = position.coords.longitude
  const latitude = position.coords.latitude
  get(`/api/forum/weather?longitude=${longitude}&latitude=${latitude}`,data=>{
    Object.assign(weather,data)
    weather.success = true
  } , error =>{
    ElMessage.error('位置信息获取超时，请检测网络设置')
    get(`/api/forum/weather?longitude=116.40529&latitude=39.90499`,data=>{
      Object.assign(weather,data)
      weather.success = true
    })
  }
  ,{
    timeout: 3000,
    enableHighAccuracy: true
  })
})

</script>

<template>
  <div style="display: flex;margin: 20px auto;gap: 20px; max-width: 1000px">
    <div style="flex: 1">
      <light-card>
        <div class="create-topic" @click="editor=true">
          <el-icon><edit-pen/></el-icon> 点击发表主题...
        </div>
      </light-card>
      <light-card style="margin-top: 10px ; height: 30px">

      </light-card>
      <div style="margin-top: 10px;display: flex;flex-direction: column;gap:10px">
        <light-card style="height: 150px"  v-for="item in 10">

        </light-card>
      </div>
    </div>
    <div style="width: 300px">
      <div style="position: sticky;top: 20px">
        <light-card>
          <div style="font-weight: bold">
            <!--不加会偏移笑死Lmao-->
            <el-icon><CollectionTag style="translate: 0 2px"/></el-icon>
            论坛公告
          </div>
          <el-divider style="margin: 10px 0"/>
          <div style="font-size: 14px;margin: 10px;color:grey">
            请大家文明交流，请勿发送违法不实信息，网站管理员将会不时审查网站内容，存在违规信息将会对你的账号进行封禁
          </div>
        </light-card>
        <light-card style="margin-top: 10px">
          <div style="font-weight: bold">
            <!--不加会偏移笑死Lmao-->
            <el-icon><Calendar style="translate: 0 2px"/></el-icon>
            天气信息
          </div>
          <el-divider style="margin: 10px 0"/>
          <Weather :data="weather "/>
        </light-card>
        <light-card style="margin-top: 10px">
          <div class="info-text">
            <div>当前日期</div>
            <div>{{today}}</div>
          </div>
          <div class="info-text">
            <div>当前IP地址:</div>
            <div>{{ipAddr}}</div>
          </div>
        </light-card>
        <div style="font-size: 14px;margin-top: 10px;color: grey">
          <el-icon><Link/></el-icon>
          友情链接
          <el-divider style="margin: 10px 0"/>
        </div>
        <div style="display: grid;grid-template-columns: repeat(2,1fr);grid-gap: 10px;margin-top: 10px">
          <div class="friend-link">
            <a href="https://si.gugufish.cn">
              <el-image style="height: 100%" src="friend.gif"/>
            </a>
          </div>
          <div class="friend-link">
            <a href="https://mit-sec.top/">
              <el-image style="height: 100%" src="friendMIT.png"/>
            </a>
          </div>
          <div class="friend-link">
            <a href="https://si.gugufish.cn">
              <el-image style="height: 100%" src="friend.gif"/>
            </a>
          </div>
          <div class="friend-link">
            <a href="https://si.gugufish.cn">
              <el-image style="height: 100%" src="friend.gif"/>
            </a>
          </div>
        </div>
      </div>
    </div>
    <topic-editor :show="editor" @success="editor = false" @close="editor = false"></topic-editor>
  </div>
</template>

<style lang="less" scoped>
.info-text{
  display: flex;
  justify-content: space-between;
  color: grey;
  font-size: 14px
}
.friend-link{
  border-radius: 5px;
  overflow: hidden;
}
.create-topic{
  background-color: #efefef;
  border-radius: 5px;
  height: 40px;
  font-size: 14px;
  color: grey;
  line-height: 40px;
  &:hover {
    cursor: pointer;
  }
}
.dark .create-topic {
  background-color: #232323;
}
</style>