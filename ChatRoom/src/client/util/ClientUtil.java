/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: ClientUtil
 * Author:   ITryagain
 * Date:     2019/5/16 20:24
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package client.util;

import client.DataBuffer;
import client.ui.ChatFrame;
import common.model.entity.Request;
import common.model.entity.Response;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import javax.swing.text.*;
import java.awt.*;
import static client.ui.ChatFrame.msgListArea;

/**
 * 〈客户端发送请求到服务器的工具〉<br>
 * 〈〉
 *
 * @author ITryagain
 * @create 2019/5/16
 * @since 1.0.0
 */

public class ClientUtil {

    /**
     * 发送请求对象,主动接收响应
     */
    public static Response sendTextRequest(Request request) throws IOException {
        Response response = null;
        try {
            // 发送请求
            DataBuffer.oos.writeObject(request);
            DataBuffer.oos.flush();
            System.out.println("客户端发送了请求对象:" + request.getAction());

            if (!"exit".equals(request.getAction())) {
                // 获取响应
                response = (Response) DataBuffer.ois.readObject();
                System.out.println("客户端获取到了响应对象:" + response.getStatus());
            } else {
                System.out.println("客户端断开连接了");
            }
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 发送请求对象,不主动接收响应
     */
    public static void sendTextRequest2(Request request) throws IOException {
        try {
            DataBuffer.oos.writeObject(request); // 发送请求
            DataBuffer.oos.flush();
            System.out.println("客户端发送了请求对象:" + request.getAction());
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 把指定文本添加到消息列表文本域中
     */
    public static void appendTxt2MsgListArea(String txt) {
        try {
            StyledDocument doc = (StyledDocument) msgListArea.getDocument();
            int startIndex = 0;
            int faceIndex;

            while ((faceIndex = txt.indexOf("[face:", startIndex)) != -1) {
                // 添加表情前的文本
                String beforeText = txt.substring(startIndex, faceIndex);
                doc.insertString(doc.getLength(), beforeText, null);

                // 查找表情代码结束位置
                int endIndex = txt.indexOf("]", faceIndex);
                if (endIndex == -1) break;

                // 获取表情ID，使用相同的路径格式
                String faceId = txt.substring(faceIndex + 6, endIndex);
                String imagePath = "ChatRoom/images/" + faceId + ".png";  // 使用相同的路径格式

                System.out.println("尝试在消息中显示表情: " + imagePath);

                // 插入表情图片
                ImageIcon icon = new ImageIcon(imagePath);
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    Image scaledImg = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImg);

                    Style style = msgListArea.addStyle("Icon", null);
                    StyleConstants.setIcon(style, icon);
                    doc.insertString(doc.getLength(), " ", style);
                    System.out.println("成功在消息中插入表情: " + faceId);
                } else {
                    System.out.println("无法在消息中加载表情图片: " + imagePath);
                    // 如果图片加载失败，至少显示一个占位符
                    doc.insertString(doc.getLength(), "[表情]", null);
                }

                startIndex = endIndex + 1;
            }

            // 添加剩余的文本
            if (startIndex < txt.length()) {
                doc.insertString(doc.getLength(), txt.substring(startIndex), null);
            }

            msgListArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            // 如果出现异常，至少保证文本能显示
            //msgListArea.append(txt);
        }
    }
}