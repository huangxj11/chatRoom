/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: CharFrame
 * Author:   ITryagain
 * Date:     2019/5/16 20:21
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package client.ui;

import client.ClientThread;
import client.DataBuffer;
import client.model.entity.MyCellRenderer;
import client.model.entity.OnlineUserListModel;
import client.util.ClientUtil;
import client.util.JFrameShaker;
import common.model.entity.FileInfo;
import common.model.entity.Message;
import common.model.entity.Request;
import common.model.entity.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.*;


/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author ITryagain
 * @create 2019/5/16
 * @since 1.0.0
 */

public class ChatFrame extends JFrame{
    private static final long serialVersionUID = -2310785591507878535L;
    /**聊天对方的信息Label*/
    private JLabel otherInfoLbl;
    /** 当前用户信息Lbl */
    private JLabel currentUserLbl;
    /**聊天信息列表区域*/
    public static JTextPane msgListArea;
    /**要发送的信息区域*/
    public static JTextPane sendArea;
    /** 在线用户列表 */
    public static JList onlineList;
    /** 在线用户数统计Lbl */
    public static JLabel onlineCountLbl;
    /** 准备发送的文件 */
    public static FileInfo sendFile;
    /**选择表情*/
    private JPopupMenu facePanel; // 表情选择面板

    /** 私聊复选框 */
    public JCheckBox rybqBtn;

    public ChatFrame(){
        this.init();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void init(){
        this.setTitle("SYSU聊天室");
        this.setSize(550, 500);
        this.setResizable(false);

        //设置默认窗体在屏幕中央
        int x = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);

        //左边主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        //右边用户面板
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        // 创建一个分隔窗格
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mainPanel, userPanel);
        splitPane.setDividerLocation(380);
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        this.add(splitPane, BorderLayout.CENTER);

