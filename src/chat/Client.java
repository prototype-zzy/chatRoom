package chat;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;

import javax.management.loading.PrivateClassLoader;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.*;

import com.mysql.cj.xdevapi.AddResult;

import db.DBOperator;

//import org.omg.CORBA.INITIALIZE;

/**
 *  聊天室客户端
 * @author Administrator
 */
public class Client {

	private Socket socket;
	private JFrame frame;
	private JTextArea showMessage;
	private JTextArea inputMessage;
	private JScrollPane sp;
	private JButton btnNewButton;
	private String nickName;
	private String passWord;
	private OutputStream out;
	private OutputStreamWriter osw;
	private PrintWriter pw;
	private BufferedReader br;
	private DBOperator db;
	
	private JFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private String logOrReg;
    
	/**
	 *     构造函数，初始化
	 */
	public Client(){
		try {
			Properties properties = new Properties();
			properties.load(Files.newInputStream(Paths.get("config.properties")));
			String host = properties.getProperty("serverip");
			int port = Integer.parseInt(properties.getProperty("serverport"));
			socket = new Socket(host, port);
			
			db = new DBOperator();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		
	    EventQueue.invokeLater(new Runnable(){
	      
	            public void run(){
	                
	                try{
	                    Client window = new Client();
	                    window.start();
	                    window.frame.setTitle(window.nickName+"'s chatroom.");
	                    window.frame.setVisible(true);
	                    
	                }catch(Exception e){
	                    e.printStackTrace();
	                }
	            }
	    });
	    
	  
	}
	
	/**
     * start方法
     */
    public void start(){
    	
    	// 获取Id
    	int userId = 0;
//        
//        nickName = JOptionPane.showInputDialog("Please enter a nickname for this chat.");
//        nickName.trim();
//        // TODO: 检查用户名
//        while (nickName.length() == 0 || nickName.trim().equals("Please enter the nickname for this chat:")) {
//            JOptionPane.showMessageDialog(frame, "Please enter at least one character.");
//            nickName = JOptionPane.showInputDialog("Please enter a nickname for this chat.");
//            
//        }
//        
//        // TODO: 检查密码
//        passWord = JOptionPane.showInputDialog("Please enter the password of the user.");
//        while (passWord.length() < 6 || passWord.trim().equals("Please enter the password of the user:")) {
//            JOptionPane.showMessageDialog(frame, "Please enter at least 6 characters.");
//            passWord = JOptionPane.showInputDialog("Please enter the password of the user.");
//            
//        }
    	
    	loginOrRegister();
        
        initialize();
        //将nickName发送给服务器用于广播上线
        try {

			/*
			 * 通过socket获取输入流，循环读取服务器端发送过来的每一行字符串，并输出到控制台即可
			 */
			InputStream in = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(in,"UTF-8");
			br = new BufferedReader(isr);

            /*
             *  Socket的方法用于获取一个输出流，将数据发送给远端计算机
             */
             out = socket.getOutputStream();
             osw = new OutputStreamWriter(out,"UTF-8");
             pw = new PrintWriter(osw,true);

             /*
              * 首先使用pw发送一个字符串，这个字符串是昵称
              * */

			//Todo 登录验证， 使用sendLogin(String username, String password)
//			System.out.println("login: " + sendLogin("zzy", "123456"));  // 测试登录
			System.out.println("login: " + sendLogin(logOrReg, nickName, passWord));
//             pw.println(nickName);

			//获取历史消息记录
			String encodedHistory = br.readLine().trim();
			System.out.println(encodedHistory);
			String decode = new String(Base64.getDecoder().decode(encodedHistory), StandardCharsets.UTF_8);
			System.out.println(decode);
			showMessage.append(decode);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 启动用来读取服务器端消息的线程
         *
         */
        GetServerMessageHandler handler = new GetServerMessageHandler();
        Thread t = new Thread(handler);
        t.start();
        
    }


	private boolean sendLogin(String logOrReg, String username, String password) throws IOException {
		pw.println(logOrReg);
		pw.println(username);
		pw.println(password);
		return br.readLine().trim().equals("SUCCEEDED");

	}
	
    /*
     * 该线程负责读取服务端发送过来的消息
     * */
	class GetServerMessageHandler implements Runnable{
		public void run(){
			try {
				
				String message = null;
				while((message = br.readLine())!=null){
					
					showMessage.append(message);
					showMessage.append("\n");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 登录或注册
	 */
	private void loginOrRegister() {
//		mainFrame = new JFrame("Main Application");
//        mainFrame.setSize(400, 300);
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        mainFrame.setLayout(new FlowLayout());
//        mainFrame.setLocationRelativeTo(null);
//
//        // 添加登录或注册按钮
//        JButton loginOrRegisterButton = new JButton("Login or Register");
//        loginOrRegisterButton.addActionListener(e -> loginOrRegister());
//        mainFrame.add(loginOrRegisterButton);
//
//        mainFrame.setVisible(true);
		
		
		JDialog loginDialog = new JDialog(mainFrame, "Login", true);
	    loginDialog.setLayout(new GridLayout(3, 2, 5, 5));
	    loginDialog.setSize(300, 150);
	    loginDialog.setLocationRelativeTo(mainFrame);

	    loginDialog.add(new JLabel("    Username:"));
	    usernameField = new JTextField();
	    loginDialog.add(usernameField);
//	    loginDialog.add(new JLabel(""));

	    loginDialog.add(new JLabel("    Password:"));
	    passwordField = new JPasswordField();
	    loginDialog.add(passwordField);

//	    loginDialog.add(new JLabel(""));
	    registerButton = new JButton("Register");
	    registerButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            String username = usernameField.getText();
	            String password = new String(passwordField.getPassword());
//	            JOptionPane.showMessageDialog(loginDialog, "Username: " + username + "\nPassword: " + password);
	            loginDialog.dispose();
	            
	            logOrReg = "REGISTER";
	            nickName = username;
	            passWord = password;
	        }
	    });
	    loginDialog.add(registerButton);
	    
	    loginButton = new JButton("Login");
//	    loginDialog.add(new JLabel(""));
	    loginButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            String username = usernameField.getText();
	            String password = new String(passwordField.getPassword());
//	            JOptionPane.showMessageDialog(loginDialog, "Username: " + username + "\nPassword: " + password);
	            loginDialog.dispose();
	            
	            logOrReg = "LOGIN";
	            nickName = username;
	            passWord = password;
	        }
	    });
	    loginDialog.add(loginButton);

	    loginDialog.setVisible(true);
	}
	
