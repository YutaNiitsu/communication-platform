package com.example.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app-properties")
public class AppProperties {

    private Message message;

    private Attachment attachment;

    private Storage storage;

    @Data
    public static class Message {
        private int maxLength; // 最大文字数制限
        private int pageRange; // 1回あたりに取得するメッセージ件数
    }

    @Data
    public static class Attachment {
        private String baseDir; // 添付ファイルのベースディレクトリ
        private List<String> allowedExtensions; // 許可される拡張子のリスト
        private List<String> allowedMimeTypes; // 許可されるMIMEタイプのリスト
        private int maxFileCount; // 1メッセージあたりの最大添付ファイル数
        private long maxFileSizeMb; // 1ファイルあたりの最大サイズ（MB）
        private long maxTotalSizeMb; // 1メッセージあたりの最大合計サイズ（MB）
        private Thumbnail thumbnail; // サムネイル生成の設定
        private Filename filename; // ファイル名生成の設定
    }

    @Data
    public static class Storage {
        private long totalBytes; // ストレージの総容量（バイト）
        private int warnThresholdPercent; // 警告閾値（パーセント）
        private int deleteThresholdPercent; // 削除閾値（パーセント）
    }

    @Data
    public static class Thumbnail {
        private boolean enabled;
        private int width;
        private int height;
        private String format;
        private int maxFileSizeKb;
    }

    @Data
    public static class Filename {
        private int randomLength;
        private String prefixTimestamp;
        private String charset;
    }
}
