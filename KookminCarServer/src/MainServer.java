import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/* github test */
public class MainServer extends JFrame{
	
		static ArrayList<MultiServerThread> list;
		private Socket socket;
		private JTextArea ta;
		private JTextField tf;
		JButton btKinect ;
		JButton btCluster ;
		JButton btSteer ;
		private String[] msg_arr = new String[]{"0", "0", "0", "0"};
		BufferedWriter out = null;
		boolean islogging = false;
		Process ProcessKinect = null;
		Process ProcessCluster = null;
		Process ProcessSteer = null;
		String kinectFacePath = "kinect\\SingleFace.exe";
		
		final int ID_CLUSTER = 10;
		final int ID_KINECT = 11;
		final int ID_PHONE = 12;
		final int ID_MATLAB = 13;
		int s = 0;
		/**
		 * 생성자
		 */
		public MainServer() {
			/* 화면 관련 설정을 합니다.*/
			setTitle("임베디드 서버 ver 1.0");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			ta = new JTextArea();
			add(new JScrollPane(ta));
			tf = new JTextField();
			tf.setEditable(false);
			
			btKinect = new JButton("키넥트실행");
			btCluster = new JButton("클러스터 실행");
			btSteer = new JButton("Steer실행");
			
			btKinect.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					File dir1 = new File(".");
					
					
					try {
						System.out.println( dir1.getCanonicalPath() );
						if(btKinect.getText().equalsIgnoreCase("키넥트실행")) {
							ProcessKinect = new ProcessBuilder("kinect\\SingleFace.exe").start();
						    btKinect.setText("키넥트중지");	
						} else {
							btKinect.setText("키넥트실행");
							ProcessKinect.destroy();
						}
						
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
	
			});
			btCluster.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			btSteer.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
				File dir1 = new File(".");
					
					
					try {
						System.out.println( dir1.getCanonicalPath() );
						if(btSteer.getText().equalsIgnoreCase("Steer실행")) {
							ProcessSteer = new ProcessBuilder("steer\\SteeringWheelSDKDemo.exe").start();
							
							btSteer.setText("Steer중지");	
							
							
						} else {
							btSteer.setText("Steer실행");
							ProcessSteer.destroy();
						}
						
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			JPanel pane = new JPanel();
			pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			pane.setLayout(new GridLayout(0,2));
			pane.add(btKinect);
			pane.add(btCluster);
			pane.add(btSteer);
			add(pane, BorderLayout.NORTH);
			add(tf, BorderLayout.SOUTH);
			setSize(500, 500);
			setVisible(true);
			
			
			
			
			islogging = true;
			
			LoggingThread threadLogging = new LoggingThread();
			threadLogging.start();
			
			/* 채팅에 필요한 설정을 합니다. */
			list = new ArrayList<MultiServerThread>();
			try {
				
				/* 포트 설정으로 서버 소캣을 만듭니다. */
				ServerSocket serverSocket = new ServerSocket(5000);
				
				/* 사용자 한명당 처리 하기 위한 스래드 객체입니다. */
				MultiServerThread mst = null;
				
				/* 깃발 값 (확장의 목적으로 사용)*/
				boolean isStop = false;
				
				/* 텍스트 창에 찍고 */
				tf.setText("서버 정상 실행중입니다.^^\n");
				
				while (!isStop) {
					
					/* 클라이언트가 접속 하기를 기다립니다. 그리고 기다렸다가 소캣을 생성! */
					socket = serverSocket.accept();
					
					/* 여기까지 왔다면 클라이언트가 접속했다는 의미죠
					 * 그러면 한 클라이언트당 처리를 담당할 스래드를 실행 합니다. */
					mst = new MultiServerThread(socket);
					
					/* 클라이 언트 관리를 위해 전체 리스트에 차곡 차곡 쌓아 줍니다. */
					list.add(mst);
					
					/* 쓰래드 시작! */
				//	mst.start();
					
				} //End Of while
			} catch (IOException e) {
				e.printStackTrace();
			} //End Of catch
			
		}// End Of init
		
		class LoggingThread extends Thread {
		   
