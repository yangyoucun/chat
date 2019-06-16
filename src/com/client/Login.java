package com.client;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class Login extends JFrame implements ActionListener{
	
private JTextField jt_user;
private JPasswordField jp_pas;
private JPasswordField jp_repas;
private ChatClient chat;
public Login(ChatClient chat){
		this.chat=chat;
		this.setBounds(200, 300,370, 370);
		Layout();
		this.setLocationRelativeTo(null);
	    this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}
     public void Layout(){
    	 
    	 // 创建内容面包容器，指定使用 边界布局
         JPanel panel = new JPanel(null);
         JLabel username=new JLabel("用户名:");
         username.setBounds(10, 10, 50, 30);
         jt_user = new JTextField();
          jt_user.setBounds(10, 40, 330, 30);
         JLabel pas=new JLabel("登陆密码:");
         pas.setBounds(10, 70, 60, 30);
         jp_pas = new JPasswordField();
         jp_pas.setBounds(10, 100, 330, 30);
         JLabel repas=new JLabel("确认密码:");
         repas.setBounds(10, 130, 60, 30);
         jp_repas = new JPasswordField();
         jp_repas.setBounds(10, 160, 330, 30);
         JPanel bot = new JPanel(new BorderLayout(21,0));
         bot.setBounds(10, 270, 330, 30);
         JButton login=new JButton("注册");
         login.addActionListener(this);
         login.setActionCommand("x001");
         login.setPreferredSize(new Dimension(96, 30));
         JButton reset=new JButton("清零");
         reset.addActionListener(this);
         reset.setActionCommand("x002");
         reset.setPreferredSize(new Dimension(96, 30));
         JButton back=new JButton("退出");
         back.addActionListener(this);
         back.setActionCommand("x003");
         back.setPreferredSize(new Dimension(96, 30));
         bot.add(login,BorderLayout.WEST);
         bot.add(reset,BorderLayout.CENTER);
         bot.add(back,BorderLayout.EAST);
    	 panel.add(username);
    	 panel.add(jt_user);
         panel.add(pas);
         panel.add(jp_pas);
         panel.add(repas);
         panel.add(jp_repas);
         panel.add(bot); 
         this.add(panel); 
     }
	@Override
	public void actionPerformed(ActionEvent e) {
		String id=e.getActionCommand();
		if(id.equals("x001")){
			
				if(!jt_user.getText().equals("")&&
				!jp_pas.getText().equals("")&&
				!jp_repas.getText().equals("")){
				if(jp_pas.getText().equals(jp_repas.getText())){
					try{		
						InetAddress addr = InetAddress.getLocalHost(); 
						String ip=addr.getHostAddress().toString();
						chat.login(ip, jt_user.getText(), jp_pas.getText());
					}catch(Exception ex){
						ex.printStackTrace();
					}
						
						
					}else{
						
						JOptionPane.showMessageDialog(
								this,
			                    "密码不一致",
			                    "错误",
			                    JOptionPane.WARNING_MESSAGE
			            );
						
					}
					
					
				}else{
					
					
					JOptionPane.showMessageDialog(
							this,
		                    "输入不为空",
		                    "错误",
		                    JOptionPane.WARNING_MESSAGE
		            );
				}
			
			
		}else if(id.equals("x002")){
			jt_user.setText("");
			jp_pas.setText("");
			jp_repas.setText("");
			
			
		}else if(id.equals("x003")){
			new ClientWindow();
			this.dispose();	
		}
	
	}
}
