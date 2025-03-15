<script setup>

import Card from "@/components/Card.vue";
import {Message, Notebook, Refresh, Select, User} from "@element-plus/icons-vue";
import {useStore} from "@/store/index.js";
import {computed} from "vue"

const store =useStore()

const registerTime =computed(()=>new Date(store.user.registerTime).toLocaleString())


</script>

<template>
  <div style="display: flex">
    <div class="setting-left">
      <card :icon="User" title="账号信息设置" desc="在这里编辑你的个人信息，你可以在隐私设置中选择是否展示这些信息">
        <el-form label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名">
            <el-input/>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group>
              <el-radio label="1">男</el-radio>
              <el-radio label="2">女</el-radio>
              <el-radio label="3">武装直升机</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="QQ号">
            <el-input/>
          </el-form-item>
          <el-form-item label="微信号">
            <el-input/>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input type="textarea" :rows="6"/>
          </el-form-item>
          <div>
            <el-button :icon="Select" type="success">保存用户信息</el-button>
          </div>
        </el-form>
      </card>
      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="你可以在这里修改你的电子邮件地址">
        <el-form>
          <el-form-item label="电子邮件">
            <el-input />
          </el-form-item>
          <el-form-item>
            <el-row style="width: 100%">
              <el-col :span="17" >
                <el-input placeholder="请输入验证码" ></el-input>
              </el-col>
              <el-col :span="1" >
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 100%" plain>获取验证码</el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>
    </div>
    <div class="setting-right">
      <div style="position: sticky;top:20px">
        <card>
          <div style="text-align: center;padding: 5px 15px 0 15px">
            <el-avatar :size="70" src="/avatar.jpg"></el-avatar>
            <div style="font-weight: bold">你好，{{store.user.username}}</div>
            <el-divider style="margin: 10px 0"></el-divider>
            <div style="font-size: 14px ;color:gray;padding: 10px" >
              能认识一下吗？我舞萌w5。我舞萌w，5，在吗？我舞萌15000。没收到？被屏蔽了？我舞萌rating 15000。我舞萌fifteen  thousand。认识一下吗？奇怪。
            </div>
          </div>
        </card>
        <card style="margin-top: 10px; font-size: 14px">
          <div>账号注册时间:{{registerTime}}</div>
          <div style="color:gray">欢迎来到我们的论坛！</div>
        </card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.setting-left{
  flex: 1;
  margin: 20px;
}
.setting-right{
  width: 300px;
  margin: 20px 30px 20px 0px;
}
</style>