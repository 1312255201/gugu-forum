<script setup>

import LightCard from "@/components/LightCard.vue";
import {Calendar, CollectionTag} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
</script>

<template>
  <div style="display: flex;margin: 20px auto;gap: 20px; max-width: 1000px">
    <div style="flex: 1">
      <light-card style="height: 2000px"/>
    </div>
    <div style="width: 280px">
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
          <Weather/>
        </light-card>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>