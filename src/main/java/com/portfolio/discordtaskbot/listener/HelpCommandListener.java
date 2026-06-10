package com.portfolio.discordtaskbot.listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        
    	/* 実行されたコマンド名が help かどうかを確認します。
         * help 以外のコマンドだった場合は、このクラスでは何もせず終了します。
         */
    	
        if (!event.getName().equals("help")) {
            return;
        }
        
        String helpMessage = 
        		"## タスク管理Bot ヘルプ\n\n"
                + "このBotでは、ゲームごとのタスクを管理できます。\n"
                + "タスクは **ユーザーごとに分かれて保存** され、**再起動後も保持** されます。\n\n"
                + "### 使用できるコマンド\n\n"
                + "```"
                + "/task add game:ゲーム名 date:期限(yyyy-MM-dd) detail:内容\n"
                + "```"
                + "ゲームのタスクを登録します。\n\n"
                + "```"
                + "/task list\n"
                + "```"
                + "自分のタスク一覧を表示します。\n"
                + "期限が近い順に表示されます。\n\n"
                + "```"
                + "/task done game:ゲーム名\n"
                + "```"
                + "指定したゲームのタスクを完了（削除）します。\n\n"
                + "```"
                + "/help\n"
                + "```"
                + "このヘルプを表示します。\n\n"
                + "### 補足\n"
                + "- タスクは `data/task-data.json` に保存されます。\n"
                + "- 同じユーザーは、同じゲーム名で重複登録できません。\n"
                + "- 他のユーザーのタスクは表示されません。";
                
         /*
          * help が実行されたときに、Botの使い方を簡単に案内するメッセージを返します。 
          * reply(...) はコマンドに対する返信を行う処理です。
         */
        event.reply(helpMessage).queue();
    }
}