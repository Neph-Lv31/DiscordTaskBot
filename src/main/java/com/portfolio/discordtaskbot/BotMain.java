package com.portfolio.discordtaskbot;

import java.time.Duration;

import com.portfolio.discordtaskbot.listener.HelpCommandListener;
import com.portfolio.discordtaskbot.listener.TaskCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import okhttp3.OkHttpClient;

public class BotMain {

    public static void main(String[] args) {
        /*
         * JDA インスタンスを終了処理でも参照できるようにするため、
         * try ブロックの外で宣言しておきます。
         * 
         * Bot 終了時に shutdownHook からこの変数を利用して、
         * JDA や HTTP 接続の後始末を行います。
         */
        JDA jda = null;

        try {
            /*
             * config.properties から Discord Bot のトークンを読み込みます。
             * トークンは機密情報であるため、ソースコードに直接書かず、
             * 外部設定ファイルから取得する形にしています。
             */
            String token = ConfigLoader.getDiscordToken();

            /*
             * 取得したトークンを使って JDA を起動します。
             * 
             * addEventListeners(...) では、
             * スラッシュコマンドを処理するリスナークラスを登録しています。
             * 
             * - HelpCommandListener : /help を担当
             * - TaskCommandListener : /task 系コマンドを担当
             */
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(
                            new HelpCommandListener(),
                            new TaskCommandListener()
                    )
                    .build();

            /*
             * Bot が Discord に正常接続し、
             * コマンド受付可能な状態になるまで待機します。
             */
            jda.awaitReady();

            /*
             * コマンドを登録する対象サーバー（Guild）の ID です。
             * 
             * 今回はテスト用サーバーに対してギルドコマンドとして登録します。
             * グローバル登録より反映が速いため、開発・検証に向いています。
             */
            long guildId = 623193719121903636L;

            /*
             * 指定した Guild ID から対象サーバーを取得します。
             */
            Guild guild = jda.getGuildById(guildId);

            /*
             * サーバーが取得できた場合のみ、そのサーバーにコマンドを登録します。
             * Guild が null の場合は、ID の誤りや Bot 未参加の可能性があります。
             */
            if (guild != null) {
                guild.updateCommands()
                        .addCommands(
                                /* =========================================
                                 * /help コマンド
                                 * =========================================
                                 * 利用できるコマンドの説明を表示するためのコマンドです。
                                 */
                                Commands.slash("help", "利用できるコマンド一覧を表示します。"),

                                /* =========================================
                                 * /task コマンド
                                 * =========================================
                                 * タスク管理機能の親コマンドです。
                                 * 
                                 * 現時点では、以下の3つのサブコマンドを登録しています。
                                 * - add  : タスクを追加する
                                 * - list : タスク一覧を表示する
                                 * - done : タスクを完了（削除）する
                                 */
                                Commands.slash("task", "タスク管理コマンドです。")
                                        .addSubcommands(
                                                /*
                                                 * /task add
                                                 * 
                                                 * game : ゲーム名
                                                 * title: タスク名
                                                 * date : 期限
                                                 * 
                                                 * 現行仕様では「1ゲームにつき1件の共有タスク方式」のため、
                                                 * game が実質的な識別キーとして使われます。
                                                 */
                                                new SubcommandData("add", "タスクを追加します。")
                                                        .addOption(OptionType.STRING, "game", "ゲーム名を入力してください。", true)
                                                        .addOption(OptionType.STRING, "title", "タスク名を入力してください。", true)
                                                        .addOption(OptionType.STRING, "date", "日付を yyyy-MM-dd 形式で入力してください。", true),

                                                /*
                                                 * /task list
                                                 * 
                                                 * 現在登録されているタスクの一覧を表示します。
                                                 */
                                                new SubcommandData("list", "登録されているタスクの一覧を表示します。"),

                                                /*
                                                 * /task done
                                                 * 
                                                 * 指定したゲーム名のタスクを完了扱いにします。
                                                 * 現時点では「完了 = ストアから削除」として実装しています。
                                                 */
                                                new SubcommandData("done", "指定したゲームのタスクを完了（削除）します。")
                                                        .addOption(OptionType.STRING, "game", "完了にするゲーム名を入力してください。", true)
                                        )
                        )
                        .queue();

                System.out.println("/help、/task add、/task list、/task done コマンドをテスト用サーバーに登録しました。");
            } else {
                System.out.println("指定したGuild IDのサーバーが見つかりませんでした。");
            }

            /*
             * 起動完了後、コンソールに基本情報を表示します。
             * 開発中の動作確認をしやすくするための出力です。
             */
            System.out.println("Bot がオンラインになりました。");
            System.out.println("Bot名: " + jda.getSelfUser().getName());

            /*
             * Eclipse の停止ボタンやアプリ終了時に、
             * JDA と HTTP クライアントをできるだけ丁寧に終了させるための処理を登録します。
             * 
             * 強制終了だけに頼らず、通常終了 → 必要なら強制終了、という順で試みることで、
             * 通信資源やスレッドを安全に後始末しやすくなります。
             */
            final JDA shutdownTarget = jda;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    /*
                     * まずは通常の終了処理を試みます。
                     */
                    shutdownTarget.shutdown();

                    /*
                     * 10 秒待っても終了しない場合は、強制終了へ切り替えます。
                     */
                    if (!shutdownTarget.awaitShutdown(Duration.ofSeconds(10))) {
                        shutdownTarget.shutdownNow();

                        /*
                         * 強制終了後も少し待機し、終了完了を促します。
                         */
                        shutdownTarget.awaitShutdown(Duration.ofSeconds(5));
                    }

                    /*
                     * JDA が内部で使用している HTTP クライアントについても、
                     * 接続プールやスレッドプールを明示的に終了します。
                     */
                    OkHttpClient client = shutdownTarget.getHttpClient();
                    client.connectionPool().evictAll();
                    client.dispatcher().executorService().shutdown();

                } catch (InterruptedException e) {
                    /*
                     * 終了待機中に割り込みが発生した場合は、
                     * 割り込み状態を復元して上位へ正しく伝わるようにします。
                     */
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    /*
                     * 終了処理中に想定外のエラーが発生した場合の保険です。
                     * 原因調査のため、スタックトレースを出力します。
                     */
                    e.printStackTrace();
                }
            }));

            /*
             * Bot を常駐動作させるため、
             * メインスレッドを待機状態にします。
             */
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            /*
             * 待機中に割り込みが発生した場合の処理です。
             */
            System.out.println("Bot の待機中に割り込みが発生しました。");
            e.printStackTrace();

            /*
             * 割り込み状態を復元します。
             */
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            /*
             * 起動中または実行中にその他のエラーが発生した場合の処理です。
             */
            System.out.println("Bot 起動中にエラーが発生しました。");
            e.printStackTrace();
        }
    }
}