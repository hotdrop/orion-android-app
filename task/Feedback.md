# 2026/8/1 18:00 フィードバック

## 作業内容
- Knowledge Archive、Knowledge Archive Editor、Settings、Incoming IntelligenceのCompose UIを、Route、Screen、画面固有componentの責務に分離した。
- componentごとのPreviewと代表状態を追加し、同期ステータスの優先順位を純粋ロジックと単体テストへ抽出した。

## 再利用可能な改善点
- 障壁または不足: Route、Screen、画面内の複数componentを1ファイルに集約すると、ViewModel接続、副作用、状態分岐、描画の変更理由が混在し、小さなUI変更でも影響範囲を判断しにくくなる。
- 改善案: Routeは専用ファイルでViewModel、Context、BackHandler、イベント収集を所有し、ScreenはUiStateの分解とcomponent配置に限定する。componentは画面別パッケージまたは画面名付きの名前で所有者を明示する。
- 根拠: Knowledge ArchiveでRouteを分離し、`components/editor`と`components/list`に整理した結果、ScreenからViewModel接続と外部URL起動が消え、各componentの利用画面をパスと名前で判別できた。

- 障壁または不足: componentへUiStateやドメインモデルを丸ごと渡すと、表示に不要な状態への依存が増え、再利用性と変更安全性を失う。
- 改善案: ScreenをUiStateを解釈する境界とし、componentには表示と操作に必要なプリミティブ値、表示用プロパティ、コールバックだけを渡す。複雑な状態優先順位はPresentation変換に抽出する。
- 根拠: SettingsとIncoming IntelligenceのcomponentsからUiState依存を排除し、カードへのエンティティ丸ごと渡しも表示フィールド単位へ変更した後も、既存UIテストソースはコンパイルに成功した。

- 障壁または不足: `AlertDialog`など別ウィンドウに描画するComposableは、通常のPreviewで表示内容を確認できない場合がある。
- 改善案: Dialogシェルと本体contentを分離し、実行時とPreviewの両方から同一content Composableを利用する。
- 根拠: Knowledge Archive Editorの確認ダイアログは、Dialog自体のPreviewでは表示できず、contentを分離することで通常確認と破壊的操作の両方を個別にPreviewできた。

- 障壁または不足: 全単体テストスイートが、今回のUI変更と無関係な`RoomKnowledgeArchiveRepositoryTest.invalidUrl_isRejectedWithoutWriting`で繰り返し失敗し、タスク由来の回帰と既存失敗の判別コストが発生している。
- 改善案: URL検証の期待値と実装を確認し、テストまたは実装の不整合を別タスクで解消し、ベースラインを緑に戻す。
- 根拠: 4画面の構造変更後にメインKotlinとUIテストソースはコンパイル成功し、新規ステータス単体テストも5件成功したが、全体では同一の既存テスト1件だけが失敗し続けた。

## 分類と反映先候補
- 分類: 恒久対応候補
- 反映先候補: `.skills/android-app-architecture/SKILL.md`、`.skills/orion-ui-experience/SKILL.md`、`.skills/android-quality-gates/SKILL.md`、UIテスト、Repository単体テスト

# 2026/8/1 18:03 フィードバック

## 作業内容
- SettingsをRoute、Screen、UiState、Presentation、Google Drive Picker境界へ分離し、排他的な操作状態と失敗種別を導入した。
- Settingsの表示文言をString Resourceへ移し、代表状態を`PreviewParameterProvider`から供給するPreviewへ整理した。

## 再利用可能な改善点
- 障壁または不足: `@Preview`関数が1つでも、`@PreviewParameter`のProviderが複数値を返すと値ごとにPreviewが生成されるため、関数数と表示件数が一致せず意図を読み取りにくい。
- 改善案: 複数状態を供給するProviderでは`getDisplayName()`を実装して状態名を表示する。単一状態だけを確認する場合はProviderを使わないか、`limit`を明示する。
- 根拠: SettingsのPreviewは1関数だったが、Providerが6状態を返すためAndroid Studio上では複数Previewとして展開された。利用中のCompose 1.11.4では`limit`の既定値は`Int.MAX_VALUE`だった。

