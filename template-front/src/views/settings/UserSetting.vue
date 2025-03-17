<script setup>

import Card from "@/components/Card.vue";
import {Message, Notebook, Refresh, Select, User} from "@element-plus/icons-vue";
import {useStore} from "@/store/index.js";
import {computed, reactive ,ref} from "vue"
import {ElMessage} from "element-plus";
import {post, get} from "@/net";

const store =useStore()

const registerTime =computed(()=>new Date(store.user.registerTime).toLocaleString())

const desc = ref('')

const baseFormRef = ref()
const emailFormRef = ref()
const baseForm = reactive({
  username: '',
  gender: '2',
  phone: '',
  qq: '',
  wechat: '',
  desc: ''
})

const emailForm =reactive({
  email: '',
  code: ''
})

const validateUsername = (_, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}
const rules = {
  username: [
    {validator: validateUsername, trigger: ['blur', 'change']},
    {min: 2, max: 20, message: '用户名的长度必须在2-20个字符之间', trigger: ['blur', 'change']},
  ], email: [
    {required: true, message: '请输入邮件地址', trigger: 'blur'},
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ]
}

const loading = reactive({
  form: true,
  base: false
})

function saveDetails() {
  baseFormRef.value.validate(isValid => {
    if (isValid) {
      loading.base=true
      post('/api/user/save-details',baseForm,()=>{
        loading.base =false
        ElMessage.success('用户信息更新成功')
        store.user.username =baseForm.username
        desc.value=baseForm.desc
      },(message)=>{
        ElMessage.warning(message)
        loading.base = false
      })
    }
  })
}

get('/api/user/details',data=>{
  baseForm.username = store.user.username
  baseForm.gender = data.gender
  baseForm.phone = data.phone
  baseForm.wechat = data.wechat
  baseForm.qq = data.qq
  baseForm.desc = data.desc
  emailForm.email = store.user.email
  desc.value = data.desc
  loading.form = false
})

const coldTime = ref(0)
const isEmailValid = ref(true)

const onValidate = (prop, isValid) => {
  if (prop === 'email')
    isEmailValid.value = isValid
}
function sendEmailCode(){
  coldTime.value = 60
  emailFormRef.value.validate(isValid=>{
    if(isValid){
      get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`,()=>{
        ElMessage.success(`验证码已成功发送到你的邮箱:${emailForm.email}，请注意查收`)
        const handle = setInterval(()=>{
          coldTime.value--
          if(coldTime.value === 0){
            clearInterval(handle)
          }
        },1000)
      },(message)=>{
        ElMessage.warning(message)
        coldTime.value = 1
      })
    }
  })
}

function modifyEmail() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      post('/api/user/modify-email',emailForm,()=>{
        ElMessage.success('邮件修改成功')
        store.user.email = emailForm.email
        emailForm.code = ''
      })
    }
  })
}


</script>

<template>
  <div style="display: flex ;max-width: 1100px;margin: auto">
    <div class="setting-left">
      <card :icon="User" title="账号信息设置" desc="在这里编辑你的个人信息，你可以在隐私设置中选择是否展示这些信息" v-loading="loading.form">
        <el-form :model="baseForm" ref="baseFormRef" :rules="rules" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="baseForm.username" maxlength="20"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="baseForm.gender">
              <el-radio :label="0">男</el-radio>
              <el-radio :label="1">女</el-radio>
              <el-radio :label="2">武装直升机</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="baseForm.phone" maxlength="11"/>
          </el-form-item>
          <el-form-item label="QQ号" prop="qq" >
            <el-input v-model="baseForm.qq" maxlength="13"/>
          </el-form-item>
          <el-form-item label="微信号" prop="wechat">
            <el-input v-model="baseForm.wechat" maxlength="30"/>
          </el-form-item>
          <el-form-item label="个人简介" prop="desc">
            <el-input v-model="baseForm.desc" type="textarea" :rows="6" maxlength="200"/>
          </el-form-item>
          <div>
            <el-button :icon="Select" @click="saveDetails" :loading="loading.base" type="success">保存用户信息</el-button>
          </div>
        </el-form>
      </card>
      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="你可以在这里修改你的电子邮件地址">
        <el-form :model="emailForm" @validate="onValidate" ref="emailFormRef" :rules="rules" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="电子邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%">
              <el-col :span="17" >
                <el-input placeholder="请输入验证码" v-model="emailForm.code"></el-input>
              </el-col>
              <el-col :span="1" >
              </el-col>
              <el-col :span="6">
                <el-button type="success" @click="sendEmailCode" :disabled="!isEmailValid || coldTime>0"
                           style="width: 100%" plain>{{
                    coldTime > 0 ? `请稍后${coldTime}`:'获取验证码'
                  }}</el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" @click="modifyEmail" type="success">更新电子邮件</el-button>
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
              {{desc|| '这个用户很懒，还没有介绍自己呢~'}}
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