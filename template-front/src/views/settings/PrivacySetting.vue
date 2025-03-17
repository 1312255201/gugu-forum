<script setup>

import Card from "@/components/Card.vue";
import {Lock, Setting, Switch, Tools} from "@element-plus/icons-vue";
</script>

<template>
  <div style="margin: auto ;max-width: 600px">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="隐私设置" desc="在这里可以设置你的资料可见性">
        <div class="checkbox-list">
          <el-checkbox>公开展示我的手机号</el-checkbox>
          <el-checkbox>公开展示我的电子邮件</el-checkbox>
          <el-checkbox>公开展示我的微信号</el-checkbox>
          <el-checkbox>公开展示我的QQ号</el-checkbox>
          <el-checkbox>公开展示我的性别</el-checkbox>
        </div>
      </card>
      <card style="margin: 20px 0" :icon="Tools" title="修改密码" desc="你可以在这里修改你的密码，请务必记住自己的密码">
        <el-form label-width="100" style="margin: 20px">
          <el-form-item label="当前密码" >
            <el-input :prefix-icon="Lock" placeholder="当前密码" maxlength="20"/>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input :prefix-icon="Lock" placeholder="新密码" maxlength="20"/>
          </el-form-item>
          <el-form-item label="重复新密码">
            <el-input :prefix-icon="Lock" placeholder="重新输入新密码密码" maxlength="20"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" type="success">立即重制密码</el-button>
          </div>
        </el-form>
      </card>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list{
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>