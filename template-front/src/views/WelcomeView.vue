<template>
    <div class="welcome-container">
        <div class="welcome-title">
            <div style="font-size: 30px;font-weight: bold">欢迎来到咕咕论坛</div>
            <div style="margin-top: 10px">在这里你可以和广大网友进行交流。</div>
            <div style="margin-top: 5px">快来注册体验一下吧</div>
        </div>
        <div class="center-card">
            <router-view v-slot="{ Component }" style="margin-bottom: 20px">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>

</script>

<style scoped>
.welcome-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background-image: url('/bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.center-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>
