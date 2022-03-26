//JavaObjServer.java ObjectStream ��� ä�� Server

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

   private ServerSocket socket; // ��������
   private Socket client_socket; // accept() ���� ������ client ����
   private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
   private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

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
            btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
            txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
            AcceptServer accept_server = new AcceptServer();
            accept_server.start();
         }
      });
      btnServerStart.setBounds(12, 356, 300, 35);
      contentPane.add(btnServerStart);
   }

   // ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
   class AcceptServer extends Thread {
      @SuppressWarnings("unchecked")
      public void run() {
         while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
            try {
               AppendText("Waiting new clients ...");
               client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
               AppendText("���ο� ������ from " + client_socket);
               // User �� �ϳ��� Thread ����
               UserService new_user = new UserService(client_socket);
               UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
               new_user.start(); // ���� ��ü�� ������ ����
               AppendText("���� ������ �� " + UserVec.size());
            } catch (IOException e) {
               AppendText("accept() error");
               // System.exit(0);
            }
         }
      }
   }

   public synchronized void AppendText(String str) {
      // textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
      textArea.append(str + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   public synchronized void AppendObject(ChatMsg msg) {
      // textArea.append("����ڷκ��� ���� object : " + str+"\n");
      textArea.append("code = " + msg.code + "\n");
      textArea.append("id = " + msg.UserName + "\n");
      textArea.append("data = " + msg.data + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   // User �� �����Ǵ� Thread
   // Read One ���� ��� -> Write All
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
      public String UserRole = "U"; // ����� �����ϰ�� u �����ϰ�� a
      public int ai = 0; // admin index �������
      public int ai2 = 0; // �ܾ� �ε���
      public int flag = 0;
      public int score=0;

      public String[] words = { "����", "�޷�", "���콺", "Ű����", "�ſ�", "����" };
      public String[] words2= {"����","����","������","������","����","����"};
      public UserService(Socket client_socket) {
         // TODO Auto-generated constructor stub
         // �Ű������� �Ѿ�� �ڷ� ����
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
         AppendText("���ο� ������ " + UserName + " ����.");
         //WriteOne("Welcome to Java chat server\n");
         WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
         String msg = UserName + "���� �����Ͽ����ϴ�.\n";
         WriteOthers(msg); // ���� user_vc�� ���� ������ user�� ���Ե��� �ʾҴ�.
      }

      public void Logout() {
         String msg = UserName + "���� ���� �Ͽ����ϴ�.\n";
         UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
         WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
         this.client_socket = null;
         AppendText("����� " + "" + UserName + " ����. ���� ������ �� " + UserVec.size());
      }

      // ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
      public void WriteAll(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteOne(str);
         }
      }

      // ��� User�鿡�� Object�� ���. ä�� message�� image object�� ���� �� �ִ�
      public void WriteAllObject(ChatMsg obj) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteChatMsg(obj);
         }
      }

      // ������ ������
      public void WriteOthersObject(ChatMsg obj) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.UserStatus == "O")
               user.WriteChatMsg(obj);
         }
      }

      // ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
      public void WriteOthers(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.UserStatus == "O")
               user.WriteOne(str);
         }
      }

      // Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
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

      // UserService Thread�� ����ϴ� Client ���� 1:1 ����
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
         // Android�� ȣȯ���� ���� ������ Field�� ���ε��� �д´�.
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
         while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��

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
               UserStatus = "O"; // Online ����
               Login();
            } else if (cm.code.matches("200")) { //�����غ�
               if (StartStatus == "R") {
                  StartStatus = "N";
                  String str = UserName + " ���� �غ����� �ϼ̽��ϴ�.\n";
                  WriteAll(str);

                  
               } else {
                  StartStatus = "R";
                  String str = UserName + " ���� �غ��ϼ̽��ϴ�.\n";
                  WriteAll(str);
               }
            }else if (cm.code.matches("300")) {  //ä�ø޼���
               msg = String.format("%s %s", cm.UserName, cm.data);
               AppendText(msg); // server ȭ�鿡 ���
               String[] args = msg.split(" "); // �ܾ���� �и��Ѵ�.
               if (args.length == 1) { // Enter key �� ���� ��� Wakeup ó���� �Ѵ�.
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
               } else if (args[1].matches("/to")) { // �ӼӸ�
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
                        String msg2 = "";
                        for (int j = 3; j < args.length; j++) {// ���� message �κ�
                           msg2 += args[j];
                           if (j < args.length - 1)
                              msg2 += " ";
                        }
                        // /to ����.. [�ӼӸ�] [user1] Hello user2..
                        user.WritePrivate(args[0] + " " + msg2 + "\n");
                        break;
                     }
                  }
               } else { // �Ϲ� ä�� �޽���
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
                     WriteAll("�����Դϴ�.");
                     score+=10;
                     ChatMsg obcm2=new ChatMsg(UserName,"800", Integer.toString(score));
                     WriteAllObject(obcm2);
                     if(score>=20) {
                        ChatMsg obcm3=new ChatMsg(UserName, "900", UserName+"���� ����ϼ̽��ϴ�.");
                        WriteAllObject(obcm3);
                     }
                     
                     PlayGame();
                  }

               }
            } else if (cm.code.matches("400")) { // logout message ó��
               Logout();
               break;
            } else if(cm.code.matches("500")) {  //��Ʈ��û
               ChatMsg obcm=new ChatMsg(UserName, "500",words2[ai2]);
               WriteAllObject(obcm);
               
            }
            else if(cm.code.matches("600")) {  //����
               ChatMsg obcm=new ChatMsg(UserName,"600"," ���������� �Ѿ�ϴ�.\n");
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
               
               ChatMsg obcm2=new ChatMsg(UserName,"800", Integer.toString(score)); //����
               WriteAllObject(obcm2);
               
               PlayGame();
               
            }else if (cm.code.matches("700")) { // ����ó��
               WriteOthersObject(cm);
               
            }
            
            // ��ΰ� Ready������ ��� ���ӽ����� �˸��� ������ ������ �ش�. ������ ���� �������..
            if (CheckForReady().equals("true") && flag == 0) {
               for(int i=0;i<user_vc.size();i++) {
                  UserService u = (UserService) user_vc.elementAt(i);
                  ChatMsg obcm2=new ChatMsg(u.UserName,"800", Integer.toString(u.score));
                  WriteAllObject(obcm2);
                  u.flag=1;
               }
               
               WriteAll(UserName + "�� �غ�Ǿ����ϴ�. ������ �����մϴ�.\n");
               PlayGame();

            }
         } // while
      } // run

      private void PlayGame() {
         
         UserService user2 = (UserService) user_vc.elementAt(ai);
         user2.UserRole = "A";
         ChatMsg obcm = new ChatMsg("", "220", user2.UserName + "���� �����Դϴ�.");
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