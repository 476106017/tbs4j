package org.example.system;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

public class Database {

    public static Map<Session,String> userNames = new ConcurrentHashMap<>();
    public static Map<Session, String > userRoom = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static Session waitUser;// 匹配中的人

    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

}
