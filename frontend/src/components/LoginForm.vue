<template>
  <form @submit.prevent="submit">
    <div>
      <label>ユーザー名</label>
      <input type="text" v-model="username" required autofocus />
    </div>

    <div>
      <label>パスワード</label>
      <input
        :type="isText ? 'text' : 'password'"
        v-model="password"
        required
        inputmode="text"
        style="ime-mode: active;"
        autocomplete="off"
      />
    </div>

    <button type="submit">ログイン</button>
  </form>
</template>

<script setup lang="ts">
import { ref } from "vue"

const emit = defineEmits(["login"])

const username = ref("")
const password = ref("")
const isText = ref(true)


function submit() {
  emit("login", username.value, password.value)
}
</script>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
label {
  font-weight: bold;
}
button {
  padding: 8px;
}
</style>
