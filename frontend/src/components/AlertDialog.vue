<template>
  <div
    v-if="visible"
    class="overlay"
  >
    <div
      class="dialog"
      :class="type"
    >
      <h3>
        {{ title }}
      </h3>

      <p>
        {{ message }}
      </p>

      <div class="actions">

        <button
          v-if="type === 'warning'"
          class="cancel-btn"
          @click="$emit('cancel')"
        >
          キャンセル
        </button>

        <button
          class="ok-btn"
          @click="$emit('confirm')"
        >
          OK
        </button>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

defineProps<{
  visible: boolean
  title: string
  message: string
  type?: "success" | "warning" | "error"
  showCancel?: boolean
}>()

defineEmits<{
  confirm: []
  cancel: []
}>()

</script>

<style scoped>

.overlay {
  position: fixed;
  inset: 0;

  display: flex;
  justify-content: center;
  align-items: center;

  background: rgba(0,0,0,0.45);

  z-index: 9999;
}

.dialog {
  width: 420px;

  padding: 24px;

  border-radius: 12px;

  background: white;

  box-shadow: 0 4px 16px rgba(0,0,0,0.2);

  color: #333333;
}

.dialog.success {
  border-top: 6px solid #4caf50;
}

.dialog.warning {
  border-top: 6px solid #ff9800;
}

.dialog.error {
  border-top: 6px solid #f44336;
}

.actions {
  margin-top: 24px;

  display: flex;
  justify-content: flex-end;

  gap: 12px;
}

.cancel-btn {
  padding: 8px 16px;
}

.ok-btn {
  padding: 8px 16px;

  border: none;

  border-radius: 4px;

  background: #1976d2;
  color: white;

  cursor: pointer;
}

</style>