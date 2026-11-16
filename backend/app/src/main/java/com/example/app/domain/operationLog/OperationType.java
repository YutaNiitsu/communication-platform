package com.example.app.domain.operationLog;

public enum OperationType {

    // セッション関連
    login,          // ユーザーがログインに成功
    logout,         // ユーザーがログアウト

    // ユーザー関連
    user_created,    // 新しいユーザーが作成された
    user_updated,    // ユーザー情報が更新された
    user_deleted,    // ユーザーが削除された

    // チャンネル関連
    channel_created, // 新しいチャンネルが作成された
    channel_updated, // チャンネル情報が更新された
    channel_join,   // ユーザーがチャンネルに参加
    channel_leave,  // ユーザーがチャンネルから退出
    channel_deleted, // チャンネルが削除された

    // メッセージ関連
    message_created, // 新しいメッセージが投稿された
    message_updated, // メッセージが編集された
    message_deleted, // メッセージが削除された
    mention_created,   // メッセージにメンションが追加された
    mention_removed, // メッセージからメンションが削除された
    reaction_added,  // メッセージにリアクションが追加された
    reaction_removed,// メッセージからリアクションが削除された

    // メッセージテンプレート関連
    message_template_created, // 新しいメッセージテンプレートが作成された
    message_template_updated, // メッセージテンプレートが更新された
    message_template_deleted, // メッセージテンプレートが削除された
    message_template_category_created, // 新しいメッセージテンプレートカテゴリが作成された
    message_template_category_updated, // メッセージテンプレートカテゴリが更新された
    message_template_category_deleted, // メッセージテンプレートカテゴリが削除された

    // 検索関連
    search,          // ユーザーが検索操作を実行

    // 添付ファイル関連
    attachment_added, // 添付ファイルが追加された
    attachment_removed // 添付ファイルが削除された
}
