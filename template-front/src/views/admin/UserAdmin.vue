<script setup>
import {User} from "@element-plus/icons-vue";
import {apiUserList} from "@/net/api/user";
import {reactive, watchEffect} from "vue";
import {useStore} from "@/store";

const store = useStore()

const userTable = reactive({
  page: 1,
  size: 10,
  total: 0,
  data: []
})

watchEffect(() => apiUserList(userTable.page, userTable.size, data => {
  userTable.total = data.total
  userTable.data = data.list
}))
</script>

<template>
  <div class="user-admin">
    <div class="title">
      <el-icon><User/></el-icon>
      论坛用户列表
    </div>
    <div class="desc">
      在这里管理论坛的所有用户，包括账号信息、封禁和禁言处理。
    </div>
    <el-table :data="userTable.data" height="320">
      <el-table-column prop="id" label="编号" width="80"/>
      <el-table-column label="用户名" width="180">
        <template #default="{ row }">
          <div class="table-username">
            <el-avatar :size="30" :src="store.avatarUserUrl(row.avatar)"/>
            <div>{{ row.username }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="danger" v-if="row.role === 'admin'">管理员</el-tag>
          <el-tag v-else>普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="电子邮件"/>
      <el-table-column label="注册时间">
        <template #default="{ row }">
          {{ new Date(row.registerTime).toLocaleString() }}
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination :total="userTable.total"
                     v-model:current-page="userTable.page"
                     v-model:page-size="userTable.size"
                     layout="total, sizes, prev, pager, next, jumper"/>
    </div>
  </div>
</template>

<style scoped>
.user-admin {
  .title {
    font-weight: bold;
  }

  .desc {
    color: #bababa;
    font-size: 13px;
    margin-bottom: 20px;
  }

  .table-username {
    height: 30px;
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: right;
  }
}
</style>