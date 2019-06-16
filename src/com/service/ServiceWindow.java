package com.service;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.client.User;


public class ServiceWindow extends JFrame implements ActionListener{
	
	// 表格所有行数据
	Vector vData = new Vector();
	private JTable table;
	int index=-1;
	private JTextArea jta;
	ChatServer chatserver;
	private JButton start;
	public ServiceWindow(){//服务器窗口
		this.setBounds(200, 300, 600, 500);
		Layout();
		this.setLocationRelativeTo(null);
	    this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}
	public void Layout(){//窗口布局
		 // 创建内容面包容器，指定使用 边界布局
        JPanel panel = new JPanel(new BorderLayout());
        JPanel bto=new JPanel();
        start = new JButton("启动");
        start.setActionCommand("0x111");
        start.addActionListener(this);
        JButton del = new JButton("删除用户");
        del.setActionCommand("0x112");
        del.addActionListener(this);
        panel.setLayout(new BorderLayout());
		JSplitPane jsp=new JSplitPane();
		JPanel left=new JPanel();
		JPanel right=new JPanel();
		jta = new JTextArea(15,24);
		DefaultTableModel dtm= new DefaultTableModel(null,ChatServer.getData());
		table = new JTable(dtm); 
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		
		cellSelectionModel.addListSelectionListener(new ListSelectionListener(){
 
			public void valueChanged(ListSelectionEvent e) {//单元格值变动事件
				String selectedData = null;
				int selectedRow = table.getSelectedRow();//被选择的行
			    if(selectedRow>-1){
			    	index=selectedRow;
			       }
				
			}
	         
		
		
		});
		JScrollPane JST=new JScrollPane(table);
		JST.setPreferredSize(new Dimension(280, 380));
		jta.setFont(new Font("宋体", Font.PLAIN, 18));
		
		jta.setEditable(false);
		JScrollPane jsl=new JScrollPane(jta);
		jsl.setPreferredSize(new Dimension(270, 380));
		left.setBorder(BorderFactory.createTitledBorder("消息记录"));
		right.setBorder(BorderFactory.createTitledBorder("在线用户"));
		jta.setLineWrap(true);
		left.add(jsl); 
		right.add(JST);
		// 设置分隔条的初始位置
        jsp.setDividerLocation(280);
		jsp.setLeftComponent(left);
		jsp.setRightComponent(right);
		panel.add(jsp,BorderLayout.NORTH);
		panel.add(bto,BorderLayout.SOUTH);
		bto.add(start);
		bto.add(del);
		this.add(panel);
		chatserver=new ChatServer(jta,table,vData,this);
		
	}
    @Override
	public void actionPerformed(ActionEvent e) {
	String id=	e.getActionCommand();
	if(id.equals("0x111")){//启动服务器
		chatserver.startServer();
		start.setText("关闭");//设置按钮为关闭
		start.setActionCommand("0x113");
	}
	else
	if(id.equals("0x112")){//在服务器端移除客户端成员
		if(index>-1){
			vData.remove(index);
			index=-1;
			DefaultTableModel dtm= new DefaultTableModel(vData,ChatServer.getData());
			table.setModel(dtm);
		}else{
		}}else
	if(id.equals("0x113")){//退出服务器
		chatserver.closeserver();
		start.setText("开启");
		start.setActionCommand("0x111");
		}
	}
    
	public static void main(String[] args) {
    	new ServiceWindow();
		
		
    		
    }

}