			Date date;
		    Date logLineDate;
		    long currentTime = 0;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isStop = true;
				while(isStop) {
					
					try {
						Thread.sleep(500);
						
						if(islogging == true) {
							logLineDate = new Date();
							currentTime = logLineDate.getTime();		
							
							
//							out.write(currentTime +" " + msg_arr[1] + " " + msg_arr[2] + " " + msg_arr[3]); 
//							out.newLine();
//							out.flush();								// 로깅 이후엔 전부 초기화
//							
//							if(true)
//							{
//								for(int i=0; i< list.size(); i++)
//									list.get(i).sendMessage(currentTime +" " + msg_arr[1] + " " + msg_arr[2] + " " + msg_arr[3]);
//							}
							
//								for(int i = 1; i < 4; i++){
//									msg_arr[i] = "0";
//								}	
								

						}
						
//						for(int i=0; i< list.size(); i++) {
//							if(list.get(i).getID()== ID_CLUSTER)
//								list.get(i).sendMessage("" + s*2);
//							s++;
//						}
						
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
				}
				super.run();
			}

		}

		
		class ClusterThread extends Thread {
			   
			Date date;
		    Date logLineDate;
		    long currentTime = 0;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isStop = true;
				System.out.println("ClusterThread Start");
				while(isStop) {
					
					try {
						Thread.sleep(500);
							
							if(true)
							{
								for(int i=0; i< list.size(); i++)
									if(list.get(i).getID()== ID_CLUSTER)
									list.get(i).sendMessage("DATA#50#50#1#4#5");
							}
		
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
				}
				super.run();
			}

		}
		
		/**
		 * <p>Title: MultiServer Thread</p>
		 *
		 * <p>Description: (내부 클래스) 한 클라이언트당 처리 하기 위한 스래드 클래스입니다.</p> 					
		 *
		 * <p>Copyright: Copyright (c) 2009</p>
		 *
		 * @author 뚱곰이
		 * @since 2009. 12. 7.
		 */
		class MultiServerThread extends Thread {
			
			private Socket socket;
			private InputStream fromClient;
			private OutputStream toClient;
			private int id;
		    Date date;
		    Date logLineDate;
		    boolean init = false;
		    /* 여러 로그의 싱크를 맞추기 위해서 
		     * [0] = time, [1] = voice, [2] = heart rate, [3] = kinect
		     * */
//		    long prevTime = 0;
		    long currentTime = 0;

