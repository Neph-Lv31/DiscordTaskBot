package com.portfolio.discordtaskbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 設定ファイル（config.properties）から
 * Discord Botのトークンを読み込むためのクラスです。
 *
 * GitHubには実際の config.properties は公開しません。
 * 代わりに config.properties.example を公開し、
 * 利用者が自分の環境で config.properties を作成する構成にします。
 */
public class ConfigLoader {

    /**
     * config.properties から Discord Bot のトークンを取得するメソッドです。
     *
     * @return Discord Bot のトークン文字列
     */
    public static String getDiscordToken() {

        // プロパティファイル（キーと値の形式）を扱うためのオブジェクトを生成します。
        Properties properties = new Properties();

        // try-with-resources構文を使用します。
        // 処理終了後にInputStreamを自動で閉じるため、リソース管理が安全になります。
        try (InputStream input = ConfigLoader.class.getClassLoader()
                // クラスパス上の config.properties を読み込みます。
                .getResourceAsStream("config.properties")) {

            // config.properties が見つからなかった場合の処理です。
            if (input == null) {
                throw new RuntimeException(
                        "config.properties が見つかりません。"
                        + " config.properties.example をコピーして config.properties を作成し、"
                        + " discord.token に自分のBot Tokenを設定してください。");
            }

            // config.properties の内容を読み込みます。
            properties.load(input);

            // discord.token というキーに設定された値を取得します。
            String token = properties.getProperty("discord.token");

            // Tokenが未設定、または空白のみの場合はエラーにします。
            if (token == null || token.isBlank()) {
                throw new RuntimeException(
                        "discord.token が設定されていません。"
                        + " config.properties の discord.token にBot Tokenを設定してください。");
            }

            // 前後に余計な空白が入っていた場合に備えて、trim() で取り除いて返します。
            return token.trim();

        } catch (IOException e) {
            // ファイル読み込み中にエラーが発生した場合、
            // 原因となった例外情報も含めてRuntimeExceptionとして通知します。
            throw new RuntimeException("config.properties の読み込みに失敗しました。", e);
        }
    }
}