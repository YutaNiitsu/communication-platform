# communication-platform

## 開発環境

### バックエンド環境

本バックエンドアプリケーション（`app`）の動作環境および使用ライブラリの一覧です。

#### 基本実行環境・フレームワーク
| 項目            | テクノロジー / バージョン |
|:----------------|:---------------|
| **Java**        | 17             |
| **Spring Boot** | 4.1.0          |
| **ビルドツール**      | Apache Maven   |

#### データベース
| 項目                  | 依存関係                    | スコープ    |
|:----------------------|:----------------------------|:--------|
| **PostgreSQL Driver** | `org.postgresql:postgresql` | runtime |

#### 主な依存ライブラリ・モジュール
| 分類                          | グループID (`groupId`)           | アーティファクトID (`artifactId`)        | バージョン / スコープ     |
|:------------------------------|:-----------------------------|:---------------------------------|:-----------------|
| **Web / API**                 | `org.springframework.boot`   | `spring-boot-starter-webmvc`     | -                |
| **データ連携**                   | `org.springframework.boot`   | `spring-boot-starter-data-jpa`   | -                |
| **セキュリティ**                    | `org.springframework.boot`   | `spring-boot-starter-security`   | -                |
| **入力検証**                  | `org.springframework.boot`   | `spring-boot-starter-validation` | -                |
| **リアルタイム通信**                | `org.springframework.boot`   | `spring-boot-starter-websocket`  | -                |
| **WebSocket**                 | `org.springframework`        | `spring-websocket`               | -                |
| **JSON処理**                  | `com.fasterxml.jackson.core` | `jackson-databind`               | -                |
| **認証 (JWT API)**            | `io.jsonwebtoken`            | `jjwt-api`                       | 0.11.5           |
| **認証 (JWT Implementation)** | `io.jsonwebtoken`            | `jjwt-impl`                      | 0.11.5 (runtime) |
| **認証 (JWT Jackson)**        | `io.jsonwebtoken`            | `jjwt-jackson`                   | 0.11.5 (runtime) |
| **コード生成補助**               | `org.projectlombok`          | `lombok`                         | optional         |

#### テストライブラリ
| 依存関係                              | スコープ |
|:--------------------------------------|:-----|
| `spring-boot-starter-webmvc-test`     | test |
| `spring-boot-starter-data-jpa-test`   | test |
| `spring-boot-starter-security-test`   | test |
| `spring-boot-starter-validation-test` | test |

---

### フロントエンド環境

本フロントエンドアプリケーション（`frontend`）の動作環境および使用ライブラリの一覧です。

#### 基本実行環境・ビルドツール
| 項目            | テクノロジー / バージョン                 |
|:----------------|:-------------------------------|
| **フレームワーク**     | Vue 3 (`^3.5.40`)              |
| **開発・ビルドツール** | Vite (`^8.2.0`)                |
| **言語**        | TypeScript (`~6.0.2`)          |
| **型チェックツール**   | vue-tsc (`^3.3.8`)             |
| **モジュール形式**   | ES Module (`"type": "module"`) |

#### 主な依存ライブラリ（`dependencies`）
| 分類                    | パッケージ名 (`package`) | バージョン    |
|:------------------------|:--------------------|:---------|
| **UIフレームワーク**           | `vue`               | ^3.5.40  |
| **ルーティング**              | `vue-router`        | ^5.2.0   |
| **状態管理**            | `pinia`             | ^4.0.2   |
| **HTTP通信**            | `axios`             | ^1.19.0  |
| **日時処理**            | `dayjs`             | ^1.11.21 |
| **リアルタイム通信 (STOMP)**  | `@stomp/stompjs`    | ^7.3.0   |
| **リアルタイム通信 (SockJS)** | `sockjs-client`     | ^1.6.1   |

#### 開発・デザイン依存関係（`devDependencies`）
| 分類                    | パッケージ名 (`package`)  | バージョン    |
|:------------------------|:---------------------|:---------|
| **Vite Plugin**         | `@vitejs/plugin-vue` | ^6.0.8   |
| **CSSフレームワーク**          | `tailwindcss`        | ^4.3.3   |
| **CSS処理**             | `postcss`            | ^8.5.25  |
| **CSSベンダープレフィックス**      | `autoprefixer`       | ^10.5.4  |
| **TypeScript 設定基盤** | `@vue/tsconfig`      | ^0.9.1   |
| **Node.js 型定義**      | `@types/node`        | ^24.13.3 |

#### 定義されているスクリプト（`scripts`）
| コマンド              | 実行内容                   | 役割                       |
|:------------------|:---------------------------|:---------------------------|
| `npm run dev`     | `vite`                     | 開発用ローカルサーバーの起動        |
| `npm run build`   | `vue-tsc -b && vite build` | 型チェックおよび本番用ビルドの実行    |
| `npm run preview` | `vite preview`             | ビルド済み成果物のローカルプレビュー確認 |

## 起動手順

本システムをローカル開発環境で起動するための手順です。

### 1. 前提条件 (Prerequisites)

