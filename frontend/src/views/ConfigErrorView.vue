<template>
  <div class="config-error">

    <h1>
      システムの設定読込に失敗しました
    </h1>

    <p>
      チャット機能は現在利用できません。
    </p>

    <p>
      画面を再読み込みしてください。
    </p>

    <button
      @click="reload"
    >
      再読み込み
    </button>

  </div>
</template>

<script setup lang="ts">

function reload() {
  window.location.reload()
}

</script>
