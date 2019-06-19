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
	
	// �����Ա����
	/**
	 * �ͻ���ͨ���߳�
	 */
	private JList<String> listUsers;
	private Thread thread=null;
	/**
	 * ͨ���׽���
	 */
	private  Socket socket=null;
	
	/**
	 * ��������������
	 */
	private DataInputStream dis=null;
	
	/**
	 * �������������
	 */
	private DataOutputStream dos=null;
	/**
	 * �Ƿ��¼
	 */
	private boolean isLogged;
	private DefaultListModel<String> modelUsers;//����ģ�ͣ����ڸ�������
	private JTextArea textAreaRecord;//�ı���
	private ClientWindow jc;
	public ChatClient(JTextArea textAreaRecord,JList<String> listUsers,ClientWindow cw){
		this.textAreaRecord=textAreaRecord;
		this.jc=cw;
		modelUsers = new DefaultListModel<String>();//��ʼ����Ա����
		listUsers.setModel(modelUsers);
	}
	public void Start() {
		if(thread==null){
			 
			thread=new Thread(){
				@Override
				public void run() {
				// TODO Auto-generated method stub
					super.run();
					// ���ӷ���������¼
					while(isLogged) {
							try {
								String msg = dis.readUTF();//�ͻ��˶�������������Ϣ
							
								String[] parts = msg.split("#");//part����洢�ӷ�������ȡ������Ϣ
								switch (parts[0]) {
								// ����������������û��б���
								case "USERLIST":
									for(int i = 1; i< parts.length; i++) {
										modelUsers.addElement(parts[i]);
									}
									break;
								// ������������������û���¼����
								case "LAND":
									modelUsers.addElement(parts[1]);
									break;
								//�ǳ�
								case "LOGOUT":
									modelUsers.removeElement(parts[1]);
									break;
								//����Ⱥ����Ϣ
								case "TALKTO_ALL":
									addMsg(parts[1]+"��������˵:"+parts[2]);
									
									break;
								//����˽����Ϣ
								case "TALKTO":
									addMsg(parts[1]+"����˵:"+parts[2]);
									break;
								//�����ļ�
                                 case "FILE":                            
                                 int result = JOptionPane.showConfirmDialog(
                                             jc,
                                             "ȷ�Ͻ����ļ���",
                                             "��ʾ",
                                             JOptionPane.YES_NO_CANCEL_OPTION);
                                	  if(result==0){
                                	  	   JFileChooser fileChooser=new JFileChooser();
                                	  	   fileChooser.setCurrentDirectory(new File(""));//����Ĭ��Ŀ¼
                                	  	   fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);//����ʾĿ¼
                                	  	   int results = fileChooser.showOpenDialog(jc);//��
                                	  	   if(results==0){//�����ļ���
                                				long fileLength = dis.readLong();//��ȡ�ļ�����
                                		        File file = new File(fileChooser.getSelectedFile().getPath()+"/"+parts[1]);//ȷ���ļ�����λ��
                                		        OutputStream  fos = new FileOutputStream(file);//�����ļ������
                                		        byte[] bytes = new byte[1024];//�½�һ��byte����ļ�
                                		        int length = 0;
                                		        while((length = dis.read(bytes, 0, bytes.length))>=-1) {//���ļ��е����ݴ���bytes�����У�ѭ���ļ�����ȡ���ݣ�ֱ����ȡ��ֵΪֹ��
                                		             fos.write(bytes, 0, length);//���ַ���д���ļ�
                                		             fos.flush();
                                		             if(length<bytes.length){//�ļ�����С��byte���鳤�ȼ��������
                                		                  fos.close();
                                                          addMsg("�ļ����");
                                		             }
                                		        }      
                                		    } 	
                                	  	}
									break;
									
								default:
									break;
								}
							} catch (IOException e) {
								// TODO �����쳣
								isLogged = false;
							}
						}
			
					}		
				};
				thread.start();	
			}	
		}
	
	public void sendChatMag(String text,String toUsername,boolean isSelected){//������Ϣ
		String msgChat=null;//������Ϣ����
		if(!isSelected){//�����ѡ��˽���������˶��ɽ�����Ϣ
			msgChat="TALKTO_ALL#"+text;	
	
		}
		if(isSelected){//ѡ��˽����ֻ���ض����˽��ܸ���Ϣ
			msgChat="TALKTO#"+toUsername+"#"+text;
		}
		if(null!=msgChat){//ѡ���ض�����
			try {
				addMsg("�ң�"+text);//���ı�������һ����Ϣ
				dos.writeUTF(msgChat);//���������˷��ͱ��ĺ���Ϣ
				dos.flush();//ˢ��
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void land(String serverIp,String username,String pass) throws IOException{//��½
		 if(socket!=null){
			return;
		}
		// ��ȡ������IP��ַ�Ͷ˿�
		// ���ӷ���������ȡ�׽���IO��
		socket = new Socket(serverIp, 8808);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		// ��ȡ�û��������������͵�¼����
		String msgLogin = "LAND#" + username+":"+pass;
		dos.writeUTF(msgLogin);
		dos.flush();
		// ��ȡ���������ص���Ϣ���ж��Ƿ��¼�ɹ�
		String response = dis.readUTF();
		// ��¼ʧ��
		if(response.equals("FAIL")) {
			addMsg("��¼������ʧ��");
			// ��¼ʧ�ܣ��Ͽ����ӣ������ͻ����߳�
			socket.close();
			return;
		}
		// ��¼�ɹ�
		if(response.equals("SUCCESS")) {
			addMsg("��¼�������ɹ�");
			isLogged = true;
		}
	}
	public void login(String serverIp,String username,String pass)throws IOException{//ע��
		// ��ȡ������IP��ַ�Ͷ˿�
		// ���ӷ���������ȡ�׽���IO��
		socket = new Socket(serverIp, 8808);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		// ��ȡ�û������룬����������ע�ᱨ��
		String msgLogin = "LOGIN#" + username+":"+pass;
		dos.writeUTF(msgLogin);
		dos.flush();
		// ��ȡ���������ص���Ϣ���ж��Ƿ�ע��ɹ�
		String response = dis.readUTF();
		// ע��ʧ��
		if(response.equals("ERROR")) {
			addMsg("ע�������ʧ��");
			socket.close();
			socket=null;
			return;
		}
		if(response.equals("SUCCESS")) {
			addMsg("ע��������ɹ�");
			socket.close();
			socket=null;
		}
		dos.close();
		dis.close();
		
		
	}
	public void sendfile(final String path,final String toUsername){//�����ļ�
		new Thread(){
			public void	run(){
				int length = 0;//�����ļ�����
				File file = new File(path);
				try{
					dos.writeUTF("FILE#"+toUsername+"#"+file.getName());//�������������������ļ�����
					dos.writeLong(file.length());//�������������ļ�����
					dos.flush();
					InputStream fin = new FileInputStream(file);//�����ļ������
					byte[] sendByte = new byte[1024];
					while((length = fin.read(sendByte, 0, sendByte.length))!=-1){//���ļ��е����ݴ���sendbyte�����У��ٽ�sendbyte�е�ÿһ���ֱ�ֵ��length���������ֵ����ôlength�Ͳ�����-1����ô�ͻ�ѭ���ļ�����ȡ��ֱ����ȡ��ֵΪֹ��
        	
						dos.write(sendByte,0,length);//���ļ�����д��sendbyte������
						dos.flush();
               
					}  
				addMsg("�ļ����ͳɹ�");
				fin.close();
				}catch (Exception e) {
					addMsg("�ļ�����ʧ��....");
					e.printStackTrace();
					// TODO: handle exception
				}
			}
		}.start();
	}
	public void landout(){//�ǳ�
		String msgLogin = "LOGOUT#";//���������͵ǳ�����
		try {
			if(dos!=null){
			dos.writeUTF(msgLogin);
			dos.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void addMsg(String msg) {// ���ı��������һ����Ϣ�������ϻ���
		textAreaRecord.append(msg + "\n");
				// �Զ��������ı��������һ��
		textAreaRecord.setCaretPosition(textAreaRecord.getText().length());
			
	}

}
