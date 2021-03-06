//JavaObjServer.java ObjectStream 기반 채팅 Server

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class JavaObjServer extends JFrame {

   /**
   * 
   */
   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   JTextArea textArea;
   private JTextField txtPortNumber;

   private ServerSocket socket; // 서버소켓
   private Socket client_socket; // accept() 에서 생성된 client 소켓
   private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
   private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

   /**
    * Launch the application.
    */
   public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
         public void run() {
            try {
               JavaObjServer frame = new JavaObjServer();
               frame.setVisible(true);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });
   }

   /**
    * Create the frame.
    */
   public JavaObjServer() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 338, 440);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(12, 10, 300, 298);
      contentPane.add(scrollPane);

      textArea = new JTextArea();
      textArea.setEditable(false);
      scrollPane.setViewportView(textArea);

      JLabel lblNewLabel = new JLabel("Port Number");
      lblNewLabel.setBounds(13, 318, 87, 26);
      
      contentPane.add(lblNewLabel);

      txtPortNumber = new JTextField();
      txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
      txtPortNumber.setText("30000");
      txtPortNumber.setBounds(112, 318, 199, 26);
      contentPane.add(txtPortNumber);
      txtPortNumber.setColumns(10);

      JButton btnServerStart = new JButton("Server Start");
      btnServerStart.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
            } catch (NumberFormatException | IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            AppendText("Chat Server Running..");
            btnServerStart.setText("Chat Server Running..");
            btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
            txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
            AcceptServer accept_server = new AcceptServer();
            accept_server.start();
         }
      });
      btnServerStart.setBounds(12, 356, 300, 35);
      contentPane.add(btnServerStart);
   }

   // 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
   class AcceptServer extends Thread {
      @SuppressWarnings("unchecked")
      public void run() {
         while (true) { // 사용자 접속을 계속해서 받기 위해 while문
            try {
               AppendText("Waiting new clients ...");
               client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
               AppendText("새로운 참가자 from " + client_socket);
               // User 당 하나씩 Thread 생성
               UserService new_user = new UserService(client_socket);
               UserVec.add(new_user); // 새로운 참가자 배열에 추가
               new_user.start(); // 만든 객체의 스레드 실행
               AppendText("현재 참가자 수 " + UserVec.size());
            } catch (IOException e) {
               AppendText("accept() error");
               // System.exit(0);
            }
         }
      }
   }

   public synchronized void AppendText(String str) {
      // textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
      textArea.append(str + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   public synchronized void AppendObject(ChatMsg msg) {
      // textArea.append("사용자로부터 들어온 object : " + str+"\n");
      textArea.append("code = " + msg.code + "\n");
      textArea.append("id = " + msg.UserName + "\n");
      textArea.append("data = " + msg.data + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   // User 당 생성되는 Thread
   // Read One 에서 대기 -> Write All
   class UserService extends Thread {
      private InputStream is;
      private OutputStream os;
      private DataInputStream dis;
      private DataOutputStream dos;

      private ObjectInputStream ois;
      private ObjectOutputStream oos;

      private Socket client_socket;
      private Vector user_vc;
      public String UserName = "";
      public String UserStatus;
      public String StartStatus = "N";
      public String UserRole = "U"; // 평범한 유저일경우 u 방장일경우 a
      public int ai = 0; // admin index 방장순서
      public int ai2 = 0; // 단어 인덱스
      public int flag = 0;
      public int score=0;

      public String[] words = { "종강", "달력", "마우스", "키보드", "거울", "가위" };
      public String[] words2= {"ㅈㄱ","ㄷㄹ","ㅁㅇㅅ","ㅋㅂㄷ","ㄱㅇ","ㄱㅇ"};
      public UserService(Socket client_socket) {
         // TODO Auto-generated constructor stub
         // 매개변수로 넘어온 자료 저장
         this.client_socket = client_socket;
         this.user_vc = UserVec;

         try {
            oos = new ObjectOutputStream(client_socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(client_socket.getInputStream());
         } catch (Exception e) {
            AppendText("userService error");
         }
      }

      public void Login() {
         AppendText("새로운 참가자 " + UserName + " 입장.");
         //WriteOne("Welcome to Java chat server\n");
         WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
         String msg = UserName + "님이 입장하였습니다.\n";
         WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
      }

      public void Logout() {
         String msg = UserName + "님이 퇴장 하였습니다.\n";
         UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
         WriteAll(msg); // 나를 제외한 다른 User들에게 전송
         this.client_socket = null;
         AppendText("사용자 " + "" + UserName + " 퇴장. 현재 참가자 수 " + UserVec.size());
      }

      // 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteAll(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteOne(str);
         }
      }

      // 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
      public void WriteAllObject(ChatMsg obj) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteChatMsg(obj);
         }
      }

      // 나빼고 보내기
      public void WriteOthersObject(ChatMsg obj) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.UserStatus == "O")
               user.WriteChatMsg(obj);
         }
      }

      // 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteOthers(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.UserStatus == "O")
               user.WriteOne(str);
         }
      }

      // Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
      public byte[] MakePacket(String msg) {
         byte[] packet = new byte[BUF_LEN];
         byte[] bb = null;
         int i;
         for (i = 0; i < BUF_LEN; i++)
            packet[i] = 0;
         try {
            bb = msg.getBytes("euc-kr");
         } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         for (i = 0; i < bb.length; i++)
            packet[i] = bb[i];
         return packet;
      }

      // UserService Thread가 담당하는 Client 에게 1:1 전송
      public synchronized void WriteOne(String msg) {
         ChatMsg obcm = new ChatMsg("", "300", msg);
         WriteChatMsg(obcm);
      }

      public synchronized void WritePrivate(String msg) {
         ChatMsg obcm = new ChatMsg("", "100", msg);
         WriteChatMsg(obcm);
      }

      //
      public synchronized void WriteChatMsg(ChatMsg obj) {
         try {
            oos.writeObject(obj.code);
            oos.writeObject(obj.UserName);
            oos.writeObject(obj.data);
            if (obj.code.equals("700")) {
               oos.writeObject(obj.imgbytes);
            }

         } catch (IOException e) {
            AppendText("oos.writeObject(ob) error");
            try {
               ois.close();
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout();

         }
      }

      public synchronized void WriteOneObject(Object ob) {
         try {
            oos.writeObject(ob);
         } catch (IOException e) {
            AppendText("oos.writeObject(ob) error");
            try {
               ois.close();
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout();
         }
      }

      //
      public ChatMsg ReadChatMsg() {
         Object obj = null;
         String msg = null;
         ChatMsg cm = new ChatMsg("", "", "");
         // Android와 호환성을 위해 각각의 Field를 따로따로 읽는다.
         try {
            obj = ois.readObject();
            cm.code = (String) obj;
            obj = ois.readObject();
            cm.UserName = (String) obj;
            obj = ois.readObject();
            cm.data = (String) obj;
            if (cm.code.equals("700")) {
               obj = ois.readObject();
               cm.imgbytes = (byte[]) obj;
               
            }
         } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            Logout();
            e.printStackTrace();
            return null;
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Logout();
            return null;
         }
         return cm;
      }

      public void run() {
         while (true) { // 사용자 접속을 계속해서 받기 위해 while문

            ChatMsg cm = null;
            String msg = null;
            if (client_socket == null)
               break;
            cm = ReadChatMsg();
            if (cm == null)
               break;
            if (cm.code.length() == 0)
               break;
            AppendObject(cm);

            if (cm.code.matches("100")) {
               UserName = cm.UserName;
               UserStatus = "O"; // Online 상태
               Login();
            } else if (cm.code.matches("200")) { //게임준비
               if (StartStatus == "R") {
                  StartStatus = "N";
                  String str = UserName + " 님이 준비해제 하셨습니다.\n";
                  WriteAll(str);

                  
               } else {
                  StartStatus = "R";
                  String str = UserName + " 님이 준비하셨습니다.\n";
                  WriteAll(str);
               }
            }else if (cm.code.matches("300")) {  //채팅메세지
               msg = String.format("%s %s", cm.UserName, cm.data);
               AppendText(msg); // server 화면에 출력
               String[] args = msg.split(" "); // 단어들을 분리한다.
               if (args.length == 1) { // Enter key 만 들어온 경우 Wakeup 처리만 한다.
                  UserStatus = "O";
               } else if (args[1].matches("/exit")) {
                  Logout();
                  break;
               } else if (args[1].matches("/list")) {
                  WriteOne("User list\n");
                  WriteOne("Name\tUStatus\tRStatus\tRole\n");
                  WriteOne("-----------------------------\n");
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     WriteOne(user.UserName + "\t\t" + user.UserStatus + "\t\t" + user.StartStatus + "\t\t"
                           + user.UserRole + "\n");
                  }
                  WriteOne("-----------------------------\n");
               } else if (args[1].matches("/sleep")) {
                  UserStatus = "S";
               } else if (args[1].matches("/wakeup")) {
                  UserStatus = "O";
               } else if (args[1].matches("/to")) { // 귓속말
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
                        String msg2 = "";
                        for (int j = 3; j < args.length; j++) {// 실제 message 부분
                           msg2 += args[j];
                           if (j < args.length - 1)
                              msg2 += " ";
                        }
                        // /to 빼고.. [귓속말] [user1] Hello user2..
                        user.WritePrivate(args[0] + " " + msg2 + "\n");
                        break;
                     }
                  }
               } else { // 일반 채팅 메시지
                  UserStatus = "O";

                  
                  WriteAllObject(cm);
                  System.out.println(words[ai2] + " " + cm.data);
                  if (cm.data.equals(words[ai2])) {
                     for (int i = 0; i < user_vc.size(); i++) {
                        UserService u = (UserService) user_vc.elementAt(i);
                        u.ai2++;
                        
                        if (u.ai2 == words.length)
                           u.ai2 = 0;
                     }
                     WriteAll("정답입니다.");
                     score+=10;
                     ChatMsg obcm2=new ChatMsg(UserName,"800", Integer.toString(score));
                     WriteAllObject(obcm2);
                     if(score>=20) {
                        ChatMsg obcm3=new ChatMsg(UserName, "900", UserName+"님이 우승하셨습니다.");
                        WriteAllObject(obcm3);
                     }
                     
                     PlayGame();
                  }

               }
            } else if (cm.code.matches("400")) { // logout message 처리
               Logout();
               break;
            } else if(cm.code.matches("500")) {  //힌트요청
               ChatMsg obcm=new ChatMsg(UserName, "500",words2[ai2]);
               WriteAllObject(obcm);
               
            }
            else if(cm.code.matches("600")) {  //포기
               ChatMsg obcm=new ChatMsg(UserName,"600"," 다음턴으로 넘어갑니다.\n");
               WriteAllObject(obcm);
               
               for (int i = 0; i < user_vc.size(); i++) {
                  UserService u = (UserService) user_vc.elementAt(i);
                  u.ai2++;
                  if (u.ai2 == words.length)
                     u.ai2 = 0;
               }
               if(cm.data.matches("TimeOver")) {
                  score-=20;
               }
               else {
                  score-=10;
               }
               
               ChatMsg obcm2=new ChatMsg(UserName,"800", Integer.toString(score)); //점수
               WriteAllObject(obcm2);
               
               PlayGame();
               
            }else if (cm.code.matches("700")) { // 사진처리
               WriteOthersObject(cm);
               
            }
            
            // 모두가 Ready상태일 경우 게임시작을 알리고 방장을 역할을 준다. 순서는 들어온 순서대로..
            if (CheckForReady().equals("true") && flag == 0) {
               for(int i=0;i<user_vc.size();i++) {
                  UserService u = (UserService) user_vc.elementAt(i);
                  ChatMsg obcm2=new ChatMsg(u.UserName,"800", Integer.toString(u.score));
                  WriteAllObject(obcm2);
                  u.flag=1;
               }
               
               WriteAll(UserName + "가 준비되었습니다. 게임을 시작합니다.\n");
               PlayGame();

            }
         } // while
      } // run

      private void PlayGame() {
         
         UserService user2 = (UserService) user_vc.elementAt(ai);
         user2.UserRole = "A";
         ChatMsg obcm = new ChatMsg("", "220", user2.UserName + "님의 차례입니다.");
         System.out.println(user2.UserName);
         WriteAllObject(obcm);
         ChatMsg obcm2 = new ChatMsg("", "250", words[ai2]);
         user2.WriteChatMsg(obcm2);

         
         for (int i = 0; i < user_vc.size(); i++) {
            UserService u = (UserService) user_vc.elementAt(i);
            u.ai++;
            
            if (u.ai == user_vc.size())
               u.ai = 0;
            System.out.println(u.UserName + " ai " + u.ai);
         }

      }

      public String CheckForReady() {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.StartStatus == "R")
               if (i == user_vc.size() - 1) {

                  return "true";
               }
         }
         return "false";

      }
   }

}