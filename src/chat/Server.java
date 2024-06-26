package chat;

import db.DBOperator;
import db.History;
import db.User;
//import org.jcp.xml.dsig.internal.dom.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mysql.*;


/**
 * 聊天室服务端
 * @author Administrator
 *
 */
public class Server {
	/*
	 *  运行在服务端的Socket
	 *  用来接收客户端的连接。 
	 */
	private ServerSocket server;
	
	/*
	 * 线程池
	 */
	private ExecutorService threadPool;
	
	/*
	 * 存放所有客户端输出流的共享集合
	 */
	private List<PrintWriter> allOut;

	private DBOperator db;
	
	/**
	 * 构造方法，用来初始化服务端
	 */
	public Server(){
		try {
			/*
			 * 初始化共享集合
			 */
			allOut = new ArrayList<PrintWriter>();
			
			/*
			 * 读取配置文件
			 * java.util.Properties
			 * 
			 */
			Properties properties = new Properties();
			/*
			 * void load(InputStream in)
			 * 该方法用于读取给定的流中的数据，然后进行解析
			 * 
			 * 我们可以使用FileInputStream这个流来读取我们
			 * 定义的配置文件config.properties,所以我们可以
			 * 创建这个流，然后将该流作为参数传给load方法。
			 * 这样Properties就可以通过FileInputStream读取
			 * 我们的配置文件了。
			 */
			FileInputStream fis = new FileInputStream("config.properties");
			properties.load(fis);
			
			//获取服务端端口号
			String port = properties.getProperty("serverport");
			System.out.println("Server port:"+port);
			/*
			 * 初始化ServerSocket时要传入一个参数
			 * 该参数就是服务端对外开启的服务端口
			 * 客户端就是通过该端口与服务端进行连接的
			 */
			server = new ServerSocket(Integer.parseInt(port));
			
			/*
			 *  获取线程的数量
			 */
			String threadCount = properties.getProperty("threadcount");
			System.out.println("Number of thread pool:"+threadCount);
			
			/*
			 * 初始化线程池
			 */
			threadPool = Executors.newFixedThreadPool(Integer.parseInt(threadCount));

			/*
			 * 连接数据库
			 */
			db = new DBOperator();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 向共享集合中添加一个输出流
	 * @param out
	 */
	public synchronized void addOut(PrintWriter out){
		allOut.add(out);
	}
	/**
	 * 从共享集合中删除给定的输出流
	 * @param out
	 */
	public synchronized void removeOut(PrintWriter out){
		allOut.remove(out);
	}
	/**
	 * 遍历共享集合中的所有输出流,将给定的消息发送给所有
	 * 的客户端
	 * @param message
	 */
	public synchronized void sendMessageToAllClient(String message){
		for(PrintWriter out : allOut){
			out.println(message);
		}
	}
	
	
	
	/**
	 * 服务端开始工作的方法
	 */
	public void start(){
		try {
			/*
			 * Socket accept()
			 * 该方法是ServerSocket开始监听8088端口，这个方法
			 * 是一个阻塞方法，直到一个客户端连接为止，若客户端
			 * 连接了，会返回一个Socket,这个Socket就是用来与
			 * 该客户端进行通讯的。
			 */
			while(true){
				System.out.println("Waiting for a client...");
				Socket socket = server.accept();
				System.out.println("A client has connected!");
				
				/*
				 * 启动一个线程，并将刚刚连接的客户端的Socket
				 * 传给它，让它去处理与这个客户端的交互。
				 */
				ClientHandler clientHandler = new ClientHandler(socket);
				threadPool.execute(clientHandler);

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
	
	/**
	 * 该内部类是为服务端服务的。用来与一个客户端进行
	 * 交互的。
	 * @author Administrator
	 *
	 */
	class ClientHandler implements Runnable{
		/*
		 * 该线程用来交互的客户端的Socket
		 */
		private Socket socket;
		/*
		 * 该客户端的昵称
		 */
		private String nickName;
		private BufferedReader br;
		private PrintWriter pw;
		public boolean authorized = false;

		public ClientHandler(Socket socket){
			this.socket = socket;
			/*
			 * 获取远程计算机的地址信息
			 */
			InetAddress address = socket.getInetAddress();
			//获取远程计算机的地址
			String add = address.getHostAddress();
			System.out.println(add+" connect.");
			
		}

		// 验证登录
		private boolean verification(){
			try {
				String name = br.readLine().trim();
				String pass = br.readLine().trim();
				nickName = name;
				User user = db.getUserByName(name);
				// 未搜索到用户
				if (user.equals(null)) {
					return false;
				}
				// 对比加密密码
				if (MyUtils.encrypt(pass).equals(user.getPassword())){
					return true;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return false;
		}
		
		public void run() {
			try {
				/*
				 * 通过Socket获取输出流，用于将消息发送给
				 * 客户端
				 */
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
				pw = new PrintWriter(osw,true);

				/*
				 * InputStream getInputStream()
				 * Socket的该方法用来获取远程计算机发送过来的数据
				 */
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in,"UTF-8");
				br = new BufferedReader(isr);
				/*
				 * 首先读取一行字符串，因为客户端发送过来的第一
				 * 行字符串是该客户端的昵称，读取到后将其设置到
				 * 属性nickName上
				 */

				/*
				 * 登录验证
				 */
				while (!verification()){
					pw.println("FAILED");
				}
				pw.println("SUCCEEDED");


				//将该客户端的输出流放入共享集合中
				addOut(pw);


				//获取历史消息记录
				List<History> histories = db.getRecentHistory();
				//全部编码为一条base64
				StringBuilder stringBuilder = new StringBuilder();
				for (History history : histories) {
					stringBuilder.append(history).append("\n");
				}
				String s = stringBuilder.toString();
				String encoded = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
				//发送给客户端
				pw.println(encoded);


				//广播，该用户上线了
				sendMessageToAllClient("["+nickName+"] is online.");
				System.out.println("["+nickName+"] is online.");
				//读取客户端发送过来的一行字符串
				/*
				 * 使用BufferedReader的readLine方法读取客户端发送
				 * 过来的一行字符串时，由于客户端所使用的操作系统
				 * 不同，这里在客户端与服务端断开连接后，该方法的
				 * 反应是不同的。
				 * 
				 * 若是windows系统的客户端与我们断开连接，那么
				 * readLine方法会抛出异常。
				 * 
				 * 若是linux系统的客户端与我们断开连接，那么
				 * readLine方法会返回null。
				 */
				String message = null;
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
				while((message = br.readLine())!=null){
					// 记录消息到数据库
					db.insertHistory(nickName, message);
					//将读取到的内容转发给所有客户端
					sendMessageToAllClient("[" + formatter.format(new Timestamp(new Date().getTime())) + "] " + nickName + ": " + message);
				}				
			
			} catch (Exception e) {
				
			} finally{
				/*
				 * 无论是linux还是windows的客户端，断开连接
				 * 后都要做的事情，就放到finally中来解决
				 */
				//将该客户端的输出流从共享集合中删除
				removeOut(pw);
				
				//广播，通知所有客户端该用户下线了
				sendMessageToAllClient("["+nickName+"] is offline.");
				System.out.println("["+nickName+"] is offline.");
				/*
				 * 将该客户端的socket关闭。
				 * 关闭socket同时也就将使用它获取的输入流与
				 * 输出流关掉了。
				 */
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
			
		}
		
	}
	
}



