<script setup>
defineProps({
  name: String,
  checkName: String,
  color: String,
  check: Boolean
})

const emit = defineEmits(['check'])
</script>

<template>
  <div class="interact-button" @click="emit('check')">
    <span class="icon" :style="{'color': check ? color : 'unset'}">
      <!--    预留插槽放置图标      -->
      <slot/>
    </span>
    <span class="name" :style="{'color': color}">
      {{check ? checkName : name}}
    </span>
  </div>
</template>

<style scoped>
.interact-button {
  display: inline-block;
  height: 20px;
  transition: .3s;
  vertical-align: middle;

  .name {
    font-size: 13px;
    margin-left: 5px;
    opacity: 0.7;
  }
  &:hover {
    cursor: pointer;
    font-size: 18px;
  }
}
</style>