        //左上边信息显示面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        //右下连发送消息面板
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());

        // 创建一个分隔窗格
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                infoPanel, sendPanel);
        splitPane2.setDividerLocation(300);
        splitPane2.setDividerSize(1);
        mainPanel.add(splitPane2, BorderLayout.CENTER);

        otherInfoLbl = new JLabel("当前状态：群聊中...");
        infoPanel.add(otherInfoLbl, BorderLayout.NORTH);

        msgListArea = new JTextPane();
        msgListArea.setEditable(false);
        infoPanel.add(new JScrollPane(msgListArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        sendPanel.add(tempPanel, BorderLayout.NORTH);

        // 聊天按钮面板
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(btnPanel, BorderLayout.CENTER);

        //字体按钮
        JButton fontBtn = new JButton("字体");
        fontBtn.setMargin(new Insets(10,10,10,10));
        fontBtn.setToolTipText("设置字体和格式");
        btnPanel.add(fontBtn);

        // 表情按钮
        JButton faceBtn = new JButton("表情");
        faceBtn.setPreferredSize(new Dimension(80, 30));
        faceBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFacePanel(faceBtn);
            }
        });
        btnPanel.add(faceBtn);

        // 振动按钮
        JButton shakeBtn = new JButton("振动");
        shakeBtn.setPreferredSize(new Dimension(80, 30));
        btnPanel.add(shakeBtn);

        // 文件按钮
        JButton sendFileBtn = new JButton("文件");
        sendFileBtn.setPreferredSize(new Dimension(80, 30));
        btnPanel.add(sendFileBtn);

        //私聊按钮
        rybqBtn = new JCheckBox("私聊");
        tempPanel.add(rybqBtn, BorderLayout.EAST);

        //要发送的信息的区域
        sendArea = new JTextPane();
        //sendArea.setLineWrap(true);
        sendPanel.add(new JScrollPane(sendArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        // 聊天按钮面板
        JPanel btn2Panel = new JPanel();
        btn2Panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.add(btn2Panel, BorderLayout.SOUTH);
        JButton closeBtn = new JButton("关闭");
        closeBtn.setToolTipText("退出整个程序");
        btn2Panel.add(closeBtn);
        JButton submitBtn = new JButton("发送");
        submitBtn.setToolTipText("按Enter键发送消息");
        btn2Panel.add(submitBtn);
        sendPanel.add(btn2Panel, BorderLayout.SOUTH);

        //在线用户列表面板
        JPanel onlineListPane = new JPanel();
        onlineListPane.setLayout(new BorderLayout());
        onlineCountLbl = new JLabel("在线用户列表(1)");
        onlineListPane.add(onlineCountLbl, BorderLayout.NORTH);

        //当前用户面板
        JPanel currentUserPane = new JPanel();
        currentUserPane.setLayout(new BorderLayout());
        currentUserPane.add(new JLabel("当前用户"), BorderLayout.NORTH);

        // 右边用户列表创建一个分隔窗格
        JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                onlineListPane, currentUserPane);
        splitPane3.setDividerLocation(340);
        splitPane3.setDividerSize(1);
        userPanel.add(splitPane3, BorderLayout.CENTER);

        //获取在线用户并缓存
        DataBuffer.onlineUserListModel = new OnlineUserListModel(DataBuffer.onlineUsers);
        //在线用户列表
        onlineList = new JList(DataBuffer.onlineUserListModel);
        onlineList.setCellRenderer(new MyCellRenderer());
        //设置为单选模式
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineListPane.add(new JScrollPane(onlineList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        //当前用户信息Label
        currentUserLbl = new JLabel();
        currentUserPane.add(currentUserLbl);

        ///////////////////////注册事件监听器/////////////////////////
        //关闭窗口
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        //关闭按钮的事件
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });

        //选择某个用户私聊
        rybqBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(rybqBtn.isSelected()){
                    User selectedUser = (User)onlineList.getSelectedValue();
                    if(null == selectedUser){
                        otherInfoLbl.setText("当前状态：私聊(选中在线用户列表中某个用户进行私聊)...");
                    }else if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("当前状态：想自言自语?...系统不允许");
                    }else{
                        otherInfoLbl.setText("当前状态：与 "+ selectedUser.getNickname()
                                +"(" + selectedUser.getId() + ") 私聊中...");
                    }
                }else{
                    otherInfoLbl.setText("当前状态：群聊...");
                }
            }
        });

        //选择某个用户
        onlineList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                User selectedUser = (User)onlineList.getSelectedValue();
                if(rybqBtn.isSelected()){
                    if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("当前状态：想自言自语?...系统不允许");
                    }else{
                        otherInfoLbl.setText("当前状态：与 "+ selectedUser.getNickname()
                                +"(" + selectedUser.getId() + ") 私聊中...");
                    }
                }
            }
        });

        //发送文本消息
        sendArea.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == Event.ENTER){
                    sendTxtMsg();
                }
            }
        });
        submitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendTxtMsg();
            }
        });

        //发送振动
        shakeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendShakeMsg();
            }
        });

        //发送文件
        sendFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendFile();
            }
        });

        this.loadData();  //加载初始数据
    }

    /**  加载数据 */
    public void loadData(){
        //加载当前用户数据
        if(null != DataBuffer.currentUser){
            currentUserLbl.setIcon(
                    new ImageIcon("images/" + DataBuffer.currentUser.getHead() + ".png"));
            currentUserLbl.setText(DataBuffer.currentUser.getNickname()
                    + "(" + DataBuffer.currentUser.getId() + ")");
        }
        //设置在线用户列表
        onlineCountLbl.setText("在线用户列表("+ DataBuffer.onlineUserListModel.getSize() +")");
        //启动监听服务器消息的线程
        new ClientThread(this).start();
    }

    /** 发送振动 */
    public void sendShakeMsg(){
        User selectedUser = (User)onlineList.getSelectedValue();
        if(null != selectedUser){
            if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                JOptionPane.showMessageDialog(ChatFrame.this, "不能给自己发送振动!",
                        "不能发送", JOptionPane.ERROR_MESSAGE);
            }else{
                Message msg = new Message();
                msg.setFromUser(DataBuffer.currentUser);
                msg.setToUser(selectedUser);
                msg.setSendTime(new Date());

                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                StringBuffer sb = new StringBuffer();
                sb.append(" ").append(msg.getFromUser().getNickname())
                        .append("(").append(msg.getFromUser().getId()).append(") ")
                        .append(df.format(msg.getSendTime()))
                        .append("\n  给").append(msg.getToUser().getNickname())
                        .append("(").append(msg.getToUser().getId()).append(") ")
                        .append("发送了一个窗口抖动\n");
                msg.setMessage(sb.toString());

                Request request = new Request();
                request.setAction("shake");
                request.setAttribute("msg", msg);
                try {
                    ClientUtil.sendTextRequest2(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                new JFrameShaker(ChatFrame.this).startShake();
            }
        }else{
            JOptionPane.showMessageDialog(ChatFrame.this, "不能群发送振动!",
                    "不能发送", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String parseFaceCode(String content) {
        // 不再需要转换为文字描述，直接返回原内容
        return content;
    }

    /** 发送文本消息 */
    public void sendTxtMsg(){
        String content = sendArea.getText();
        //content = parseFaceCode(content);


        if ("".equals(content)) { //无内容
            JOptionPane.showMessageDialog(ChatFrame.this, "不能发送空消息!",
                    "不能发送", JOptionPane.ERROR_MESSAGE);
        } else { //发送
            User selectedUser = (User)onlineList.getSelectedValue();
//			if(null != selectedUser &&
//					DataBuffer.currentUser.getId() == selectedUser.getId()){
//				JOptionPane.showMessageDialog(ChatFrame.this, "不能给自己发送消息!",
//						"不能发送", JOptionPane.ERROR_MESSAGE);
//				return;
//			}

            //如果设置了ToUser表示私聊，否则群聊
            Message msg = new Message();
            if(rybqBtn.isSelected()){  //私聊
                if(null == selectedUser){
                    JOptionPane.showMessageDialog(ChatFrame.this, "没有选择私聊对象!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                    return;
                }else if (DataBuffer.currentUser.getId() == selectedUser.getId()){
                    JOptionPane.showMessageDialog(ChatFrame.this, "不能给自己发送消息!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                    return;
                }else{
                    msg.setToUser(selectedUser);
                }
            }
            msg.setFromUser(DataBuffer.currentUser);
            msg.setSendTime(new Date());

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            StringBuffer sb = new StringBuffer();
            sb.append(" ").append(df.format(msg.getSendTime())).append(" ")
                    .append(msg.getFromUser().getNickname())
                    .append("(").append(msg.getFromUser().getId()).append(") ");
            if(!this.rybqBtn.isSelected()){//群聊
//				if(null == selectedUser){
//					sb.append("对大家说");
//				}else{
//					sb.append("对").append(selectedUser.getNickname())
//						.append("(").append(selectedUser.getId()).append(")")
//						.append("说");
//				}
                sb.append("对大家说");
            }
            sb.append("\n  ").append(content).append("\n");
            msg.setMessage(sb.toString());

            Request request = new Request();
            request.setAction("chat");
            request.setAttribute("msg", msg);
            try {
                ClientUtil.sendTextRequest2(request);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //JTextArea中按“Enter”时，清空内容并回到首行
            InputMap inputMap = sendArea.getInputMap();
            ActionMap actionMap = sendArea.getActionMap();
            Object transferTextActionKey = "TRANSFER_TEXT";
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),transferTextActionKey);
            actionMap.put(transferTextActionKey,new AbstractAction() {
                private static final long serialVersionUID = 7041841945830590229L;
                public void actionPerformed(ActionEvent e) {
                    sendArea.setText("");
                    sendArea.requestFocus();
                }
            });
            sendArea.setText("");
            ClientUtil.appendTxt2MsgListArea(msg.getMessage());
        }
    }


    private void initFacePanel() {
        facePanel = new JPopupMenu();
        facePanel.setLayout(new GridLayout(2, 5, 5, 5));

        // 添加调试信息
        System.out.println("正在初始化表情面板...");

        for (int i = 1; i <= 10; i++) {
            final String faceId = String.format("%02d", i);
            String imagePath = "ChatRoom/images/" + faceId + ".png";

            // 调试信息：输出图片路径
            System.out.println("尝试加载表情图片: " + imagePath);

            // 获取项目根目录下的图片
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("警告：图片文件不存在: " + imagePath);
                continue;
            }

            ImageIcon icon = new ImageIcon(imagePath);
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                System.out.println("成功加载表情图片: " + faceId);
                Image scaledImg = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);

                JLabel faceLabel = new JLabel(icon);
                faceLabel.setToolTipText("表情" + faceId);
                faceLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

                final ImageIcon finalIcon = icon;
                faceLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("选择了表情: " + faceId);
                        try {
                            // 直接在文本区域插入图片
                            StyledDocument doc = (StyledDocument) sendArea.getDocument();
                            Style style = sendArea.addStyle("Icon", null);
                            StyleConstants.setIcon(style, finalIcon);
                            doc.insertString(doc.getLength(), " ", style);
                            doc.insertString(doc.getLength(), "[face:" + faceId + "]", null);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                        facePanel.setVisible(false);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        ((JLabel)e.getSource()).setBorder(
                                BorderFactory.createLineBorder(Color.BLUE));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        ((JLabel)e.getSource()).setBorder(
                                BorderFactory.createEmptyBorder(2, 2, 2, 2));
                    }
                });
                facePanel.add(faceLabel);
            } else {
                System.out.println("警告：无法加载表情图片: " + faceId);
            }
        }
    }

    // 添加插入表情的方法
    private void insertFaceToSendArea(String faceId) {
        try {
            // 插入表情代码
            sendArea.getDocument().insertString(
                    sendArea.getCaretPosition(),
                    "[face:" + faceId + "]",
                    null
            );
            // 同时显示表情图片
            ImageIcon icon = new ImageIcon("images/face/" + faceId + ".png");
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImg = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
                sendArea.insertIcon(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showFacePanel(JButton btn) {
        if (facePanel == null) {
            initFacePanel();
        }
        facePanel.show(btn, 0, btn.getHeight());
    }

    /** 发送文件 */
    private void sendFile() {
        // 移除对选中用户的检查，改为支持群发和私发
        User selectedUser = (User)onlineList.getSelectedValue();
        boolean isGroupSend = !rybqBtn.isSelected(); // 使用私聊按钮来判断是否群发

        if(!isGroupSend && selectedUser == null) {  // 如果是私聊但没选择用户
            JOptionPane.showMessageDialog(this,
                    "私聊发送文件请选择一个在线用户!",
                    "操作提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(!isGroupSend && DataBuffer.currentUser.getId() == selectedUser.getId()){
            JOptionPane.showMessageDialog(this,
                    "不能给自己发送文件!",
                    "操作提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要发送的文件");

        // 显示文件选择对话框
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if(file.length() > 1024 * 1024 * 100){  // 100MB
                JOptionPane.showMessageDialog(this,
                        "文件大小不能超过100MB!",
                        "操作提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 构建文件信息对象
            FileInfo sendFile = new FileInfo();
            sendFile.setFromUser(DataBuffer.currentUser);
            // 只有私聊时才设置接收用户
            if (!isGroupSend) {
                sendFile.setToUser(selectedUser);
            }
            try {
                sendFile.setSrcName(file.getCanonicalPath());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            sendFile.setSendTime(new Date());

            // 发送文件传输请求
            Request request = new Request();
            request.setAction("toSendFile");
            request.setAttribute("file", sendFile);
            request.setAttribute("isGroupSend", isGroupSend); // 添加群发标记

            try {
                ClientUtil.sendTextRequest2(request);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "文件发送请求失败!",
                        "失败提示",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }

            // 显示等待提示
            String tipMsg;
            if (isGroupSend) {
                tipMsg = "【文件消息】向所有人发送文件 [" + file.getName() + "]，等待接收...\n";
            } else {
                tipMsg = "【文件消息】向 " + selectedUser.getNickname()
                        + "(" + selectedUser.getId() + ") 发送文件 ["
                        + file.getName() + "]，等待对方接收...\n";
            }
            ClientUtil.appendTxt2MsgListArea(tipMsg);
        }
    }

    /** 关闭客户端 */
    private void logout() {
        int select = JOptionPane.showConfirmDialog(ChatFrame.this,
                "确定退出吗？\n\n退出程序将中断与服务器的连接!", "退出聊天室",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            Request req = new Request();
            req.setAction("exit");
            req.setAttribute("user", DataBuffer.currentUser);
            try {
                ClientUtil.sendTextRequest(req);
            } catch (IOException ex) {
                ex.printStackTrace();
            }finally{
                System.exit(0);
            }
        }else{
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
    }

    /*踢除*/
    public static void remove() {
        int select = JOptionPane.showConfirmDialog(sendArea,
                "您已被踢除？\n\n", "系统通知",
                JOptionPane.YES_NO_OPTION);

        Request req = new Request();
        req.setAction("exit");
        req.setAttribute("user", DataBuffer.currentUser);
        try {
            ClientUtil.sendTextRequest(req);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }

    }
}
