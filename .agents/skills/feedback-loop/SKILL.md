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