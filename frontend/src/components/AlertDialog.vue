<template>
  <div
    v-if="visible"
    class="overlay"
  >
    <div class="dialog">
      <h3>{{ title }}</h3>

      <p>{{ message }}</p>

      <div class="actions">

        <button
          class="cancel-btn"
          @click="cancel"
        >
          キャンセル
        </button>

        <button
          class="ok-btn"
          @click="confirm"
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
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

function confirm() {
  emit("confirm")
}

function cancel() {
  emit("cancel")
}

</script>

<style scoped>

.overlay {
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.4);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 9999;
}

.dialog {
  width: 400px;

  background: white;

  border-radius: 8px;

  padding: 24px;

  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.dialog h3 {
  margin-bottom: 16px;
}

.dialog p {
  white-space: pre-line;
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

  background: #1976d2;
  color: white;

  border: none;
  border-radius: 4px;
}

</style>