package com.portfolio.discordtaskbot.model;

import java.time.LocalDate;

/**
 * 1件のタスク情報を表すクラス
 * 
 * このプロジェクトでは、
 * 「ゲームごとに1件の共有タスクを持つ」設計を採用しているため、
 * 1つの Task オブジェクトが1件分の共有タスクを表します。
 * 
 * また、JSONファイルへの保存・読込を行うため、
 * Jackson で復元しやすい形にしています。
 */
public class Task {

    /** ゲーム名 */
    private String game;

    /** タスク名 */
    private String title;

    /** 締切日 */
    private LocalDate date;

    /**
     * 引数なしコンストラクタ
     * 
     * JSON読込時に Jackson がオブジェクトを生成するために使用します。
     * 通常のアプリ側処理では基本的に使いません。
     */
    public Task() {
    }

    /**
     * 通常利用用のコンストラクタ
     * 
     * @param game  ゲーム名
     * @param title タスク名
     * @param date  締切日
     */
    public Task(String game, String title, LocalDate date) {
        this.game = game;
        this.title = title;
        this.date = date;
    }

    /**
     * ゲーム名を取得する
     * 
     * @return ゲーム名
     */
    public String getGame() {
        return game;
    }

    /**
     * ゲーム名を設定する
     * 
     * JSON読込時に使用されます。
     * 
     * @param game ゲーム名
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * タスク名を取得する
     * 
     * @return タスク名
     */
    public String getTitle() {
        return title;
    }

    /**
     * タスク名を設定する
     * 
     * JSON読込時に使用されます。
     * 
     * @param title タスク名
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 締切日を取得する
     * 
     * @return 締切日
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 締切日を設定する
     * 
     * JSON読込時に使用されます。
     * 
     * @param date 締切日
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
}