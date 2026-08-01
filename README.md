# ORION
このアプリは、技術情報・技術ブログメモを管理するための、近未来サイバーコンソールをテーマとした Android アプリです。
このアプリは私一人しか使わないため **実用性よりもワクワクを優先する** をコンセプトにします。
具体的にはUI・アニメーション・サウンド・世界観。「未来の司令システムを操作している」と感じられることを最優先に設計します。

## コンセプト

映画やゲームに登場するコンソール画面には、多くの場合、実用性を無視した派手な演出があります。
通常の業務アプリでは避けられるような、

* ホログラムのようなUI
* サイバーな演出
* 無駄に格好いいアニメーション
* ブートシーケンス
* ターミナル風の画面遷移
* 発光するパネル
* リアルタイムログ表示

を積極的に取り入れ、「触っていて楽しい」アプリを目指します。

---

# 機能
## Incoming Intelligence 
- 今週収集した技術情報を管理します。
- Google Drive上のGoogleドキュメントのタイトル
- Google Drive上の最終更新日時
- 設定したフォルダからの相対パス

Google Driveの指定したフォルダを設定画面で選択し、画面上の手動更新を実行したときだけ、タイトル、最終更新日時、相対パスを取得します。アプリ起動時やバックグラウンドでは自動取得しません。
取得結果は端末内へ保存し、オフラインや取得失敗時も最後に取得できた一覧を表示します。
一覧の項目をタップすると、対象のGoogleドキュメントをGoogleドキュメントアプリまたはブラウザで開きます。ORION内ではドキュメント本文を表示しません。

開発環境からGoogle Driveへ接続するための設定は [`docs/GoogleDriveSetup.md`](docs/GoogleDriveSetup.md) を参照してください。

---

## Knowledge Archive

最近読んだ記事や書籍の記録です。保存する内容は以下の通りです。

* 記事タイトル
* URL
* メモ

---

# Design Principles

* Jetpack Compose First
* Material Designに縛られない
* 60FPSを維持する
* 過剰なアニメーションでも滑らかに動くこと
* ローカルファースト

---

# UI Theme

世界観は「近未来のAI研究施設」です。キーワードは以下の通りです。

* Cyberpunk
* Sci-Fi
* Tactical UI
* Hologram
* HUD
* Terminal
* Intelligence
* Mission Control

---

# Tech Stack

* Kotlin
* Jetpack Compose
* Room
* Navigation Compose
* Coroutines
* Flow
* Material 3（必要最低限）
* Canvas
* Animation
* Custom Drawing

---
