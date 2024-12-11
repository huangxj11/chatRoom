/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: RequestProcessor
 * Author:   ITryagain
 * Date:     2019/5/15 18:50
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package server.controller;

import common.model.entity.*;
import server.DataBuffer;
import server.OnlineClientIOCache;
import server.model.bot.ChatBot;
import server.model.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author ITryagain
 * @create 2019/5/15
 * @since 1.0.0
 */

public class RequestProcessor implements Runnable {
    private Socket currentClientSocket;  //当前正在请求服务器的客户端Socket

    public RequestProcessor(Socket currentClientSocket){
        this.currentClientSocket = currentClientSocket;
    }

    public void run() {
        boolean flag = true; //是否不间断监听
        try{
            OnlineClientIOCache currentClientIOCache = new OnlineClientIOCache(
                    new ObjectInputStream(currentClientSocket.getInputStream()),
                    new ObjectOutputStream(currentClientSocket.getOutputStream()));
            while(flag){ //不停地读取客户端发过来的请求对象
                //从请求输入流中读取到客户端提交的请求对象
                Request request = (Request)currentClientIOCache.getOis().readObject();
                System.out.println("Server读取了客户端的请求:" + request.getAction());

                String actionName = request.getAction();   //获取请求中的动作
                if(actionName.equals("userRegiste")){      //用户注册
                    registe(currentClientIOCache, request);
                }else if(actionName.equals("userLogin")){  //用户登录
                    login(currentClientIOCache, request);
                }else if("exit".equals(actionName)){       //请求断开连接
                    flag = logout(currentClientIOCache, request);
                }else if("chat".equals(actionName)){       //聊天
                    chat(request);
                }else if("shake".equals(actionName)){      //振动
                    shake(request);
                }else if("toSendFile".equals(actionName)){ //准备发送文件
                    toSendFile(request);
                }else if("agreeReceiveFile".equals(actionName)){ //同意接收文件
                    agreeReceiveFile(request);
                }else if("refuseReceiveFile".equals(actionName)){ //拒绝接收文件
                    refuseReceiveFile(request);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /** 发送欢迎消息 */
    private void sendWelcomeMessage(User user) throws IOException {
        // 创建系统消息用户
        User systemUser = new User(0L, "System");

        // 创建欢迎消息
        Message welcomeMsg = new Message();
        welcomeMsg.setFromUser(systemUser);
        welcomeMsg.setSendTime(new Date());

        // 格式化欢迎消息内容
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(welcomeMsg.getSendTime())).append(" ");
        sb.append("系统消息\n  欢迎用户 ").append(user.getNickname())
                .append("(").append(user.getId()).append(") ")
                .append("加入聊天室！\n  来自 ")
                .append(currentClientSocket.getInetAddress().getHostAddress())
                .append(":").append(currentClientSocket.getPort()).append("\n");
        welcomeMsg.setMessage(sb.toString());

        // 创建响应对象
        Response response = new Response();
        response.setType(ResponseType.CHAT);
        response.setStatus(ResponseStatus.OK);
        response.setData("txtMsg", welcomeMsg);

        // 广播给所有在线用户
        iteratorResponse(response);
    }

    /** 拒绝接收文件 */
    private void refuseReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");
        Response response = new Response();
        response.setType(ResponseType.REFUSERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);

        // 向发送方发送拒绝接收的响应
        OnlineClientIOCache ocic = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(ocic, response);
    }

    /** 同意接收文件 */
    private void agreeReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");

        // 向发送方发送同意接收的响应
        Response response = new Response();
        response.setType(ResponseType.AGREERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);
        OnlineClientIOCache sendIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(sendIO, response);

        // 向接收方发送开始接收文件的响应
        Response response2 = new Response();
        response2.setType(ResponseType.RECEIVEFILE);
        response2.setData("sendFile", sendFile);
        response2.setStatus(ResponseStatus.OK);
        OnlineClientIOCache receiveIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
        this.sendResponse(receiveIO, response2);
    }

    /** 客户端退出 */
    public boolean logout(OnlineClientIOCache oio, Request request) throws IOException{
        System.out.println(currentClientSocket.getInetAddress().getHostAddress()
                + ":" + currentClientSocket.getPort() + "走了");

        User user = (User)request.getAttribute("user");
        //把当前上线客户端的IO从Map中删除
        DataBuffer.onlineUserIOCacheMap.remove(user.getId());
        //从在线用户缓存Map中删除当前用户
        DataBuffer.onlineUsersMap.remove(user.getId());

        Response response = new Response();  //创建一个响应对象
        response.setType(ResponseType.LOGOUT);
        response.setData("logoutUser", user);
        oio.getOos().writeObject(response);  //把响应对象往客户端写
        oio.getOos().flush();
        currentClientSocket.close();  //关闭这个客户端Socket

        DataBuffer.onlineUserTableModel.remove(user.getId()); //把当前下线用户从在线用户表Model中删除
        iteratorResponse(response);//通知所有其它在线客户端

        return false;  //断开监听
    }
    /** 注册 */
    public void registe(OnlineClientIOCache oio, Request request) throws IOException {
        User user = (User)request.getAttribute("user");
        UserService userService = new UserService();
        userService.addUser(user);

        Response response = new Response();  //创建一个响应对象
        response.setStatus(ResponseStatus.OK);
        response.setData("user", user);

        oio.getOos().writeObject(response);  //把响应对象往客户端写
        oio.getOos().flush();

        //把新注册用户添加到RegistedUserTableModel中
        DataBuffer.registedUserTableModel.add(new String[]{
                String.valueOf(user.getId()),
                user.getPassword(),
                user.getNickname(),
                String.valueOf(user.getSex())
        });
    }

    /** 登录 */
    public void login(OnlineClientIOCache currentClientIO, Request request) throws IOException {
        String idStr = (String)request.getAttribute("id");
        String password = (String) request.getAttribute("password");
        UserService userService = new UserService();
        User user = userService.login(Long.parseLong(idStr), password);

        Response response = new Response();  //创建一个响应对象
        if(null != user){
            if(DataBuffer.onlineUsersMap.containsKey(user.getId())){ //用户已经登录了
                response.setStatus(ResponseStatus.OK);
                response.setData("msg", "该用户已经在别处上线了！");
                currentClientIO.getOos().writeObject(response);  //把响应对象往客户端写
                currentClientIO.getOos().flush();
            }else { //正确登录
                // 1. 添加到在线用户
                DataBuffer.onlineUsersMap.put(user.getId(), user);

                // 2. 设置在线用户列表
                response.setData("onlineUsers",
                        new CopyOnWriteArrayList<User>(DataBuffer.onlineUsersMap.values()));
                response.setStatus(ResponseStatus.OK);
                response.setData("user", user);
                currentClientIO.getOos().writeObject(response);
                currentClientIO.getOos().flush();

                // 3. 通知其他用户有新用户上线
                Response response2 = new Response();
                response2.setType(ResponseType.LOGIN);
                response2.setData("loginUser", user);
                iteratorResponse(response2);

                // 4. 发送欢迎消息
                sendWelcomeMessage(user);

                // 5. 缓存用户
                DataBuffer.onlineUserIOCacheMap.put(user.getId(), currentClientIO);
                DataBuffer.onlineUserTableModel.add(
                        new String[]{String.valueOf(user.getId()),
                                user.getNickname(),
                                String.valueOf(user.getSex())});
            }
        }else{ //登录失败
            response.setStatus(ResponseStatus.OK);
            response.setData("msg", "账号或密码不正确！");
            currentClientIO.getOos().writeObject(response);
            currentClientIO.getOos().flush();
        }
    }

    /** 聊天 */
    public void chat(Request request) throws IOException {
        Message msg = (Message)request.getAttribute("msg");
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        // 检查是否是@机器人的消息
        if (!ChatBot.isBot(msg.getFromUser()) && ChatBot.shouldProcessMessage(msg)) {
            ChatBot.processMessage(msg);  // 处理消息并由机器人回复
        }

        if(msg.getToUser() != null){ //私聊
            OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
            sendResponse(io, response);
        }else{  //群聊
            for(Long id : DataBuffer.onlineUserIOCacheMap.keySet()){
                if(msg.getFromUser().getId() == id){    continue; }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
        }
    }
    /*广播*/
    public static void board(String str) throws IOException {
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("系统通知\n  "+str+"\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.BOARD);
        response.setData("txtMsg", msg);

        for (Long id : DataBuffer.onlineUserIOCacheMap.keySet()) {
            sendResponse_sys(DataBuffer.onlineUserIOCacheMap.get(id), response);
        }
    }

    /*踢除用户*/
    public static void remove(User user_) throws IOException{
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());
        msg.setToUser(user_);

        StringBuffer sb = new StringBuffer();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("系统通知您\n  "+"您被强制下线"+"\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.REMOVE);
        response.setData("txtMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    /*私信*/
    public static void chat_sys(String str,User user_) throws IOException{
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());
        msg.setToUser(user_);

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("系统通知您\n  "+str+"\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    /** 发送振动 */
    public void shake(Request request)throws IOException {
        Message msg = (Message) request.getAttribute("msg");

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(msg.getFromUser().getNickname())
                .append("(").append(msg.getFromUser().getId()).append(") ")
                .append(df.format(msg.getSendTime())).append("\n  给您发送了一个窗口抖动\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.SHAKE);
        response.setData("ShakeMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse(io, response);
    }

    /** 准备发送文件 */
    public void toSendFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("file");
        boolean isGroupSend = (boolean)request.getAttribute("isGroupSend");

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.TOSENDFILE);
        response.setData("sendFile", sendFile);

        if (isGroupSend) {
            // 群发文件：给除了发送者外的所有在线用户发送文件请求
            for(Long userId : DataBuffer.onlineUserIOCacheMap.keySet()) {
                // 跳过发送者自己
                if(userId == sendFile.getFromUser().getId()) {
                    continue;
                }

                // 为每个接收者创建新的文件信息对象
                FileInfo newFileInfo = new FileInfo();
                newFileInfo.setFromUser(sendFile.getFromUser());
                newFileInfo.setToUser(DataBuffer.onlineUsersMap.get(userId));
                newFileInfo.setSrcName(sendFile.getSrcName());
                newFileInfo.setSendTime(sendFile.getSendTime());

                Response newResponse = new Response();
                newResponse.setStatus(ResponseStatus.OK);
                newResponse.setType(ResponseType.TOSENDFILE);
                newResponse.setData("sendFile", newFileInfo);

                // 发送请求给每个接收者
                OnlineClientIOCache ioCache = DataBuffer.onlineUserIOCacheMap.get(userId);
                sendResponse(ioCache, newResponse);
            }
        } else {
            // 私发文件：原有的单用户发送逻辑
            OnlineClientIOCache ioCache = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
            sendResponse(ioCache, response);
        }
    }

    /** 给所有在线客户都发送响应 */
    private void iteratorResponse(Response response) throws IOException {
        for(OnlineClientIOCache onlineUserIO : DataBuffer.onlineUserIOCacheMap.values()){
            ObjectOutputStream oos = onlineUserIO.getOos();
            oos.writeObject(response);
            oos.flush();
        }
    }

    /** 向指定客户端IO的输出流中输出指定响应 */
    private void sendResponse(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getOos();
        oos.writeObject(response);
        oos.flush();
    }

    /** 向指定客户端IO的输出流中输出指定响应 */
    private static void sendResponse_sys(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getOos();
        oos.writeObject(response);
        oos.flush();
    }
}
