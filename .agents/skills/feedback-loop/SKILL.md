---
name: feedback-loop
description: Record reusable development-process improvements discovered while working on ORION. Use only when a task reveals a repeatable obstacle, missing rule, verification gap, or documentation improvement; do not create feedback merely to prove that a task was completed. Use accumulated entries when the user explicitly asks to reflect them into project rules.
---

# ORION Development Feedback

- AGENTS.mdまたはスキルのどのルールに曖昧さや実装しづらさがあったか明記する。
- タスク固有の失敗や感想は記録しない。
- 記録対象がなければ `task/Feedback.md` を作成・更新しない。

## 改善受信箱へ追記する

- `task/Feedback.md` を一時的な改善受信箱として扱い、既存内容を保持して末尾へ追記する。
- 次の形式を使い、日時は書き込み時のローカル時刻にする。

```md
# YYYY/M/D HH:mm フィードバック

## 変更・改善すべきルール
- 障壁または不足: ...
- 改善対象のルールファイル(AGENTS.mdまたはskill): ...

## 分類と反映先候補
- 分類: タスク固有 / 恒久対応候補
- 反映先候補: AGENTS.md / .agents/skills/...
```

## 明示的な依頼でルールへ反映する

- 蓄積したフィードバックの反映をユーザーが依頼したときに実施する。記録の追記だけで恒久ルールを変更しない。
- 既存の反映結果を確認し、未反映の記録を重複整理する。タスク固有の事情と再利用可能な改善を区別する。
- 共通原則はAGENTS.md、対象領域の判断や具体的な手順は関連スキルへ置き、同じ規則を重複して管理しない。
- 反映先の既存ルールと参照先を読み、AGENTS.mdとの矛盾や関連スキル間の不整合を解消する。
- `task/Feedback.md` の原文と過去の反映結果を保持し、末尾へ反映日時、元記録の識別情報、反映先、処理結果を追記する。統合、見送り、残件がある場合は理由も記す。
- 差分、参照パス、スキル構文を確認し、実行した検証と未実施の検証を報告する。
