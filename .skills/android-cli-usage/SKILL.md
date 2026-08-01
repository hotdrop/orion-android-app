---
name: android-cli-usage
description: Decide whether and how to use Android CLI for ORION. Use for current official Android guidance, SDK or emulator operations, app execution, screenshots, layout inspection, Android Studio integration, or Android-specific environment diagnosis; do not use for routine repository edits that existing code already answers.
---

# Android CLI Usage

## 利用を判断する

- AGP、Compose、Navigation、edge-to-edge、R8、Kotlinなど、最新の公式仕様が判断を左右する場合に使う。
- SDK、AVD、エミュレータ、アプリ実行、画面キャプチャ、レイアウト調査、Android Studio連携が必要な場合に使う。
- 既存コードとビルド設定だけで判断できる通常のKotlin・Compose修正には使わない。
- CLIのインストールや更新が必要な場合は、先にユーザーの承認を得る。
- コマンド自体の詳しい仕様が必要な場合は、汎用 `android-cli` スキルも読む。

## 結果を扱う

- 公式情報やCLI出力を判断材料として使い、`README.md` とプロジェクト固有ルールを上書きしない。
- 長いログやJSONをそのまま会話へ貼らず、判断に必要な結果、警告、次の行動だけを要約する。
- SDKや端末の状態に依存する結果は、確認した環境とともに報告する。
- 実行や計測が失敗した場合は、推測で成功扱いせず、失敗理由と未確認事項を明示する。
- Androidコードやビルド設定を変更した場合は `.skills/android-quality-gates/SKILL.md` に従う。

## 基本確認

- 利用可否は `command -v android` で確認する。
- バージョンは `android --version` で確認する。
- SDKと環境は、必要な場合だけ `android info` で確認する。
- sandbox外への書き込みやGUI操作が必要なら、実行前に承認を得る。
