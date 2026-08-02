# Google Drive接続設定

ORIONはGoogle Identity Servicesの`AuthorizationClient`とGoogle Drive API v3を使用します。アプリは端末上で動作しますが、Google Drive APIのOAuth認可にはGoogle Cloudプロジェクトが必須です。Firebaseへのアプリ登録、`google-services.json`、OAuthクライアントシークレットは使用しません。アクセストークンをリポジトリへ保存しないでください。

ORIONはGoogle Pickerを使用しません。`drive.file`はPickerで個別に共有されたファイルだけを対象とし、選択フォルダ配下の走査には適さないためです。本文を取得しないORIONは`drive.metadata.readonly`だけを要求し、Drive APIでフォルダ選択と手動同期に必要なメタデータを読み取ります。

## Google Cloudの設定

1. Google CloudでORION用プロジェクトを作成します。
2. Google Drive APIを有効化します。
3. Google Auth PlatformのBranding、Audience、Data Accessを設定します。
4. Data Accessへ`https://www.googleapis.com/auth/drive.metadata.readonly` を追加します。このスコープはrestricted scopeです。
5. ClientsからAndroid OAuthクライアントを作成します。
   - ローカルrelease版: Package nameは`jp.hotdrop.orion`、SHA-1はそのAPKを実際に署名した証明書のフィンガープリント
   - debug版も使用する場合: 別のAndroid OAuthクライアントを作成し、Package nameは`jp.hotdrop.orion.debug`、SHA-1はdebug署名証明書のフィンガープリント
6. 個人利用ではAudienceをテスト状態に保ち、ORIONで使用するGoogleアカウントをTest usersへ追加します。一般公開する場合はrestricted scopeに必要なOAuth検証を別途行います。

debug署名のSHA-1は、次のGradleタスクで確認できます。

```shell
./gradlew signingReport
```

Google Play App Signingを使用する場合は、アップロード鍵ではなく、Play Consoleに表示されるアプリ署名鍵のSHA-1を別のAndroid OAuthクライアントへ登録します。

## アプリでの接続

1. ORIONのSettingsを開きます。
2. `SELECT DRIVE FOLDER`を選択します。
3. Googleアカウントを認可します。
4. ORION内のフォルダ選択画面でDriveのフォルダ階層を移動し、Incoming Intelligenceの基準フォルダを確定します。
5. Incoming Intelligenceへ戻り、`SYNC`を実行します。

Settingsのフォルダ選択中は、フォルダ階層の表示に必要なメタデータだけをDrive APIから取得します。Incoming Intelligenceでは、アプリ起動、画面表示、バックグラウンド処理でDrive APIへ接続せず、`SYNC`を押した場合だけアクセストークンを取得して選択フォルダを走査します。

## 取得範囲

- 選択フォルダとそのサブフォルダ内のGoogleドキュメントとWord文書（`.docx`）を対象にします。
- 本文は取得しません。
- ファイルID、タイトル、最終更新日時、相対パス、外部表示URLだけをRoomへ保存します。
- 全階層の取得に成功した場合だけRoomキャッシュを更新します。
- 認証、通信、部分取得に失敗した場合は、最後に成功した一覧を維持します。
