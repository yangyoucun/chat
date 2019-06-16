package com.client;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ChatClient {
	
	// 定义成员变量
	/**
	 * 客户端通信线程
	 */
	private JList<String> listUsers;
	private Thread thread=null;
	/**
	 * 通信套接字
	 */
	private  Socket socket=null;
	
	/**
	 * 基本数据输入流
	 */
	private DataInputStream dis=null;
	
	/**
	 * 基本数据输出流
	 */
	private DataOutputStream dos=null;
	/**
	 * 是否登录
	 */
	private boolean isLogged;
	private DefaultListModel<String> modelUsers;//数据模型，用于更新数据
	private JTextArea textAreaRecord;//文本框
	private ClientWindow jc;
	public ChatClient(JTextArea textAreaRecord,JList<String> listUsers,ClientWindow cw){
		this.textAreaRecord=textAreaRecord;
		this.jc=cw;
		modelUsers = new DefaultListModel<String>();//初始化成员变量
		listUsers.setModel(modelUsers);
	}
	public void Start() {
		if(thread==null){
			 
		thread=new Thread(){
		@Override
			public void run() {
			// TODO Auto-generated method stub
				super.run();
			// 连接服务器并登录
				while(isLogged) {
							try {
								String msg = dis.readUTF();
							
								String[] parts = msg.split("#");
								switch (parts[0]) {
								// 处理服务器发来的用户列表报文
								case "USERLIST":
									for(int i = 1; i< parts.length; i++) {
										modelUsers.addElement(parts[i]);
									}
									break;
								// 处理服务器发来的新用户登录表报文
								case "LAND":
									modelUsers.addElement(parts[1]);
									break;
								//登出
								case "LOGOUT":
									modelUsers.removeElement(parts[1]);
									break;
								//接收群聊消息
								case "TALKTO_ALL":
									addMsg(parts[1]+"跟所有人说:"+parts[2]);
									
									break;
								//接收私聊消息
								case "TALKTO":
									
									addMsg(parts[1]+"跟我说:"+parts[2]);
									break;
								//接受文件
                                 case "FILE":
//                              
                                 int result = JOptionPane.showConfirmDialog(
                                             jc,
                                             "确认接受文件？",
                                             "提示",
                                             JOptionPane.YES_NO_CANCEL_OPTION);
                                	  if(result==0){
                                	  	   JFileChooser fileChooser=new JFileChooser();
                                	  	   fileChooser.setCurrentDirectory(new File(""));
                                	  	   fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);
                                	  	   int results = fileChooser.showOpenDialog(jc);
                                	  	   if(results==0){
                                				long fileLength = dis.readLong();
                                		        File file = new File(fileChooser.getSelectedFile().getPath()+"/"+parts[1]);
                                		        OutputStream  fos = new FileOutputStream(file);
                                		        byte[] bytes = new byte[1024];
                                		        int length = 0;
                                		        while((length = dis.read(bytes, 0, bytes.length))>=-1) {
                                		             fos.write(bytes, 0, length);
                                		             fos.flush();
                                		             if(length<bytes.length){
                                		                  fos.close();
                                                          addMsg("文件完成");
                                		             }
                                		        }      
                                		    } 	
                                	  	}
									break;
									
								default:
									break;
								}
							} catch (IOException e) {
								// TODO 处理异常
								isLogged = false;
							}
						}
			
					}		
				};
				thread.start();	
			}	
		}
	
	public void sendChatMag(String text,String toUsername,boolean isSelected){//发送消息
		String msgChat=null;
		if(!isSelected){//如果不选择私聊则所有人都可接受消息
			msgChat="TALKTO_ALL#"+text;	
	
		}
		if(isSelected){//选择私聊则只能特定的人接受该消息
			msgChat="TALKTO#"+toUsername+"#"+text;
		}
		if(null!=msgChat){//文本框不为空，为发送消息
			try {
				addMsg("我："+text);
				dos.writeUTF(msgChat);//数据输出为msgchat中的消息
				dos.flush();//刷新
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void land(String serverIp,String username,String pass) throws IOException{//登陆
		 if(socket!=null){
			return;
		}
		// 获取服务器IP地址和端口
		// 连接服务器，获取套接字IO流
		socket = new Socket(serverIp, 8808);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		// 获取用户名，构建、发送登录报文
		String msgLogin = "LAND#" + username+":"+pass;
		dos.writeUTF(msgLogin);
		dos.flush();
		// 读取服务器返回的信息，判断是否登录成功
		String response = dis.readUTF();
		// 登录失败
		if(response.equals("FAIL")) {
			addMsg("登录服务器失败");
			// 登录失败，断开连接，结束客户端线程
			socket.close();
			return;
		}
		// 登录成功
		if(response.equals("SUCCESS")) {
			addMsg("登录服务器成功");
			isLogged = true;
		}
	}
	public void login(String serverIp,String username,String pass)throws IOException{//注册
		// 获取服务器IP地址和端口
		// 连接服务器，获取套接字IO流
		socket = new Socket(serverIp, 8808);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		// 获取用户名密码，构建、发送注册报文
		String msgLogin = "LOGIN#" + username+":"+pass;
		dos.writeUTF(msgLogin);
		dos.flush();
		// 读取服务器返回的信息，判断是否注册成功
		String response = dis.readUTF();
		// 注册失败
		if(response.equals("ERROR")) {
			addMsg("注册服务器失败");
			socket.close();
			socket=null;
			return;
		}
		if(response.equals("SUCCESS")) {
			addMsg("注册服务器成功");
			socket.close();
			socket=null;
		}
		dos.close();
		dis.close();
		
		
	}
	public void sendfile(final String path,final String toUsername){//发送文件
		new Thread(){
		public void	run(){
			
    		
		 int length = 0;
		File file = new File(path);
		try{
		dos.writeUTF("FILE#"+toUsername+"#"+file.getName());
    	dos.writeLong(file.length());
    	dos.flush();
       InputStream fin = new FileInputStream(file);
       byte[] sendByte = new byte[1024];
        while((length = fin.read(sendByte, 0, sendByte.length))!=-1){
        	
        	   dos.write(sendByte,0,length);
               dos.flush();
               
        }  
             addMsg("文件发送成功");
        fin.close();
		}catch (Exception e) {
			addMsg("文件发送失败....");
			e.printStackTrace();
			// TODO: handle exception
		}}
		}.start();
	}
	public void landout(){//登出
		String msgLogin = "LOGOUT#";
		try {
			if(dos!=null){
			dos.writeUTF(msgLogin);
			dos.flush();}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void addMsg(String msg) {//发送消息
		// 在文本区中添加一条消息，并加上换行
		textAreaRecord.append(msg + "\n");
				// 自动滚动到文本区的最后一行
		textAreaRecord.setCaretPosition(textAreaRecord.getText().length());
			
	}

}
