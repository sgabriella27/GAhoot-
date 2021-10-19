import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Vector;

public class Server {
	
	int listenPort = 8088;
	int broadcastPort = 8888;
	InetAddress inetAddr;
	MulticastSocket listenSocket;
	MulticastSocket broadcastSocket;
	Vector<CreateQuiz> create;
	Vector<String> username = new Vector<>();

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
	
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}
	
	private void sendPacketTo(byte[] temp, InetAddress addr, int port) throws Exception {
		DatagramPacket packet = new DatagramPacket(temp, temp.length, addr, port);
		broadcastSocket.send(packet);
	}
	
	private void sendPacketBroadcast(byte[] temp) throws Exception {
		DatagramPacket packet = new DatagramPacket(temp, temp.length, inetAddr, broadcastPort);
		broadcastSocket.send(packet);
	}
	
	public void createQuiz() throws Exception {
		byte[] temp = new byte[64000];
		DatagramPacket packet = new DatagramPacket(temp, temp.length);
		listenSocket.receive(packet);
		CreateQuiz createQuiz = (CreateQuiz) deserialize(packet.getData());
		
		create.add(createQuiz);
	}
	
	public void answerQuiz() throws Exception {
		int index = 0;
		byte[] temp = new byte[64000];
		DatagramPacket packet = new DatagramPacket(temp, temp.length);
		listenSocket.receive(packet);
		AnswerQuiz answerQuiz = (AnswerQuiz) deserialize(packet.getData());
		index = answerQuiz.getIndex() - 1;
		CreateQuiz createQuiz =  create.get(index);
		
		if(createQuiz.getUsername().equals(answerQuiz.getUsername())) {
			String sameUname = "Can not answer your own question";
			sendPacketTo(sameUname.getBytes(), packet.getAddress(), packet.getPort());
		}
		else if(createQuiz.getAnswer().equalsIgnoreCase(answerQuiz.getAnswer())) {
			String correct = "oke";
			create.remove(index);
			sendPacketTo(correct.getBytes(), packet.getAddress(), packet.getPort());
		}
		else {
			String aFalse = "You answer the question wrong";
			sendPacketTo(aFalse.getBytes(), packet.getAddress(), packet.getPort());
		}
	}
	
	public Server() {
		create = new Vector<>(Arrays.asList(new CreateQuiz("buah", "apa itu jeruk?", "jeruk itu buah", "gaby")));
		System.out.println("+==================+");
		System.out.println("| GAhoot! - Server |");
		System.out.println("+==================+");
		try {
			listenSocket = new MulticastSocket(listenPort);
			broadcastSocket = new MulticastSocket(broadcastPort);
			
			inetAddr = InetAddress.getByName("224.0.0.2");
			listenSocket.joinGroup(inetAddr);
			
			while (true) {
				byte[] temp = new byte[64000];
				String str = "";
				DatagramPacket packet = new DatagramPacket(temp, temp.length);
				listenSocket.receive(packet);
				
				str = new String(packet.getData(), 0, packet.getLength());
				
				if(str.contains("!add")) {
					if(username.contains(str)) {
						String existUname = "Username already exist!";
						sendPacketTo(existUname.getBytes(), packet.getAddress(), packet.getPort());
					}
					else {
						username.add(str);
						String oke = "oke";
						sendPacketTo(oke.getBytes(), packet.getAddress(), packet.getPort());
					}
				}
				else if(str.contains("!menu")) {
					switch (str) {
					case "!menu1":
						createQuiz();
						break;
					case "!menu2":
						answerQuiz();
						break;
					}
				}
				else if(str.contains("getQuestion")) {
					sendPacketBroadcast(serialize(create));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}
