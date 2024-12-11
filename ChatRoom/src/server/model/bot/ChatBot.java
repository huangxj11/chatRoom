package server.model.bot;

import common.model.entity.Message;
import common.model.entity.User;
import common.model.entity.Response;
import common.model.entity.ResponseType;
import common.model.entity.ResponseStatus;
import server.DataBuffer;
import server.OnlineClientIOCache;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatBot {
    private static final User BOT_USER = new User(9999L, "ChatBot");
    private static final Map<String, String> RESPONSES = new HashMap<>();

    static {
        // 预设一些问答对
        RESPONSES.put("你好", "你好！我是聊天机器人，很高兴为你服务！");
        RESPONSES.put("天气", "抱歉，我暂时无法获取实时天气信息，建议查看天气预报APP。");
        RESPONSES.put("时间", "当前时间是: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        RESPONSES.put("帮助", "你可以问我以下问题：\n1. 你好\n2. 天气\n3. 时间\n4. 帮助");
    }

    // 添加判断是否需要处理消息的方法
    public static boolean shouldProcessMessage(Message message) {
        return message.getMessage().toLowerCase().contains("@chatbot");
    }

    public static void processMessage(Message message) throws IOException {
        // 移除触发词，只保留实际问题
        String content = message.getMessage().replaceAll("(?i)@chatbot", "").trim();
        String response = null;

        // 检查消息是否包含关键词
        for (Map.Entry<String, String> entry : RESPONSES.entrySet()) {
            if (content.contains(entry.getKey())) {
                response = entry.getValue();
                break;
            }
        }

        // 如果没有找到匹配的回复，使用默认回复
        if (response == null) {
            response = "抱歉，我不太理解你的问题。你可以输入\"@ChatBot 帮助\"查看我可以回答的问题。";
        }

        // 创建机器人的回复消息
        Message botMessage = new Message();
        botMessage.setFromUser(BOT_USER);
        botMessage.setToUser(message.getFromUser());
        botMessage.setSendTime(new Date());

        // 格式化消息
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(botMessage.getSendTime())).append(" ")
                .append(BOT_USER.getNickname())
                .append("(").append(BOT_USER.getId()).append(") ")
                .append("回复").append(message.getFromUser().getNickname())
                .append("\n  ").append(response).append("\n");
        botMessage.setMessage(sb.toString());

        // 创建响应对象
        Response botResponse = new Response();
        botResponse.setType(ResponseType.CHAT);
        botResponse.setStatus(ResponseStatus.OK);
        botResponse.setData("txtMsg", botMessage);

        // 发送响应
        OnlineClientIOCache clientIO = DataBuffer.onlineUserIOCacheMap.get(message.getFromUser().getId());
        if (clientIO != null) {
            clientIO.getOos().writeObject(botResponse);
            clientIO.getOos().flush();
        }
    }

    public static boolean isBot(User user) {
        return user != null && user.getId()==BOT_USER.getId();
    }
}