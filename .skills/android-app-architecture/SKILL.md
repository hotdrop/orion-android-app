---
name: android-app-architecture
description: Design or modify ORION's Android architecture, state management, feature boundaries, persistence, Room schema, Google Drive integration, dependency injection, repositories, use cases, or module structure. Use before introducing an architectural library or changing data ownership and synchronization behavior.
---

# ORION Android Architecture

## 現状を確認する

- `README.md`、対象コード、`gradle/libs.versions.toml`、モジュール構成を先に読む。
- READMEのTech Stackは方向性として扱い、依存関係に存在しないRoom、Navigation、DIなどを導入済みとみなさない。
- 仕様がREADMEにない場合は、実装で仕様を創作せず、必要な判断をユーザーへ確認する。

## 小さく構成する

- 単一の `app` モジュールから開始し、機能単位のパッケージと明確な責務で整理する。
- モジュール分割は、独立したビルド境界、所有境界、再利用、ビルド時間などの効果を説明できる場合だけ行う。
- Compose UIを状態とイベントの入出力として設計し、単方向データフローを保つ。
- ライフサイクルを越えて状態を保持する必要がある画面ではViewModelを使い、永続化や外部SDKの詳細をComposableへ置かない。
- UseCaseは、複数画面で再利用する処理、複数データ源を調停する処理、独立して検証すべき複雑なルールにだけ導入する。
- interfaceと実装クラスを機械的に対で作らない。外部サービス境界、端末機能境界、テスト差し替えなど明確な理由がある場所だけ抽象化する。

## ローカルファーストを守る

- 構造化された永続データへRoomを導入する場合は、ローカルデータを画面表示の正本にする。
- Repositoryにローカル保存と外部連携の調停を集約し、UIからDAO、認証、Google Driveクライアントを直接呼ばない。
- Google Driveを外部データ源として扱い、認証、取得、変換、保存の境界を分離する。オフラインでも保存済み情報を閲覧・編集できる設計を優先する。
- 同期を実装する前に、同一性、重複、削除、再試行、部分失敗、更新時刻、競合の扱いをタスク内で明文化する。
- Drive由来のファイル名を初期タイトルに利用しても、ユーザーが編集したタイトルや概要を後続同期で暗黙に上書きしない。
- 失敗を握りつぶさず、再試行可能な失敗、ユーザー操作が必要な失敗、内部診断情報を分ける。

## 依存関係を管理する

- コンストラクタ注入と明示的な生成で十分な間は、DIフレームワークを追加しない。
- Hiltなどは依存グラフ、スコープ、差し替えの複雑さを実際に軽減できる段階で導入する。
- 新規ライブラリは、既存APIで解決できない理由、保守性、サイズ、性能、テスト容易性を確認してから追加する。
- アーキテクチャ変更後は `.skills/android-quality-gates/SKILL.md` に従って検証する。

## 完了条件

- UI、状態管理、永続化、外部連携の所有境界を説明できる。
- ローカルデータの正本と同期時のユーザー編集値の扱いが明確である。
- 不要なレイヤー、interface、モジュール、依存関係を増やしていない。
- エラーのユーザー表示、再試行、内部診断を混同していない。
