package com.portfolio.discordtaskbot.listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.portfolio.discordtaskbot.model.Task;
import com.portfolio.discordtaskbot.store.TaskStore;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * /task コマンドを処理するリスナークラス
 */
public class TaskCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!"task".equals(event.getName())) {
            return;
        }

        String subcommandName = event.getSubcommandName();

        if (subcommandName == null) {
            event.reply("サブコマンドが指定されていません。")
                 .setEphemeral(true)
                 .queue();
            return;
        }

        /* 
         * /task add
         */
        if ("add".equals(subcommandName)) {

            String userId = event.getUser().getId();

            OptionMapping gameOption = event.getOption("game");
            OptionMapping titleOption = event.getOption("title");
            OptionMapping dateOption = event.getOption("date");

            if (gameOption == null || titleOption == null || dateOption == null) {
                event.reply("必要な引数が取得できませんでした。")
                     .setEphemeral(true)
                     .queue();
                return;
            }

            String game = gameOption.getAsString();
            String title = titleOption.getAsString();
            String date = dateOption.getAsString();

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

            LocalDate parsedDate;

            try {
                parsedDate = LocalDate.parse(date, inputFormatter);
            } catch (DateTimeParseException e) {
                event.reply("日付の形式が正しくありません。`yyyy-MM-dd` 形式で入力してください。")
                     .setEphemeral(true)
                     .queue();
                return;
            }

            if (TaskStore.existsByGame(userId, game)) {
                event.reply("ゲーム「" + game + "」には、すでにタスクが登録されています。")
                     .setEphemeral(true)
                     .queue();
                return;
            }

            Task task = new Task(game, title, parsedDate);
            TaskStore.save(userId, task);

            // ★ 表示用フォーマット
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            String replyMessage = "## タスクを登録しました\n\n"
                    + "**" + task.getGame() + "**\n"
                    + "期限: " + task.getDate().format(displayFormatter) + "\n"
                    + "内容: " + task.getTitle();

            event.reply(replyMessage).queue();
        }

        /* 
         * /task list
         */
        else if ("list".equals(subcommandName)) {

            String userId = event.getUser().getId();
            String userName = event.getUser().getName();

            Map<String, Task> allTasks = TaskStore.findAll(userId);

            if (allTasks.isEmpty()) {
                String emptyMessage = "## " + userName + " さんのタスク一覧\n\n"
                        + "現在、登録されているタスクはありません。\n"
                        + "`/task add` で新しいタスクを登録してください。";

                event.reply(emptyMessage).queue();
                return;
            }

            List<Task> sortedTasks = allTasks.values().stream()
                    .sorted(Comparator.comparing(Task::getDate))
                    .collect(Collectors.toList());

            LocalDate today = LocalDate.now();

            // ★ 表示用フォーマット
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            StringBuilder message = new StringBuilder();
            message.append("## ").append(userName).append(" さんのタスク一覧\n\n");

            for (int i = 0; i < sortedTasks.size(); i++) {
                Task task = sortedTasks.get(i);

                String deadlineLabel;
                long daysBetween = ChronoUnit.DAYS.between(today, task.getDate());

                if (task.getDate().isBefore(today)) {
                    deadlineLabel = "（期限切れ）";
                } else if (task.getDate().isEqual(today)) {
                    deadlineLabel = "（今日まで）";
                } else {
                    deadlineLabel = "（あと" + daysBetween + "日）";
                }

                message.append("**")
                       .append(i + 1)
                       .append(". ")
                       .append(task.getGame())
                       .append("**\n")
                       .append("期限: ")
                       .append(task.getDate().format(displayFormatter))
                       .append(" ")
                       .append(deadlineLabel)
                       .append("\n")
                       .append("内容: ")
                       .append(task.getTitle())
                       .append("\n\n");
            }

            event.reply(message.toString()).queue();
        }

        /*
         * /task done
         */
        else if ("done".equals(subcommandName)) {

            String userId = event.getUser().getId();

            OptionMapping gameOption = event.getOption("game");

            if (gameOption == null) {
                event.reply("ゲーム名が取得できませんでした。")
                     .setEphemeral(true)
                     .queue();
                return;
            }

            String game = gameOption.getAsString();

            Task task = TaskStore.findByGame(userId, game);

            if (task == null) {
                event.reply("ゲーム「" + game + "」のタスクは登録されていません。")
                     .setEphemeral(true)
                     .queue();
                return;
            }

            TaskStore.deleteByGame(userId, game);

            /* 
             * 表示用フォーマット
             */
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            String replyMessage = "## タスクを完了にしました\n\n"
                    + "**" + task.getGame() + "**\n"
                    + "期限: " + task.getDate().format(displayFormatter) + "\n"
                    + "内容: " + task.getTitle();

            event.reply(replyMessage).queue();
        }

        else {
            event.reply("未対応のサブコマンドです。")
                 .setEphemeral(true)
                 .queue();
        }
    }
}