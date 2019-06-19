package com.service;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import com.db.DB;

public class ChatServer {
	
	// 添加用于功能实现的成员变量
	/**
	 * 服务器套接字
	 */
	private ServerSocket server;
	/**
	 * 判断服务器是否在运行
	 */
	private boolean isRunning=false;
	/**
	 * 客户端映射，key -> String：客户端名称； value -> ClientHandler： 客户端处理线程
	 */
	private HashMap<String, ClientHandler> clientHandlerMap = new HashMap<String, ClientHandler>();
	private JTextArea textAreaRecord;//文本框
	private JTable jt;//在线用户列表
	private Vector vData;//用户数据
	
	public ChatServer(JTextArea textAreaRecord,JTable jt,Vector vData,ServiceWindow serviceWindow) {//创建服务器
		this.textAreaRecord=textAreaRecord;//文本框
		this.jt=jt;
		this.vData=vData;
		try {
			server = new ServerSocket(8808);//新建一个服务器端套接字
			serviceWindow.setVisible(true);
		} catch (IOException e) {
			 JOptionPane.showMessageDialog(
					serviceWindow,
                    "端口已占用",
                    "错误",
                    JOptionPane.WARNING_MESSAGE
            );
			 serviceWindow.setVisible(false);
			 isRunning = false;
		}
	}
	public void startServer() {//启动服务器
		
		isRunning = true;
		new Thread(){
			public void run() {
				addMsg("服务器启动成功");//服务器被启动时发送消息
				// 当服务器处于运行状态时，循环监听客户端的连接请求
				while(isRunning) {
					try {
						Socket socket = server.accept();//服务器进入等待状态
						// 创建与客户端交互的线程
						Thread thread = new Thread(new ClientHandler(socket));
						thread.start();//启动线程
					} catch (IOException e) {
						System.out.println("还没连接");
					}
				}
			}
			
		}.start();
	}
	public static Vector  getData(){//获取用户数据，放入Vector数组中
		Vector vName = new Vector();
		vName.add("用户名");
		vName.add("IP地址");
		vName.add("端口");
		vName.add("登陆时间");
		
		return vName;
	}
	//与客户端交互的类
	class ClientHandler implements Runnable {
		private Socket socket;//套接字
		private DataInputStream dis;//数据输入流
		private DataOutputStream dos;//数据输出流
		private boolean isConnected;//是否连接
		private String username;//用户信息
		//获取格式化的当前时间字符串
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		public ClientHandler(Socket socket) {
			this.socket = socket;
			try {
			
				this.dis = new DataInputStream(socket.getInputStream());//创建socket输入流对象，读取客户端信息
				this.dos = new DataOutputStream(socket.getOutputStream());//创建socket输出流对象
				isConnected = true;
			} catch (IOException e) {
				isConnected = false;
				e.printStackTrace();
			}
		}

		
		public void run() {//启动线程
			while(isRunning && isConnected) {
				try {
					// 读取客户端发送的报文
					String msg = dis.readUTF();
					String[] parts = msg.split("#");//将客户端发送过来的消息存放在part数组中
					switch (parts[0]) {
					case "LOGIN"://注册
						String landinfo = parts[1];//构建用户信息
						String landUsername=landinfo.split(":")[0];
						String landpass=landinfo.split(":")[1];
						DB insert=new DB();
						boolean stus=insert.update("INSERT INTO `userinfo` (`username`, `password`) VALUES ('"+landUsername+"', '"+landpass+"')");
						if(stus){//如果用户注册成功则返回success，已存在则返回error
						dos.writeUTF("SUCCESS");
						
					   }else{
						dos.writeUTF("ERROR");	
						
					}
					insert.close();
					dos.flush();
					socket.shutdownInput();
					socket.shutdownOutput();
					break;
					
					// 处理登录报文
					case "LAND":
						String logininfo = parts[1];
						String loginUsername=logininfo.split(":")[0];
						String loginpass=logininfo.split(":")[1];
						// 如果该用户名已登录，则返回失败信息，否则返回成功信息
						if(clientHandlerMap.containsKey(loginUsername)) {
							dos.writeUTF("FAIL");
						} else {
							 DB db=new DB();
							 ResultSet rs=db.gets("select * from `userinfo` where username='"
							+loginUsername+"'and password='"+loginpass+"'");//从数据库中获取用户信息
							try {
                                   if(rs.first()){
                                	    Vector vRow = new Vector();
	           							dos.writeUTF("SUCCESS");
	           							vRow.add(loginUsername);
	           							vRow.add(socket.getLocalAddress().getHostAddress());
	           						    vRow.add(socket.getPort());
	           						    vRow.add(dateString);
	           						    vData.add(vRow);
	           						    DefaultTableModel dtm= new DefaultTableModel(vData,getData());
	           							jt.setModel(dtm);
	           							// 将此客户端处理线程的信息添加到clientHandlerMap中
	           							clientHandlerMap.put(loginUsername, this);
	           							// 将现有用户的信息发给新用户
	           							StringBuffer msgUserList = new StringBuffer();
	           							msgUserList.append("USERLIST#");
	           							for(String username : clientHandlerMap.keySet()) {
	           								msgUserList.append(username + "#");
	           							}
	           							dos.writeUTF(msgUserList.toString());
	           							// 将新登录的用户信息广播给其他用户
	           							String msgLogin = "LAND#" + loginUsername;
	           							broadcastMsg(loginUsername, msgLogin);
	           							// 存储登录的用户名
	           							this.username = loginUsername;   
                                   }
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							db.close();
						}
						
						break;
					case "LOGOUT"://登出
						clientHandlerMap.remove(username);//在clientHandleMap中移除用户
						String msgLogout="LOGOUT#"+username;//构建、给客户端发送LOGOUT报文
						addMsg(username+"已下线");
						broadcastMsg(username, msgLogout);
						isConnected=false;//断开连接
						socket.close();//关闭套接字
						
						break;
					case "TALKTO_ALL"://群聊
						String msgTalkToAll="TALKTO_ALL#"+username+"#"+parts[1];//给客户端发送TALKTO_ALL报文和消息内容
						broadcastMsg(username, msgTalkToAll);//将信息广播发送给其他人
						break;
					case "TALKTO"://私聊
						ClientHandler clientHandler=clientHandlerMap.get(parts[1]);//获取客户端的地址
						if(null!=clientHandler){
							String msgTalkTo="TALKTO#"+username+"#"+parts[2];//给客户端发送TALKTO报文和消息内容
							clientHandler.dos.writeUTF(msgTalkTo);//给客户端写入消息
							clientHandler.dos.flush();
						}
						break;
                       case "FILE"://文件
						ClientHandler file=clientHandlerMap.get(parts[1]);//获取文件
						if(null!=file){
							String msgTalkTo="FILE#"+parts[2];//给客户端发送FILE报文和文件
							int length = 0;
								try{
									file.dos.writeUTF(msgTalkTo);//给客户端写入消息
									long fileLength = dis.readLong();//文件长度
									file.dos.writeLong(fileLength);
									  
									byte[] sendByte = new byte[1024];
								     
								    while((length = dis.read(sendByte, 0, sendByte.length))!=-1){//将文件中的数据存入sendbyte数组中，再将sendbyte中的每一个分别赋值给length。如果还有值，那么length就不等于-1，那么就会循环的继续读取，直到读取完值为止。
								        file.dos.write(sendByte,0,length);//将文件内容写入sendbyte数组中
								        file.dos.flush(); 
								    }  
										
						       
								}catch (Exception e) {
									addMsg("文件传输完成");
									// TODO: handle exception
								}
								
							
							
						}
						break;
						
						
						

					default:
						break;
					}
				} catch (IOException e) {
					isConnected = false;
					
				}
			}
		}
		
		/**
		 * 将某个用户发来的消息广播给其它用户
		 * @param fromUsername 发来消息的用户
		 * @param msg 需要广播的消息
		 */
		private void broadcastMsg(String fromUsername, String msg) throws IOException{
			for(String toUserName : clientHandlerMap.keySet()) {
				if(fromUsername.equals(toUserName) == false) {//如果不是私聊
					DataOutputStream dos = clientHandlerMap.get(toUserName).dos;//创建数据输出流
					dos.writeUTF(msg);//给客户端写消息
					dos.flush();
				}
			}
		}
	}
	public void closeserver(){
		isRunning = false;
		// 关闭服务器套接字，清空客户端映射
		try {
			
			clientHandlerMap.clear();
			addMsg("服务器关闭成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 添加消息到文本框textAreaRecord
	 * @param msg，要添加的消息
	 */
	private void addMsg(String msg) {
		// 在文本区中添加一条消息，并加上换行
		textAreaRecord.append(msg + "\n");
		// 自动滚动到文本区的最后一行
		textAreaRecord.setCaretPosition(textAreaRecord.getText().length());
	}

}
