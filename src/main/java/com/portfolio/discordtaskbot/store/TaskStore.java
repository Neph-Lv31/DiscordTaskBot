package com.portfolio.discordtaskbot.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfolio.discordtaskbot.model.Task;

/**
 * タスクを保存・管理するクラス（ユーザー対応版）
 * 
 * 構造：
 * ユーザーID → (ゲーム名 → Task)
 */
public class TaskStore {

    /** 保存ファイルパス */
    private static final String FILE_PATH = "data/task-data.json";

    /** JSON変換用 */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * ユーザーごとのタスク管理マップ
     * 
     * キー   : ユーザーID
     * 値     : (ゲーム名 → Task)
     */
    private static final Map<String, Map<String, Task>> userTaskMap = new HashMap<>();

    /* ===========================================
     * 初期化（起動時にファイル読込）
     * =========================================== */
    static {
        loadFromFile();
    }

    /* ===========================================
     * ユーザー別処理
     * =========================================== */

    /**
     * 指定ユーザーのタスクマップを取得（なければ新規作成）
     */
    private static Map<String, Task> getUserTasks(String userId) {
        return userTaskMap.computeIfAbsent(userId, k -> new HashMap<>());
    }

    /**
     * 指定ユーザーのゲームが存在するか
     */
    public static boolean existsByGame(String userId, String game) {
        return getUserTasks(userId).containsKey(game);
    }

    /**
     * タスク保存
     */
    public static void save(String userId, Task task) {
        getUserTasks(userId).put(task.getGame(), task);
        saveToFile();
    }

    /**
     * タスク取得
     */
    public static Task findByGame(String userId, String game) {
        return getUserTasks(userId).get(game);
    }

    /**
     * タスク削除
     */
    public static void deleteByGame(String userId, String game) {
        getUserTasks(userId).remove(game);
        saveToFile();
    }

    /**
     * 全タスク取得（ユーザー単位）
     */
    public static Map<String, Task> findAll(String userId) {
        return new HashMap<>(getUserTasks(userId));
    }

    /* ===========================================
     * ファイル保存
     * =========================================== */
    private static void saveToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(new File(FILE_PATH), userTaskMap);
        } catch (IOException e) {
            System.out.println("タスクデータの保存に失敗しました。");
            e.printStackTrace();
        }
    }

    /* ===========================================
     * ファイル読込
     * =========================================== */
    private static void loadFromFile() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("保存ファイルが存在しないため、新規作成として開始します。");
            return;
        }

        try {
            Map<String, Map<String, Task>> loaded =
                    objectMapper.readValue(file,
                            new TypeReference<Map<String, Map<String, Task>>>() {});

            if (loaded != null) {
                userTaskMap.clear();
                userTaskMap.putAll(loaded);
            }

            System.out.println("タスクデータ読込成功（ユーザー数: " + userTaskMap.size() + "）");

        } catch (Exception e) {
            System.out.println("タスクデータ読込失敗。ファイル破損の可能性あり。");
            e.printStackTrace();

            userTaskMap.clear();
            System.out.println("空データで起動します。");
        }
    }
}