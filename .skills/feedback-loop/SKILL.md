---
name: feedback-loop
description: Record reusable development-process improvements discovered while working on ORION. Use only when a task reveals a repeatable obstacle, missing rule, verification gap, or documentation improvement; do not create feedback merely to prove that a task was completed. Use accumulated entries when the user explicitly asks to reflect them into project rules.
---

# ORION Development Feedback

## 記録を判断する

- 同種タスクでも再発し得る障壁、欠けた検証、曖昧なルール、改善可能な手順を発見した場合だけ記録する。
- タスク固有の失敗や感想だけなら記録しない。
- 記録対象がなければ `task/Feedback.md` を作成・更新しない。

## 改善受信箱へ追記する

- `task/Feedback.md` を一時的な改善受信箱として扱い、既存内容を保持して末尾へ追記する。
- 次の形式を使い、日時は書き込み時のローカル時刻にする。

```md
# YYYY/M/D HH:mm フィードバック

## 作業内容
- 何を変更したか

## 再利用可能な改善点
- 障壁または不足: ...
- 改善案: ...
- 根拠: ...

## 分類と反映先候補
- 分類: タスク固有 / 恒久対応候補
- 反映先候補: AGENTS.md / .skills/... / docs / tests / その他
```

- 完了報告では記録した事実だけを短く伝え、本文を重複して転載しない。

## ルールへ反映する

- ユーザーが明示的に反映を依頼した場合だけ、全エントリを精査してルール、ドキュメント、テストへ移す。
- 一時的な事情を恒久ルールへ昇格させず、同じ内容を複数ファイルへ重複させない。
- 反映済み内容を確認してから `task/Feedback.md` を空にし、未反映項目があれば残す。
