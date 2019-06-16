package com.client;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ClientWindow extends JFrame implements ActionListener{
	private JTextField jt_user;
	private JPasswordField jt_pass;
	JList<String> jlist=null;
	private String ip;
	private JCheckBox jc;
	private String touser="";
	private ChatClient chat;
	private JTextField context;
	private JButton land;
	private JButton login;
	private JButton send;
	public ClientWindow(){
		this.setBounds(200, 300, 600, 500);
		Layout();
		
		this.setLocationRelativeTo(null);
	    this.setVisible(true);
	    this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowClosing(arg0);
				
				chat.landout();
			}
			
			
		});
	}
        
	
	public void Layout(){
		 // 创建内容面包容器，指定使用 边界布局
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT,11,5));
        JPanel bto=new JPanel(new FlowLayout(FlowLayout.LEFT,11,5));
        JLabel username=new JLabel("用户名：");
        jt_user = new JTextField();
//        jt_user.setText("yonghu"+(int)(Math.random()*100));
        jt_user.setPreferredSize(new Dimension(150, 30));
        JLabel pas=new JLabel("密码：");
        jt_pass = new  JPasswordField();
//        jt_pass.setText("yonghu"+(int)(Math.random()*100));
//        jt_user.setText("yonghu"+(int)(Math.random()*100));
        jt_pass.setPreferredSize(new Dimension(150, 30));
        land = new JButton("登陆");
        land.setActionCommand("0x001");
        land.addActionListener(this);
        land.setPreferredSize(new Dimension(60, 30));
        login = new JButton("注册");
        login.setActionCommand("0x002");
        login.addActionListener(this);
        login.setPreferredSize(new Dimension(60, 30));
        context = new JTextField();
        context.setPreferredSize(new Dimension(270, 30));
        jc = new JCheckBox("私聊");
        send = new JButton("发送消息");
        send.setEnabled(false);
        send.setActionCommand("0x003");
        send.addActionListener(this);
        final JButton sendFile = new JButton("发送文件");
        sendFile.setEnabled(false);
        sendFile.setActionCommand("0x004");
        sendFile.addActionListener(this);
        panel.setLayout(new BorderLayout());
		JSplitPane jsp=new JSplitPane();
		JPanel left=new JPanel();
		JPanel right=new JPanel();
		JTextArea jta=new JTextArea();
		jta.setPreferredSize(new Dimension(260, 335));
		jlist=new JList<>();
		jlist.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				sendFile.setEnabled(false);
				if(jlist.getSelectedIndex()>-1&&
				jlist.getModel().getSize()>1&&!jt_user.getText().equals(jlist.getSelectedValue())
				){
					touser=jlist.getSelectedValue();
					sendFile.setEnabled(true);
				}
				};
			
		});
		JScrollPane JST=new JScrollPane(jlist);
		JST.setPreferredSize(new Dimension(280, 340));
		jta.setFont(new Font("宋体", Font.PLAIN, 18));
		jta.setEditable(false);
		JScrollPane jsl=new JScrollPane(jta);
		jsl.setPreferredSize(new Dimension(270, 340));
		left.setBorder(BorderFactory.createTitledBorder("消息记录"));
		right.setBorder(BorderFactory.createTitledBorder("在线用户"));
		jta.setLineWrap(true);
		left.add(jsl); 
		right.add(JST);
		// 设置分隔条的初始位置
        jsp.setDividerLocation(280);
		jsp.setLeftComponent(left);
		jsp.setRightComponent(right);
		panel.add(top,BorderLayout.NORTH);
		panel.add(jsp,BorderLayout.CENTER);
		panel.add(bto,BorderLayout.SOUTH);
		
		top.add(username);
		top.add(jt_user);
		top.add(pas);
		top.add(jt_pass);
		top.add(land);
		top.add(login);
		bto.add(context);
		bto.add(jc);
		bto.add(send);
		bto.add(sendFile);
		this.add(panel);
		chat = new ChatClient(jta,jlist,this);
	
	}
	public static void main(String[] args) {
		new ClientWindow();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String id=e.getActionCommand();
		//land
		if(id.equals("0x001")){
			
			try {
				InetAddress addr = InetAddress.getLocalHost(); 
		        String ip=addr.getHostAddress().toString();
						chat.land(ip,jt_user.getText(),jt_pass.getText());
						chat.Start();
						land.setEnabled(false);
						login.setEnabled(false);
						send.setEnabled(true);
						jt_user.setEditable(false);
						jt_pass.setEditable(false);
						
					} catch (Exception ex) {
						chat.addMsg("连接登录服务器时出现异常");
						ex.printStackTrace();
						return;
					}
			//login
		}else if(id.equals("0x002")){
			this.dispose();
			new Login(chat);
			//发送消息
		}else if(id.equals("0x003")){
			chat.sendChatMag(context.getText(),touser,jc.isSelected());
			context.setText("");
			//发文件
		}else if(id.equals("0x004")){
			JFileChooser fileChooser=new JFileChooser();
			 fileChooser.setCurrentDirectory(new File(""));
			 int result = fileChooser.showOpenDialog(this);
			 if(result==0){fileChooser.getSelectedFile().getPath();
			 chat.sendfile(fileChooser.getSelectedFile().getPath(),touser);
			 
			 }
		}
		
		
	}
	
	
	
}
