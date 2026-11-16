export interface AppConfig {
  message: MessageConfig
  attachment: AttachmentConfig
  storage: StorageConfig
}

export interface MessageConfig {
  maxLength: number
  pageRange: number
}

export interface AttachmentConfig {
  allowedExtensions: string[]
  maxFileCount: number
  maxFileSizeMb: number
  maxTotalSizeMb: number
}

export interface StorageConfig {
  warnThresholdPercent: number
  deleteThresholdPercent: number
}