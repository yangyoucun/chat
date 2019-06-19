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
	
	// ������ڹ���ʵ�ֵĳ�Ա����
	/**
	 * �������׽���
	 */
	private ServerSocket server;
	/**
	 * �жϷ������Ƿ�������
	 */
	private boolean isRunning=false;
	/**
	 * �ͻ���ӳ�䣬key -> String���ͻ������ƣ� value -> ClientHandler�� �ͻ��˴����߳�
	 */
	private HashMap<String, ClientHandler> clientHandlerMap = new HashMap<String, ClientHandler>();
	private JTextArea textAreaRecord;//�ı���
	private JTable jt;//�����û��б�
	private Vector vData;//�û�����
	
	public ChatServer(JTextArea textAreaRecord,JTable jt,Vector vData,ServiceWindow serviceWindow) {//����������
		this.textAreaRecord=textAreaRecord;//�ı���
		this.jt=jt;
		this.vData=vData;
		try {
			server = new ServerSocket(8808);//�½�һ�����������׽���
			serviceWindow.setVisible(true);
		} catch (IOException e) {
			 JOptionPane.showMessageDialog(
					serviceWindow,
                    "�˿���ռ��",
                    "����",
                    JOptionPane.WARNING_MESSAGE
            );
			 serviceWindow.setVisible(false);
			 isRunning = false;
		}
	}
	public void startServer() {//����������
		
		isRunning = true;
		new Thread(){
			public void run() {
				addMsg("�����������ɹ�");//������������ʱ������Ϣ
				// ����������������״̬ʱ��ѭ�������ͻ��˵���������
				while(isRunning) {
					try {
						Socket socket = server.accept();//����������ȴ�״̬
						// ������ͻ��˽������߳�
						Thread thread = new Thread(new ClientHandler(socket));
						thread.start();//�����߳�
					} catch (IOException e) {
						System.out.println("��û����");
					}
				}
			}
			
		}.start();
	}
	public static Vector  getData(){//��ȡ�û����ݣ�����Vector������
		Vector vName = new Vector();
		vName.add("�û���");
		vName.add("IP��ַ");
		vName.add("�˿�");
		vName.add("��½ʱ��");
		
		return vName;
	}
	//��ͻ��˽�������
	class ClientHandler implements Runnable {
		private Socket socket;//�׽���
		private DataInputStream dis;//����������
		private DataOutputStream dos;//���������
		private boolean isConnected;//�Ƿ�����
		private String username;//�û���Ϣ
		//��ȡ��ʽ���ĵ�ǰʱ���ַ���
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		public ClientHandler(Socket socket) {
			this.socket = socket;
			try {
			
				this.dis = new DataInputStream(socket.getInputStream());//����socket���������󣬶�ȡ�ͻ�����Ϣ
				this.dos = new DataOutputStream(socket.getOutputStream());//����socket���������
				isConnected = true;
			} catch (IOException e) {
				isConnected = false;
				e.printStackTrace();
			}
		}

		
		public void run() {//�����߳�
			while(isRunning && isConnected) {
				try {
					// ��ȡ�ͻ��˷��͵ı���
					String msg = dis.readUTF();
					String[] parts = msg.split("#");//���ͻ��˷��͹�������Ϣ�����part������
					switch (parts[0]) {
					case "LOGIN"://ע��
						String landinfo = parts[1];//�����û���Ϣ
						String landUsername=landinfo.split(":")[0];
						String landpass=landinfo.split(":")[1];
						DB insert=new DB();
						boolean stus=insert.update("INSERT INTO `userinfo` (`username`, `password`) VALUES ('"+landUsername+"', '"+landpass+"')");
						if(stus){//����û�ע��ɹ��򷵻�success���Ѵ����򷵻�error
						dos.writeUTF("SUCCESS");
						
					   }else{
						dos.writeUTF("ERROR");	
						
					}
					insert.close();
					dos.flush();
					socket.shutdownInput();
					socket.shutdownOutput();
					break;
					
					// �����¼����
					case "LAND":
						String logininfo = parts[1];
						String loginUsername=logininfo.split(":")[0];
						String loginpass=logininfo.split(":")[1];
						// ������û����ѵ�¼���򷵻�ʧ����Ϣ�����򷵻سɹ���Ϣ
						if(clientHandlerMap.containsKey(loginUsername)) {
							dos.writeUTF("FAIL");
						} else {
							 DB db=new DB();
							 ResultSet rs=db.gets("select * from `userinfo` where username='"
							+loginUsername+"'and password='"+loginpass+"'");//�����ݿ��л�ȡ�û���Ϣ
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
	           							// ���˿ͻ��˴����̵߳���Ϣ��ӵ�clientHandlerMap��
	           							clientHandlerMap.put(loginUsername, this);
	           							// �������û�����Ϣ�������û�
	           							StringBuffer msgUserList = new StringBuffer();
	           							msgUserList.append("USERLIST#");
	           							for(String username : clientHandlerMap.keySet()) {
	           								msgUserList.append(username + "#");
	           							}
	           							dos.writeUTF(msgUserList.toString());
	           							// ���µ�¼���û���Ϣ�㲥�������û�
	           							String msgLogin = "LAND#" + loginUsername;
	           							broadcastMsg(loginUsername, msgLogin);
	           							// �洢��¼���û���
	           							this.username = loginUsername;   
                                   }
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							db.close();
						}
						
						break;
					case "LOGOUT"://�ǳ�
						clientHandlerMap.remove(username);//��clientHandleMap���Ƴ��û�
						String msgLogout="LOGOUT#"+username;//���������ͻ��˷���LOGOUT����
						addMsg(username+"������");
						broadcastMsg(username, msgLogout);
						isConnected=false;//�Ͽ�����
						socket.close();//�ر��׽���
						
						break;
					case "TALKTO_ALL"://Ⱥ��
						String msgTalkToAll="TALKTO_ALL#"+username+"#"+parts[1];//���ͻ��˷���TALKTO_ALL���ĺ���Ϣ����
						broadcastMsg(username, msgTalkToAll);//����Ϣ�㲥���͸�������
						break;
					case "TALKTO"://˽��
						ClientHandler clientHandler=clientHandlerMap.get(parts[1]);//��ȡ�ͻ��˵ĵ�ַ
						if(null!=clientHandler){
							String msgTalkTo="TALKTO#"+username+"#"+parts[2];//���ͻ��˷���TALKTO���ĺ���Ϣ����
							clientHandler.dos.writeUTF(msgTalkTo);//���ͻ���д����Ϣ
							clientHandler.dos.flush();
						}
						break;
                       case "FILE"://�ļ�
						ClientHandler file=clientHandlerMap.get(parts[1]);//��ȡ�ļ�
						if(null!=file){
							String msgTalkTo="FILE#"+parts[2];//���ͻ��˷���FILE���ĺ��ļ�
							int length = 0;
								try{
									file.dos.writeUTF(msgTalkTo);//���ͻ���д����Ϣ
									long fileLength = dis.readLong();//�ļ�����
									file.dos.writeLong(fileLength);
									  
									byte[] sendByte = new byte[1024];
								     
								    while((length = dis.read(sendByte, 0, sendByte.length))!=-1){//���ļ��е����ݴ���sendbyte�����У��ٽ�sendbyte�е�ÿһ���ֱ�ֵ��length���������ֵ����ôlength�Ͳ�����-1����ô�ͻ�ѭ���ļ�����ȡ��ֱ����ȡ��ֵΪֹ��
								        file.dos.write(sendByte,0,length);//���ļ�����д��sendbyte������
								        file.dos.flush(); 
								    }  
										
						       
								}catch (Exception e) {
									addMsg("�ļ��������");
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
		 * ��ĳ���û���������Ϣ�㲥�������û�
		 * @param fromUsername ������Ϣ���û�
		 * @param msg ��Ҫ�㲥����Ϣ
		 */
		private void broadcastMsg(String fromUsername, String msg) throws IOException{
			for(String toUserName : clientHandlerMap.keySet()) {
				if(fromUsername.equals(toUserName) == false) {//�������˽��
					DataOutputStream dos = clientHandlerMap.get(toUserName).dos;//�������������
					dos.writeUTF(msg);//���ͻ���д��Ϣ
					dos.flush();
				}
			}
		}
	}
	public void closeserver(){
		isRunning = false;
		// �رշ������׽��֣���տͻ���ӳ��
		try {
			
			clientHandlerMap.clear();
			addMsg("�������رճɹ�");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * �����Ϣ���ı���textAreaRecord
	 * @param msg��Ҫ��ӵ���Ϣ
	 */
	private void addMsg(String msg) {
		// ���ı��������һ����Ϣ�������ϻ���
		textAreaRecord.append(msg + "\n");
		// �Զ��������ı��������һ��
		textAreaRecord.setCaretPosition(textAreaRecord.getText().length());
	}

}
