<template>
  <div
    class="info-overlay"
    @click="close"
  >
    <div
      class="info-dialog"
      @click.stop
    >

      <div class="header">
        <h3>添付ファイル情報</h3>

        <button @click="close">
          ×
        </button>
      </div>

      <table>
        <tbody>

          <tr>
            <th>ファイル名</th>
            <td>
              {{ attachment.originalFileName }}
            </td>
          </tr>

          <tr>
            <th>ファイル種別</th>
            <td>
              {{ attachment.fileType }}
            </td>
          </tr>

          <tr>
            <th>サイズ</th>
            <td>
              {{ formatSize(attachment.fileSize) }}
            </td>
          </tr>

          <tr>
            <th>作成日時</th>
            <td>
              {{ attachment.createdAt }}
            </td>
          </tr>

        </tbody>
      </table>

    </div>
  </div>
</template>

<script setup lang="ts">

const props = defineProps<{
  attachment: any
}>()

const emit =
  defineEmits(["close"])

function close() {
  emit("close")
}

function formatSize(
  bytes: number
) {

  if (bytes < 1024) {
    return `${bytes} B`
  }

  if (bytes < 1024 * 1024) {
    return `${(
      bytes / 1024
    ).toFixed(2)} KB`
  }

  return `${(
    bytes /
    1024 /
    1024
  ).toFixed(2)} MB`
}

</script>

<style scoped>

.info-overlay {
  position: fixed;
  inset: 0;

  background: rgba(
    0,
    0,
    0,
    0.4
  );

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 3000;
}

.info-dialog {
  width: 600px;

  background: white;

  border-radius: 8px;

  padding: 20px;

  color: #333333;
}

.header {
  display: flex;

  justify-content: space-between;

  margin-bottom: 16px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border: 1px solid #ddd;

  padding: 8px;

  text-align: left;

  color: #333333;
}

th {
  width: 150px;
  background: #f5f5f5;
}

</style>