	/*
	 * 窗口init
	 * 
	 * */
	private void initialize() {
	    
	    frame = new JFrame();
	    frame.setBounds(100,100,450,300);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setLayout(null);
	    showMessage = new JTextArea(10,50);
	    sp = new JScrollPane(showMessage);
	    sp.setBounds(25, 25, 400, 150);
	    showMessage.setBounds(25,25,400,140);
	    frame.getContentPane().add(sp);
	    inputMessage = new JTextArea();
	    inputMessage.setBounds(40,184, 231, 79);
	    frame.getContentPane().add(inputMessage);
	    btnNewButton = new JButton("send");
	    btnNewButton.addActionListener(new buttonAction());
	    btnNewButton.setBounds(282, 195, 102, 49);
	    frame.getContentPane().add(btnNewButton);
	    
        
    }
	
	/*
	 * 按钮单击
	 * 
	 * */
	private class buttonAction implements ActionListener{
	    
	    @Override
	    public void actionPerformed(ActionEvent e){
	        //TODO Auto-generated method stup
	        if (e.getSource() == btnNewButton) {
	            
	            String messageString = inputMessage.getText();
	            
	            //发送消息
	            
	            if(messageString.isEmpty()||messageString.trim().equals("")){
	                JOptionPane.showMessageDialog(frame, "Chat content cannot be empty.");
	            }else{
	                messageString.trim();
	                pw.println(messageString);
	                inputMessage.setText("");
	                
	            }
                
            }
	    }
	    
	}
	
	
	
}







