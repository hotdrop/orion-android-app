---
name: android-quality-gates
description: Select and run risk-based verification for ORION Android changes. Use after changing Kotlin, Compose UI, resources, Gradle configuration, architecture, persistence, synchronization, or external integrations, and when deciding which tests, previews, builds, device checks, or performance measurements are required.
---

# ORION Android Quality Gates

## リスクを判定する

変更内容に必要な最小限ではなく、失敗時の影響に見合う確認を選ぶ。

| 変更 | 必須確認 | 条件付き確認 |
| --- | --- | --- |
| Markdown・コメントのみ | リンク、表記、差分 | Androidビルドは不要 |
| 純粋なロジック・変換 | 対象単体テスト、既存単体テスト | 境界値、失敗系、プロパティテスト |
| ViewModel・状態変換 | 状態遷移または抽出した純粋ロジックの単体テスト | 再試行、キャンセル、復元、Flowの順序 |
| Compose UI・Resource | Kotlinコンパイル、既存Previewまたは実行画面、主要状態 | 新規・大幅変更時のPreview追加、UIテスト、端末確認、アクセシビリティ確認 |
| Room・永続化 | DAOまたは移行テスト、既存単体テスト | アップグレード、破損、ロールバック確認 |
| Google Drive・同期 | 変換、重複、競合、部分失敗のテスト | sandbox、認証失効、再試行、オフライン確認 |
| Gradle・依存関係 | Sync相当の解決、Kotlinコンパイル、既存単体テスト | releaseビルド、R8、APKサイズ確認 |
| Canvas・モーション・音 | 実行可能なら動作確認、主要状態 | Canvas範囲拡大・ブラー・多層発光・無限アニメーション・ジャンク検知時のフレーム計測、メモリ、ライフサイクル、連打確認 |

## テストを配置する

- Model、変換、Repository、同期規則などの複雑なロジックを単体テストする。
- ViewModelの分岐が単純なら過剰なモックテストを作らず、複雑な規則を純粋ロジックへ分離してテストする。
- Roomのクエリ、制約、移行は実データベースを使うテストで確認する。
- UIテストは、重要導線、回帰リスクの高い操作、セマンティクス保証に限定する。
- UIの見た目はテストだけで保証せず、変更リスクと利用可能な環境に応じて既存Previewまたは実行画面を確認する。新規画面や大幅な変更では、可能なら両方を確認する。

## 検証を実行する

- 同一variantを触るタスクはキャッシュ競合を避けるため直列に実行する。
- Kotlin変更では、原則として `./gradlew :app:compileDebugKotlin` を先に実行する。
- 単体テスト対象がある場合は、その後に `./gradlew :app:testDebugUnitTest` を実行する。
- instrumentationや端末確認は、対象端末またはエミュレータが利用可能な場合に実行する。環境がなければ残存リスクとして報告して成果を渡し、実行確認が明示的な受け入れ条件なら環境の提供を依頼する。
- 描画性能へ影響する変更は、代表操作をリリース相当の環境で確認し、体感だけで60FPSを断定しない。
- 失敗した検証を省略して完了扱いせず、原因を修正するか、依頼範囲外の問題として証拠とともに報告する。

## 完了を報告する

- 実行したコマンドまたは確認、結果、未実施項目を簡潔に示す。
- 既存の失敗と今回の変更による失敗を区別する。
- Markdownだけの変更では、差分、リンク、スキル構文の確認を行い、Gradleを実行していないことを明示する。
