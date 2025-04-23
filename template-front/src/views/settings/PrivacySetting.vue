<script setup>

import Card from "@/components/Card.vue";
import {Lock, Setting, Switch, Tools} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {onMounted, reactive, ref} from "vue";
import {apiUserChangePassword, apiUserPrivacy, apiUserPrivacySave} from "@/net/api/user";


const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: ''
})

const validatePassword = (_, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
const rules ={
  password:[
    {required:true, message:'请输入原来的密码',trigger:'blur'}
  ],
  new_password:[
    {required:true, message:'请输入新的的密码',trigger:'blur'},
    { min: 6, max: 20, message: '密码的长度必须在6-20个字符之间', trigger: ['blur', 'change'] }
  ],
  new_password_repeat:[
    {required:true, message:'请再吃输入新的的密码',trigger:'blur'},
    {validator: validatePassword, trigger: ['blur', 'change'] }
  ]
}

const formRef = ref()
const valid = ref(false)
const onValidate = (prop,isValid)=>valid.value = isValid

function resetPassword(){
  formRef.value.validate(valid =>{
    if(valid){
      apiUserChangePassword(form, () => {
        ElMessage.success("修改密码成功")
        formRef.value.resetFields();
      })
    }
  })
}

const saving = ref(true)

const privacy = reactive({
  phone: false,
  wechat:false,
  qq: false,
  email: false,
  gender: false
})

function savePrivacy(type,status){
  apiUserPrivacySave({ type, status }, saving)
}

onMounted(() => {
  apiUserPrivacy(data => {
    Object.assign(privacy, data)
    saving.value = false
  })
})
</script>
<template>
  <div style="margin: auto ;max-width: 600px">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="隐私设置" desc="在这里可以设置你的资料可见性" v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="savePrivacy('phone',privacy.phone)" v-model="privacy.phone">公开展示我的手机号</el-checkbox>
          <el-checkbox @change="savePrivacy('email',privacy.email)" v-model="privacy.email">公开展示我的电子邮件</el-checkbox>
          <el-checkbox @change="savePrivacy('wechat',privacy.wechat)" v-model="privacy.wechat">公开展示我的微信号</el-checkbox>
          <el-checkbox @change="savePrivacy('qq',privacy.qq)" v-model="privacy.qq">公开展示我的QQ号</el-checkbox>
          <el-checkbox @change="savePrivacy('gender',privacy.gender)" v-model="privacy.gender">公开展示我的性别</el-checkbox>
        </div>
      </card>
      <card style="margin: 20px 0" :icon="Tools" title="修改密码" desc="你可以在这里修改你的密码，请务必记住自己的密码">
        <el-form :rules="rules" :model="form" ref="formRef" @validate="onValidate" label-width="100" style="margin: 20px">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.password" placeholder="当前密码" maxlength="20"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password" placeholder="新密码" maxlength="20"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat" placeholder="重新输入新密码密码" maxlength="20"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" type="success" @click="resetPassword">立即重制密码</el-button>
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