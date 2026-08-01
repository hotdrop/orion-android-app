# Google Drive接続設定

ORIONはGoogle Identity Servicesの`AuthorizationClient`、Google Picker、Google Drive API v3を使用します。OAuthクライアントシークレットやアクセストークンをリポジトリへ保存しないでください。

## Google Cloudの設定

1. Google CloudでORION用プロジェクトを作成します。
2. Google Drive APIとGoogle Picker APIを有効化します。
3. Google Auth PlatformのBranding、Audience、Data Accessを設定します。
4. Data Accessでは`https://www.googleapis.com/auth/drive.file`を使用します。
5. ClientsからAndroid OAuthクライアントを作成します。
   - Package name: `jp.hotdrop.orion`
   - SHA-1: 使用するdebugまたはrelease署名証明書のフィンガープリント
6. OAuthアプリがテスト状態の場合は、ORIONで使用するGoogleアカウントをTest usersへ追加します。

debug署名のSHA-1は、次のGradleタスクで確認できます。

```shell
./gradlew signingReport
```

Google Play App Signingを使用するrelease版では、Play Consoleに表示されるアプリ署名鍵のSHA-1を別のAndroid OAuthクライアントへ登録します。

## アプリでの接続

1. ORIONのSettingsを開きます。
2. `SELECT DRIVE FOLDER`を選択します。
3. Googleアカウントと、Incoming Intelligenceの基準フォルダを選択します。
4. Incoming Intelligenceへ戻り、`SYNC`を実行します。

アプリ起動、画面表示、バックグラウンド処理ではDrive APIへ接続しません。`SYNC`を押した場合だけアクセストークンを取得し、選択フォルダを走査します。

## 取得範囲

- 選択フォルダとそのサブフォルダ内のGoogleドキュメントだけを対象にします。
- 本文は取得しません。
- ファイルID、タイトル、最終更新日時、相対パス、外部表示URLだけをRoomへ保存します。
- 全階層の取得に成功した場合だけRoomキャッシュを更新します。
- 認証、通信、部分取得に失敗した場合は、最後に成功した一覧を維持します。
