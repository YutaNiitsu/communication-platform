import { ref } from "vue"

export type AlertType =
  | "success"
  | "warning"
  | "error"

const visible = ref(false)
const title = ref("")
const message = ref("")
const type = ref<AlertType>("warning")

let resolver:
  ((value: boolean) => void)
  | null = null

async function show(
  dialogTitle: string,
  dialogMessage: string,
  dialogType: AlertType
): Promise<boolean> {

  title.value = dialogTitle
  message.value = dialogMessage
  type.value = dialogType

  visible.value = true

  return new Promise<boolean>((resolve) => {
    resolver = resolve
  })
}

function confirm() {

  visible.value = false

  resolver?.(true)

  resolver = null
}

function cancel() {

  visible.value = false

  resolver?.(false)

  resolver = null
}

export function useAlertDialog() {

  function showSuccess(
    title: string,
    message: string
  ) {

    return show(
      title,
      message,
      "success"
    )
  }

  function showWarning(
    title: string,
    message: string
  ) {

    return show(
      title,
      message,
      "warning"
    )
  }

  function showError(
    title: string,
    message: string
  ) {

    return show(
      title,
      message,
      "error"
    )
  }

  return {
    visible,
    title,
    message,
    type,
    confirm,
    cancel,
    showSuccess,
    showWarning,
    showError
  }
}