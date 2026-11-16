<script setup lang="ts">
import { useToastStore } from '@/stores/useToastStore'

const toastStore = useToastStore()
</script>

<template>
  <Transition name="toast">
    <div v-if="toastStore.message" :class="['toast-container', `toast-${toastStore.type}`]">
      <span class="toast-message">{{ toastStore.message }}</span>
      <button class="toast-close" @click="toastStore.clearToast">×</button>
    </div>
  </Transition>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 280px;
  max-width: 400px;
  padding: 12px 16px;
  color: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 14px;
}

.toast-error {
  background-color: #e74c3c;
}

.toast-success {
  background-color: #2ecc71;
}

.toast-info {
  background-color: #3498db;
}

.toast-message {
  flex-grow: 1;
  margin-right: 12px;
  word-break: break-all;
}

.toast-close {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

/* アニメーション */
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>