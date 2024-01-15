# 作物栽培管理システムアプリケーション

## 概要
このプロジェクトは、家庭菜園初心者の方でも、より楽しく、簡単に、効率的に家庭菜園を行うことを目的としたＷｅｂアプリケーションです。栽培計画の管理、栽培記録などの機能が含まれています。

## デモ

[デモを見る](https://saibaitecho.mydns.jp/app)

![デモスクリーンショット](![Alt text](demo.png))

### テストユーザーアカウント

※会社によって変更予定※
- ユーザー名: `testuser@test`
- パスワード: `testpassword`

## 使用技術
- Java SE 11(JDK 16.0.2)
- Spring Boot(2.7.17)
- PostgreSQL(13.11)
- AWS Cloud9
- Googleチャート
- FilePond


## 機能
- 認証機能  
    ログイン・ログアウト、パスワード再発行

- ユーザー管理機能  
    ユーザー登録、編集、削除、表示

- 作物管理機能  
    作物データの登録、編集、削除、表示、一覧表示〜検索機能

- 生産区画管理機能  
    区画データの登録、編集、削除、表示、区画割り当て、一覧表示〜検索機能

- 栽培計画管理機能  
    栽培計画データの登録、編集、削除、表示、履歴への切り替え、一覧表示〜検索機能、ガントチャート表示

- 栽培日誌機能  
    栽培日誌データの登録、編集、削除、表示、一覧表示〜検索機能

- ログ管理機能  
    ログの入出力
  
[テーブル定義](https://docs.google.com/spreadsheets/d/16QvwpC1gDVS6sfycjvpCnuTa3cDbFxV92TrALxpilkU/edit?usp=share_link)  閲覧権限を全員に
[ER図](https://drive.google.com/file/d/1hMk7DVq80gpqik0OOTqB4LYgM6oG-kq2/view?usp=share_link)  閲覧権限を全員に
[Github](https://github.com/yossshhhi0930/portfolio/)
  
## 使い方  
1. アプリにサインアップまたはログインします。  

2. ”マイページ” の ”新しく栽培計画を立てる” より、種蒔きをする月をクリックします。  

3. 種蒔きをする作物をクリックします。  

4. ”この作物で作物計画を作成する” ボタンをクリックし、必要情報を入力して、 ”登録” ボタンをクリックします。  
   ※**収穫完了予定日と使用可能な区画は自動算出ボタンをクリックして取得可能です。**  

5. ナビバーの ”栽培計画管理” ⇒ ”栽培計画一覧” をクリックすると、登録した栽培計画一覧の表、およびガントチャートが表示されます。  
   ※**作物名、区画、播種年、栽培ステイタスで絞り込み検索可能です。**

6. 栽培計画一覧画面の ”栽培日誌を作成” リンクをクリックすると、その栽培計画に紐づく画像付きの栽培日誌を作成することができます。  
   ※**登録した栽培日誌は、各栽培計画の詳細表示画面に表示されます。**

## 開発経緯
家庭菜園を趣味で行っており、まだ作物についての知識が乏しい私でも、簡単に効率的に楽しく栽培計画を立てられたらという思いから開発を行いました。  
  
## 工夫した点
　このアプリを制作するにあたり、まず第一に、自分が使う目的での開発ということもあり、どのよにすれば使いやすいか、自分の感覚やイメージを存分に駆使して開発に取り組むことができたことは大きな利点になっています。また、なるべく無駄がなくシンプルな実装を意識しました。  
以下、工夫した点を記述しています。  
  
・　”マイページ”　の　”新しく栽培計画を立てる”　では、月ごとに、種蒔きが可能な作物一覧をを表示し、そこから作物栽培計画を立てることができる設計になっている。 指定の時期に種を蒔くことができる作物を見つけやすく、収穫完了予定日も自動で取得可能である。これにより、私のように作物の知識がまだ乏しい初心者の方でも簡単に作物計画を立てることができるような設計になっている。  
  
・区画の管理機能により、栽培するスペースを無駄なく効率的に使用した栽培計画を立てることができる。  
  
・　”マイページ”　では、現在栽培中の作物と、今後栽培予定の作物の一覧が表示されるようになっている。時間の経過に伴い、変化する流動的な情報も、一目で確認することができるので、便利な機能である。  
  
・　"作物登録”　では、作物画像を　”トップ画像”　と　”その他画像”　を別々に登録できるように実装。これにより、編集時でも、トップ画像の変更が可能である。  
    
・　”栽培計画登録”、 ”栽培計画編集”　では、　”栽培終了”　のチェックボックスにチェックを入れることで、履歴としても保存が可能。また、栽培計画ごとに、栽培日誌が表示されるようになっているので、後から履歴と日誌を同時に参照することが可能。  
  
・　”栽培計画一覧”　はこのアプリケーションの中核であり、全ての栽培計画と栽培履歴がここで参照可能。最初の表示画面では、現在計画中のすべての栽培計画が表示される。その後、作物名、区画、播種年、栽培ステイタスでの絞り込み検索が可能。また同時に、検索内容に応じ計画ごとにガントチャートも表示される。ガントチャート表示機能があることで、視覚的に計画・履歴状況を把握することが可能。ガントチャートをクリックすると、その栽培計画の詳細画面に遷移する。  
　【使用例】  
　・　***区画の絞り込み検索　＆　ガントチャート***・・・区画ごとの利用状況を視覚的に把握でき、区画の空き状況を参照するのに役立ち、次の計画が立てやすい。  
　・　***年での絞り込み検索　＆　ガントチャート***・・・年ごとの表示機能により、年間スケジュールとしての管理に役立つ。  
　・　***”栽培終了分のみ表示”　で絞り込み検索***・・・履歴のデータ管理として利用可能。  
  
・　”栽培日誌一覧”　では、画像付きのカード形式で表示している。これにより、作物の成長過程を画像で確認することができる。  

## 苦労した点
・作物編集画面に実装において、作物画像の編集とテキスト情報の編集を一つの画面で行おうとすると、画像を削除・追加をした際に入力途中のテキスト情報がリセットされてしまう。一つの画面に二つのformを持たせる方法やjavascriptを使用し入力途中のデータを維持する方法など試みたが、現在の自分のスキルでは困難であった。
代替手段として、画像編集とテキスト情報の編集の画面を分けることで解決。また、栽培日誌の方では、同様に画像登録機能があるが画面を別々にしていないのは、登録するテキスト情報が比較的少ないため、一つの画面でもさほど支障がないと考え、編集画面は分けていない。  
  
・複数画像選択機能の実装において、一つのインプットタグから、Ctrlキーを使って一度に複数画像を選択する方法や複数のインプットタグを使用する方法はなく、複数回に分けて画像を複数選択する方法を模索・試行した結果、filepoundというライブラリを使用して複数回に分けて画像を複数選択する実装に成功。  
  
・栽培計画登録画面の実装において、この画面は、登録機能の他に、作物検索機能、収穫完了予定日の計算機能、利用可能な区画の取得機能を持つ。それぞれ、ボタンを設置し、情報を取得できるようになっている。初めは、ボタンごとにGETメソッドを実装する方法を試みるが、作物登録画面同様、入力途中のデータがリセットされてしまう。一つの画面に複数のformを設ける方法など模索・試行するが、困難であった。そこで、一つのPOSTメソッドでパラメータの値によって条件分けをするという実装方法により解決した。  
  
・栽培計画登録画面の実装において、利用可能な区画の情報の維持にも苦戦した。区画の情報はformエンティティのフィールド値であるが、ページをリロードすると、リセットされてしまっていた。原因は、そのフィールド値の元となる利用可能な区画の選択肢のリストデータがリセットされるからである。利用可能な区画の選択肢のリストデータはパラメータを使用しても、リスト型であるため、フロントとサーバー間の送受信が困難であった。そこで、formデータ受信の都度、送られてきたデータに基づき利用可能な区画の選択肢のリストデータを取得するという方法で解決した。この実装では、一度、区画を選択した後に、播種日や収穫案量予定日を変更して、　”収穫完了予定日を算出する”　ボタンをクリックした場合、その新しいデータに基づき、利用可能な区画が再度計算され、返される。元々選択していた区画が再び利用可能である場合は、そのデータは維持され、利用可能でなくなった場合には、新しい選択肢を返し、区画名は未選択の状態で返される。これは、ある意味理にかなった実装にはなっている。

・栽培計画一覧のガントチャート表示の実装において、ガントチャートのx軸の表示幅の期間を１月～１２月の一年間に指定したかった為、Googleチャートのscriptコード内で、表示期間の設定を試みたが、どのような方法でも困難であった為、代替手段として、指定された年一年間を表示するためのダミーの”start”という名前のエンティティを生成し表示させる方法をとっている。  
  
・作物データのフィールド値である播種可能期間の値の設定において、その値は年の情報を持たない月と日のみの値であるが、Controller内では、年を持つ値として計算する必要があった為、適切な型や処理を模索。初めはManthDate型（Controller内でLocalDate型に変換して計算に使用）で実装を進めていたが、データベースでManthDate型の処理ができないことに気づき、Date型に変更し、Controller内ではLocalDate型に変換して計算。月と日以外の情報は一旦デフォルト値を設定し、フロントサイドには月と日のみ表示させるようにすることで解決。また、年を跨いで入力した場合の処理の実装にも成功。  
   
・画像の保存と表示において、画像保存先ディレクトリのパスとエンティティに設定する画像表示用のパスが異なり、更に、ローカル環境でのパスとデプロイ後のパスも異なっていたため、正しいパスの模索と指定に難儀。  
  
・本番環境へのデプロイにおいて、cloud9を使用せずに、AWSで手動デプロイする方法を試みましたが、Eclipseでjarファイルを生成する際に、RDSへの接続が上手くいかず断念し、Cloud9でデプロイを行うことで解決。  
  
・認証機能の実装において、トークンや秘密情報の設定、URLメール送信の設定、秘密情報入力の失敗回数上限や有効期限の設定。  
  
## 問題点・課題

・作物登録・編集と、栽培日誌登録・編集において、ページをリロードした際に、選択中の画像がリセットされてしまう。 
  
・栽培計画一覧のガントチャート表示の実装において、ダミーの”start”という名前のエンティティが表示されてしまう。  
  
・画像編集とテキスト情報の編集を一つの画面でできた方が良い。  
  
・ユーザー情報の変更において、Email変更の際の再認証機能があった方が良い。  
  
・ユーザー情報変更において、Email変更の仮登録用エンティティのemailChangeEntityとパスワード再設定用エンティティPasswordReissueInfoは、メールにURLが届いた後、処理されない場合、そのまま残ってしまうので、自動削除される実装があった方が良い。  
  
・ログイン時にパスワードを間違えた回数が上限を上回った場合に、一時的にログインをブロックする機能があった方が良い。  
  
・現在、一つの作物に登録できる播種期間は一つまでだが、作物に播種期間が複数存在する場合（春まき、秋蒔きの両方がある場合など）を考慮し、複数期間を設定できた方が良い。  