* Docker / Docker Desktop がインストールされ、起動していること
* Java 17 以上 および Maven がインストールされていること
* Node.js（v18以上推奨）および npm がインストールされていること

---

### 2. データベース (PostgreSQL) の起動

`communication-platform/infra` ディレクトリに移動し、Docker Compose を使用して PostgreSQL コンテナを起動します。

```bash
# infra ディレクトリに移動
cd communication-platform/infra

# PostgreSQL コンテナの起動
docker compose up -d postgres
```

> **補足:** 初回起動時に `initdb/` ディレクトリ内の SQL スクリプト（`01_schema.sql`, `02_master_data.sql`, `03_test_users.sql` など）が自動適用され、テーブル作成および初期データが投入されます。  
> コンテナの起動・ヘルスチェック状態は以下のコマンドで確認できます。
> ```bash
> docker ps
> ```

---

### 3. バックエンド (`app`) の起動

Spring Boot アプリケーションを起動します。

```bash
# app ディレクトリに移動
cd ../app

# 依存関係の解決とアプリケーションの起動
mvn spring-boot:run
又は、環境変数も同時に指定をする場合
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DJWT_SECRET=my-super-secret-key-256bit-long1234"
```

* **起動完了の確認:** ターミナルに `Started AppServerApplication in ... seconds` のログが表示されれば、`http://localhost:8080` でバックエンドが受け付け可能な状態になります。

---

### 4. フロントエンド (`frontend`) の起動

Vue 3 + Vite アプリケーションを起動します。

```bash
# 別ターミナルを開き、frontend ディレクトリに移動
cd communication-platform/frontend

# パッケージのインストール（初回および依存変更時のみ）
npm install

# 開発用ローカルサーバーの起動
npm run dev
```

---

### 5. アプリケーションへのアクセス

各種サービスが起動したら、ブラウザで以下の URL にアクセスしてください。

* **フロントエンド画面 (Web Application):**
  * **URL:** `http://localhost:5173/` （または Vite 起動時にターミナルに表示される URL）
* **バックエンド API / WebSocket (REST API & WebSocket Endpoint):**
  * **REST API Root:** `http://localhost:8080/`
  * **WebSocket Endpoint:** `ws://localhost:8080/ws/chat`

---

### 6. 初期ログイン情報（テスト用）

初期データとして以下のユーザーが登録されています。ログイン画面（`LoginView`）にて使用可能です。

| ユーザー名 (`username`) | 役割 (`role`) | パスワード (`password`) |
|:--------------------|:--------------|:-------------------|
| `admin_user`        | admin         | `password`         |
| `test_user1`        | user          | `password`         |

--- 

## 機能一覧

### 1. ログイン・ユーザー管理

| 機能名                      | 説明                                                  |
|:----------------------------|:------------------------------------------------------|
| **ユーザー登録**                | 画面から新しいユーザーのアカウント（ユーザー名・パスワード・権限）を作成します。      |
| **ログイン / ログアウト**            | ユーザー名とパスワードを入力してログインし、安全に利用を開始・終了します。      |
| **権限管理 (admin / user)** | 管理者（admin）と一般ユーザー（user）の役割に応じた操作制限を行います。 |
| **アカウント有効/無効化制御**    | 有効/無効ステータスを設定し、無効化されたアカウントのログインを拒否します。     |

---

### 2. チャンネル機能

| 機能名              | 説明                                               |
|:--------------------|:---------------------------------------------------|
| **チャンネル一覧表示**   | 作成されているチャンネル（案件・研修・タスクごとのルーム）の一覧を表示します。  |
| **チャンネル作成**       | 目的に応じた新しいチャンネルを作成します（作成者は自動的に参加します）。 |
| **チャンネル詳細表示**   | 選択したチャンネル内のメッセージや参加者情報を表示します。            |
| **チャンネル参加者管理** | チャンネルに参加しているメンバーの一覧を表示・管理します。              |

---

### 3. メッセージ投稿機能

| 機能名                     | 説明                                                      |
|:---------------------------|:----------------------------------------------------------|
| **メッセージ投稿**              | チャンネル内にテキストメッセージを投稿します。                                |
| **投稿情報表示**           | メッセージと一緒に「投稿者名」「投稿日時」「編集日時」を表示します。        |
| **メッセージ編集**              | 送信済みのメッセージ内容を後から修正します。                            |
| **メッセージ削除 (論理削除)**   | メッセージを削除します（データは保持したまま非表示にする論理削除）。             |
| **リアルタイム反映 (WebSocket)** | 画面を更新しなくても、他のユーザーの投稿・編集・削除が即座に画面に反映されます。 |

---

### 4. 検索機能

| 機能名           | 説明                                            |
|:-----------------|:------------------------------------------------|
| **キーワード検索**    | メッセージ内のテキストを対象にキーワードで検索します。                |
| **チャンネル絞り込み**  | 検索対象を特定のチャンネル内に限定して検索します。            |
| **投稿者絞り込み** | 特定のユーザーが投稿したメッセージに絞り込んで検索します。           |
| **日付範囲検索** | 指定した期間（開始日～終了日）に投稿されたメッセージを検索します。 |