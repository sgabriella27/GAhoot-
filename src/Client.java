import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

public class Client {

	Scanner scan = new Scanner(System.in);
	boolean cek = true;
	int sendPort = 8088;			
	int listenPort = 8888;
	MulticastSocket listenSocket;
	DatagramSocket socket;
	InetAddress inetAddr;
	String username, usernameSend;
	static Vector<CreateQuiz> createQuiz = new Vector<>();

	public void clear() {
		for (int i = 0; i <= 30; i++) {
			System.out.println();
		}
	}

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}
	
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
	
	private void sendPacket(byte[] temp) throws Exception {
		DatagramPacket packet = new DatagramPacket(temp, temp.length, inetAddr, sendPort);
		socket.send(packet);
	}
	
	private void sendPacket(String str) throws Exception {
		sendPacket(str.getBytes());
	}
	
	private Object receivePacketObject() throws Exception {
		byte[] temp = new byte[64000];
		DatagramPacket packet = new DatagramPacket(temp, temp.length);
		listenSocket.receive(packet);
		return deserialize(temp);
	}
	
	private String receivePacketString() throws Exception {
		byte[] temp = new byte[1024];
		DatagramPacket packet = new DatagramPacket(temp, temp.length);
		socket.receive(packet);
		return new String(temp).trim();
	}
	
	private Vector<CreateQuiz> fetchQuizes() throws Exception {
		sendPacket("getQuestion");
		return (Vector<CreateQuiz>) receivePacketObject();
	}

	public void createQuiz() {
		String theme = "", question = "", answer = "";
		byte[] temp = new byte[1024];
		
		do {
			System.out.print("Input Question Theme [3 - 15 Characters] : ");
			theme = scan.nextLine();
			if (theme.length() < 3 || theme.length() > 15) {
				System.out.println("Theme length must between 3 - 15 characters");
				cek = false;
			} else {
				cek = true;
			}
		} while (cek == false);

		do {
			System.out.print("Input Question [must ends with '?'] : ");
			question = scan.nextLine();
			if (!question.endsWith("?")) {
				System.out.println("Question must ends with '?'");
				cek = false;
			} else {
				cek = true;
			}
		} while (cek == false);

		do {
			System.out.print("Input Question's Answer : ");
			answer = scan.nextLine();
			if(answer.equals("")) {
				System.out.println("Answer can not be empty");
				cek = false;
			}
			else {
				cek = true;
			}
		}
		while(cek == false);

		CreateQuiz createQuiz = new CreateQuiz(theme, question, answer, username);
		
		try {
			sendPacket("!menu1");
			sendPacket(serialize(createQuiz));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void answerQuiz(Vector<CreateQuiz> question) {
		int qIdx = 0;
		String answer = "", again = "";
		do {
			System.out.print("Input Question's Index [1 - " + question.size() + "] : ");
			qIdx = scan.nextInt(); scan.nextLine();
			if(qIdx < 1 || qIdx > question.size()) {
				System.out.println("Question index must between 1 - " + question.size());
				cek = false;
			}
			else {
				cek = true;
			}
		}
		while(cek == false);
		do {			
				do {
					System.out.print("Input Answer : ");
					answer = scan.nextLine();
					if(answer.equals("")) {
						System.out.println("Answer can not be empty");
						cek = false;
					}
					else {
						cek = true;
					}
				}
				while(cek == false);
				AnswerQuiz answerQuiz = new AnswerQuiz(qIdx, answer, username);
				
				try {
					sendPacket("!menu2");
					sendPacket(serialize(answerQuiz));
					String msg = receivePacketString();
					if(!msg.equals("oke")) {
						System.out.println(msg);
						if(msg.equals("You answer the question wrong")) {
							System.out.print("Do you want to answer again? [Yes | No (Case Insensitive) : ");
							again = scan.nextLine();
							if(again.equalsIgnoreCase("Yes")) {
								cek = false;
							}
							else {
								return;
							}
						}
					}
					else {
						System.out.println("You answer the question correct");
						scan.nextLine();
						cek = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		while(cek == false);
	}

	public Client() {
		byte[] temp = new byte[1024];
		String chooseMenu;
		DatagramPacket packet;

		try {
			socket = new DatagramSocket();
			listenSocket = new MulticastSocket(listenPort);

			inetAddr = InetAddress.getByName("224.0.0.2");
			listenSocket.joinGroup(inetAddr);

			int choose = 0;
			clear();
			System.out.println("+==========================+");
			System.out.println("|          GAhoot!         |");
			System.out.println("+==========================+");
			System.out.println();
			do {
				System.out.print("Input Username [can not contain space] : ");
				username = scan.nextLine();		
				usernameSend = "!add" + username;
				sendPacket(usernameSend.getBytes());
				
				String msg = receivePacketString();
				boolean ok = msg.equals("oke");
				cek = ok;
				if (!ok) System.out.println(msg);
			} while (cek == false);
			
			BroadcastListenerThread thread = new BroadcastListenerThread(listenSocket);
			thread.start();

			while (true) {
				do {
					sendPacket("getQuestion");
					choose = scan.nextInt();
					scan.nextLine();
					chooseMenu = "!menu"+ choose;
					BroadcastListenerThread.canPrint = false;
					switch (choose) {
					case 1:
						clear();
						createQuiz();
						break;
					case 2:
						clear();
						answerQuiz(createQuiz);
						break;
					case 3:
						System.exit(0);
					}
				} while (choose < 1 || choose > 3);
				BroadcastListenerThread.canPrint = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client();
	}
	
	private static class BroadcastListenerThread extends Thread {

		private MulticastSocket listenSocket;
		static boolean canPrint = true;
		
		public void clear() {
			for (int i = 0; i <= 30; i++) {
				System.out.println();
			}
		}

		public BroadcastListenerThread(MulticastSocket listenSocket) {
			this.listenSocket = listenSocket;
		}
		
		private void prettyPrintQuestion(Vector<CreateQuiz> questions) {
			int i = 1;
			System.out.println();
			System.out.println("+====+==========================================+======================+");
			System.out.printf("| %-3s | %-40s| %-20s |\n", "No.", "Questions", "Creator");
			System.out.println("+====+==========================================+======================+");
			for (CreateQuiz createQuiz : questions) {
				System.out.printf("| %-3d | %-40s| %-20s |\n", i, createQuiz.getQuestion(), createQuiz.getUsername());
				i++;
			}
			System.out.println("+====+==========================================+======================+");
			System.out.println();
		}

		@Override
		public void run() {
			try {
				while (true) {
					byte[] buffer = new byte[64000];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					listenSocket.receive(packet);
					Vector<CreateQuiz> createQuiz = (Vector<CreateQuiz>) deserialize(packet.getData());
					Client.createQuiz = createQuiz;
					
					if(canPrint == false) {
						continue;
					}

					clear();
					prettyPrintQuestion(createQuiz);
					System.out.println("1. Create New Quiz");
					System.out.println("2. Answer Quiz");
					System.out.println("3. Exit");
					System.out.print("> ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