			String fileName;
			
			
			public MultiServerThread(Socket socket) throws IOException
			{
				
				System.out.println("connecting  " + socket);
				this.socket = socket;
				fromClient = socket.getInputStream();
				toClient   = socket.getOutputStream();
				id = -1;
				start();
				
			}
			public void sendMessage(String str)
			{
				
				try {
					toClient.write(str.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public int getID(){
				return id;
			}
		    
		    String s = "출력 파일에 저장될 이런 저런 문자열입니다.";

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				boolean isStop = false;

				try{
//					InputStream fromClient = socket.getInputStream();
//					OutputStream toClient = socket.getOutputStream();
					/* 채팅으로 어떤 문자가 왔는지 담는 변수 */
					String outdata = null;
										InetAddress ClientUrl = socket.getInetAddress();
					byte[] buf = new byte[1024];
					int count;
					
					ta.append(ClientUrl.toString()+ " IP 주소의 클라이언트에서 접속 하였습니다.\r\n");					

					while ((count = fromClient.read(buf)) != -1)
					{
						
						//toClient.write(buf, 0, count);
						
						outdata = new String(buf, 0 , count, "utf-8");
						outdata = outdata.trim();
						System.out.println(outdata);
					//	ta.append("RECV: " + outdata + " \r\n");  //kin#23.25 

						
						//ta.append(socket.getInetAddress() +"에서 받은 메시지: "+outdata +"\r\n");
						
						String[] str = outdata.split("#");
						
						/* 홍길동#exit, 이렇게 온다면 종료하겠다는 뜻 */
						if (str[0].equals("com")) {  // com#start
							date= new Date();
							/* 종료가 맞다면 다른 사용자게에 알리고 */
							//broadCasting(message);
							
							/*그리고 쑝~ 나가줍니다.*/
							if(str[1].equals("end")){
								//isStop = true;
							    //  out.write(s); out.newLine();
							      System.out.println("end");
							      islogging = false;
							      if(out != null)
							    	  out.close();
								// 로그기록 끝
							}
							else if(str[1].equals("start")){
								// 로그기록시작
								System.out.println("start");
								String DateFormat ="#"+date.getYear()+"#"+ date.getDay() +"#"+date.getHours()+"#"+date.getMinutes()+"#"+date.getSeconds();
//								File desti = new File("C:\\Embedded");
								fileName = ClientUrl.getHostAddress() +DateFormat+".txt";
								ta.append("생성한 파일 이름:" +fileName+ "\r\n");
								  //해당 디렉토리의 존재여부를 확인
//									if(!desti.exists()){
//								  //없다면 생성
//									desti.mkdirs(); 
//									}
								out = new BufferedWriter(new FileWriter(fileName));
								islogging = true;
							}
							else if(str[1].equals("stop"))
							{
								islogging = false;
								if(out != null)
									out.close();
							}
							else if(str[1].equals("kinecton")) {
//								   oProcess = new ProcessBuilder(kinectFacePath).start();
								ProcessKinect = new ProcessBuilder("kinect\\SingleFace.exe").start();
							}
							else if(str[1].equals("kinectoff")){
//								if(oProcess != null)
//									oProcess.destroy();
								ProcessKinect.destroy();
							}else if(str[1].equals("id")){  // 아이디 입력
								
								if(str[2].equals("cluster")){
									
									id = ID_CLUSTER;
									ClusterThread ct = new ClusterThread();  // 데이터 전송 시작
									ct.start();
									ta.append("클러스터 프로그램 로그인 성공!" +    "\r\n");	
								}
								else if(str[2].equals("kinect")){
									id = ID_KINECT;
								} 
								else if(str[2].equals("phone")){
									id = ID_PHONE;
								} 
								else if(str[2].equals("matlab")){
									id = ID_MATLAB;
									ta.append("Matlab 프로그램 로그인 성공!" +    "\r\n");	
								} 
							}
							
							
						} else if (str[0].equals("steer")) {
						
							date= new Date();


						}
						else { // msg#단어
							/* 종료가 아니라면 해당하는 메세지를 리스트에 저장된 모든 
							 * 클라이언트 들에게 전달해 줍니다. */
							//broadCasting(message);// 모든 사용자에게 채팅 내용 전달
							//
							if(islogging == true)
							{
								
//								out = new BufferedWriter(new FileWriter(fileName, true));
					
								if(str[0].equals("msg_voice")){
									msg_arr[1] = str[1];
								} else if(str[0].equals("msg_heart")){
									msg_arr[2] = str[1];
								} else if(str[0].equals("msg_kinect")){
									msg_arr[3] = str[1];
								} else if(str[0].equals("msg_matlab_call")){
									
										sendMessage(currentTime +" " + msg_arr[1] + " " + msg_arr[2] + " " + msg_arr[3]);
									
								}
								else
								{
									ta.append("클라이언으로부터 잘못된 데이터 입력" +  "\r\n");
								}
								// Log per every 0.5 seconds.
								// 처음 실행 시 일단 기록한다. 이후 부턴 0.5 초 단위로 기록

						//		System.out.println("msg-logging");
								
//								out.close();
							} //if(islogging == true)
						}
				
					}
					
					toClient.close();
					ta.append("RECV: " + "연결 종료" + " \r\n");
				}
				catch(IOException e) {
					list.remove(this);// 장길산을 뺀다.
					ta.append(socket.getInetAddress() +e.getMessage()
							+ " IP 주소의 클라이언트에서 비정상 종료하셨습니다.\r\n");
					tf.setText("남은 클라이언트 수 : " + list.size());
				}
				super.run();
			}
			

		}// 내부 클래스
		
		/**
		 * 시작 메인입니다.
		 * @param args
		 */
		public static void main(String[] args) {
			new MainServer();
		} //End Of Main
		
	} //End Of Class