- 障壁または不足: String Resource変更後に`:app:compileDebugKotlin`だけを実行しても、Manifestが参照する既存Resourceの欠落を検出できない場合がある。
- 改善案: `res/`を変更したタスクでは既存エントリを差分で確認し、`:app:processDebugResources`を含むビルドまたは`:app:testDebugUnitTest`まで実行してResource Linkを検証する。
- 根拠: Settings文言の追加時に既存の`app_name`が欠落してもKotlinコンパイルは成功し、その後の単体テスト実行中のAAPT Resource Linkで初めて検出された。

## 分類と反映先候補
- 分類: 恒久対応候補
- 反映先候補: `.skills/orion-ui-experience/SKILL.md`、`.skills/android-quality-gates/SKILL.md`

# 2026/8/1 18:04 フィードバック

## 作業内容
- Roomの1→2マイグレーション実装、登録処理、移行テストを削除し、開発中の最新スキーマをバージョン1へ直接反映した。
- データベース名の定数を`DATABASE_NAME`へ変更し、Roomマイグレーションの禁止と`const val`の命名規則を`AGENTS.md`へ追加した。

## 再利用可能な改善点
- 障壁または不足: Roomの既存スキーマ番号や移行テストだけを見てマイグレーションが必要だと判断すると、未リリースで互換対象の端末データが存在しない開発段階でも、不要な旧スキーマと移行経路を維持してしまう。
- 改善案: Roomスキーマ変更前に、リリース済みか、実端末へインストール済みか、保持すべき既存データがあるかを確認する。すべて該当しない実装工程ではバージョン1の初期スキーマを直接更新し、マイグレーションコード、登録処理、移行テストを作らない。
- 根拠: ORIONは未リリース、未インストール、テスト工程前で互換対象がなかったため、1→2移行はデータ保護に寄与せず、`@Database`と`1.json`をバージョン1へ揃えるだけで十分だった。

- 障壁または不足: `const val`を通常のプロパティ名と同じ形式で命名すると、IDEの命名規則違反が実装後に発生する。
- 改善案: 定数は作成時から大文字のスネークケースにし、変更したファイルの`const val`と参照名を差分確認する。
- 根拠: `Name`に`Const property name 'Name' should not contain lowercase letters`が発生し、`DATABASE_NAME`への変更が必要になった。

## 分類と反映先候補
- 分類: 恒久対応候補
- 反映先候補: `AGENTS.md`（反映済み）、`.skills/android-app-architecture/SKILL.md`、`.skills/android-quality-gates/SKILL.md`

# 2026/8/1 18:05 フィードバック

## 作業内容
- `OrionRoot`、`OrionNavHost`、各機能のRoute、ViewModel Factory、`OrionApplication`の依存関係と責務を調査し、Hilt未導入の判断が現在の構成に適しているかを診断した。

## 再利用可能な改善点
- 障壁または不足: 手動DIで開始した後、機能追加のたびに依存をActivityからRoot、NavHost、Routeへ引き回すと、初期には妥当だった判断がいつの間にかUI全体を変更する依存構造へ悪化する。
- 改善案: Repository、DataSource、ViewModel Factoryが複数のRouteへ伝播し始めた時点をDI方式の再評価基準にする。DIフレームワーク導入後は、ApplicationとDI Moduleを依存構築の境界とし、RootとNavHostにはデータ層の依存を渡さない。
- 根拠: 現状は4つのアプリ依存が`MainActivity`から`OrionRoot`、`OrionAppShell`、`OrionNavHost`へ順に渡され、各RouteがViewModel Factoryを構築しているため、機能追加や依存変更がナビゲーションとアプリシェルのシグネチャへ波及する。

- 障壁または不足: Navigationの現在地と、別のViewModelが保持する選択中Destinationを併用すると、同じ画面状態に複数の正本が生まれ、復元や戻る操作で不整合を起こしやすい。
- 改善案: 現在の画面とトップレベル選択状態はNavControllerのback stackから導出し、Navigationで既に保持される状態を別ViewModelへ重複保存しない。
- 根拠: `OrionAppShell`はNavControllerのcurrent routeと`OrionViewModel.selectedDestination`を併用し、routeを解決できない場合だけ後者へフォールバックしているため、状態所有者が一意ではない。

## 分類と反映先候補
- 分類: 恒久対応候補
- 反映先候補: `.skills/android-app-architecture/SKILL.md`、Navigation設計、DI導入判断基準
