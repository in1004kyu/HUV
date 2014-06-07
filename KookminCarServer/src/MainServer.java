import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class MainServer extends JFrame {
	
		private ArrayList<MultiServerThread> list;
		private Socket socket;
		private JTextArea ta;
		private JTextField tf;

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
			add(tf, BorderLayout.SOUTH);
			setSize(300, 300);
			setVisible(true);
			
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
					mst = new MultiServerThread();
					
					/* 클라이 언트 관리를 위해 전체 리스트에 차곡 차곡 쌓아 줍니다. */
					list.add(mst);
					
					/* 쓰래드 시작! */
					mst.start();
					
				} //End Of while
			} catch (IOException e) {
				e.printStackTrace();
			} //End Of catch
			
		}// End Of init
		

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
			private ObjectInputStream ois;
			private ObjectOutputStream oos;
			private InetAddress ClientUrl;
		    BufferedWriter out;
		    String s = "출력 파일에 저장될 이런 저런 문자열입니다.";
		    Date date;
		    Date logLineDate;
		    boolean init = false;
		    /* 여러 로그의 싱크를 맞추기 위해서 
		     * [0] = time, [1] = voice, [2] = heart rate, [3] = kinect
		     * */
		    long prevTime = 0;
		    long currentTime = 0;
			private String[] msg_arr = new String[]{"0", "0", "0", "0"};
			String fileName;
			@Override
			public void run() {
				
				/* 깃발 값*/
				boolean isStop = false;
				
				
				try {
					/* 클라이언트 입력에 관한 Stream */
					ois = new ObjectInputStream(socket.getInputStream());
					
					/* 클라이언트 출력에 관한 Stream */
					oos = new ObjectOutputStream(socket.getOutputStream());
					
					/* 채팅으로 어떤 문자가 왔는지 담는 변수 */
					String message = null;
					
					ClientUrl = socket.getInetAddress();
					ta.append(ClientUrl.toString()
							+ " Iy P 주소의 클라이언트에서 접속하였습니다.\r\n");					
					while (!isStop) {
						
						/* 클라이언트가 입력을 할때까지 기다립니다. */
						message = (String) ois.readObject();
						
						/* 여기 넘어왔다면 클라이언트가 어떤 문자를 보냈고 */
						
						/* 홍길동#방가방가 <-- 요런 식이기 때문에 방가 방가랑 홍길동 이랑 분리 해줍니다.*/
						ta.append(socket.getInetAddress() +"에서 받은 메시지: "+message +"\r\n");
						String[] str = message.split("#");
						
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
							      out.close();
								// 로그기록 끝
							}
							else if(str[1].equals("start")){
								// 로그기록시작
								System.out.println("start");
								String DateFormat ="#"+date.getYear()+"#"+ date.getDay() +"#"+date.getHours()+"#"+date.getMinutes()+"#"+date.getSeconds();
								File desti = new File("C:\\Embedded");
								fileName = ClientUrl.getHostAddress() +DateFormat+".txt";
								ta.append("생성한 파일 이름:" +fileName+ "\r\n");
								  //해당 디렉토리의 존재여부를 확인
//									if(!desti.exists()){
//								  //없다면 생성
//									desti.mkdirs(); 
//									}
								out = new BufferedWriter(new FileWriter(fileName));
							}
							else if(str[1].equals("stop"))
							{
								isStop = true;
							}
						} else { // msg#단어
							/* 종료가 아니라면 해당하는 메세지를 리스트에 저장된 모든 
							 * 클라이언트 들에게 전달해 줍니다. */
							//broadCasting(message);// 모든 사용자에게 채팅 내용 전달
							
							out = new BufferedWriter(new FileWriter(fileName, true));
							
							logLineDate = new Date();
							currentTime = logLineDate.getTime();							
							if(str[0].equals("msg_voice")){
								msg_arr[1] = str[1];
							} else if(str[0].equals("msg_heart")){
								msg_arr[2] = str[1];
							} else if(str[0].equals("msg_kinect")){
								msg_arr[3] = str[1];
							}							
							else
							{
								ta.append("클라이언으로부터 잘못된 데이터 입력" +  "\r\n");
							}
							// Log per every 0.5 seconds.
							// 처음 실행 시 일단 기록한다. 이후 부턴 0.5 초 단위로 기록
							if(currentTime - prevTime > 500 || prevTime == 0){
								if(prevTime == 0){
									// 처음 실행시 prevTime을 현재 시간으로
									System.out.println("초기화");
									prevTime = logLineDate.getTime();
								}
								System.out.println("msg " + currentTime + " " + prevTime);
								out.write(currentTime +" " + msg_arr[1] + " " + msg_arr[2] + " " + msg_arr[3]); 
								out.newLine();
								prevTime = currentTime;
								// 로깅 이후엔 전부 초기화
								for(int i = 1; i < 4; i++){
									msg_arr[i] = "0";
								}
							}
							System.out.println("msg-not-logging");
							
							out.close();
						}
					} //End Of while
					
					/* 여기 왔다는 증거는 이 클라이 언트가 종료 한다는 것이기 때문에
					 * 리스트에서 빼 줍니다. */
					list.remove(this);
					
					/*그리고 서버 화면에 누가 종료 되었는지 알려주는 것이죠*/
					ta.append(socket.getInetAddress() + " IP 주소의 사용자께서 종료하셨습니다.\r\n");
					tf.setText("연결 클라이언트 수 : " + list.size());
					
				} catch (Exception e) {
					list.remove(this);// 장길산을 뺀다.
					ta.append(socket.getInetAddress() +e.getMessage()
							+ " IP 주소의 클라이언트에서 비정상 종료하셨습니다.\r\n");
					tf.setText("남은 클라이언트 수 : " + list.size());
				} //End Of catch
			} //End Of run

			/**
			 * 모두에게 전송 합니다.
			 * @param message
			 */
//			public void broadCasting(String message) {
//				for (MultiServerThread ct : list) {
//					ct.send(message);
//				} //End Of for
//			} //End Of Method broadCasting

			/**
			 * 한 사용자에게 전송합니다.
			 * @param message
			 */
//			public void send(String message) { // 한 사용자에게 전송
//				try {
//					oos.writeObject(message);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} //End Of catch
//			} //End Of Method send
		}// 내부 클래스
		
		/**
		 * 시작 메인입니다.
		 * @param args
		 */
		public static void main(String[] args) {
			new MainServer();
		} //End Of Main
		
	} //End Of Class
