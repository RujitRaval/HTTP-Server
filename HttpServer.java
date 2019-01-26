import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;

public class HttpServer implements Runnable{
	
	Socket socket;
	HttpServer(Socket s){
		socket = s;
	}
	
	public static void main(String[] args) {
		if(args.length!=1){
			System.out.println("You must provide a port number as an argument.");
			System.exit(-1);
		}
		else{
			try{
				int port = Integer.parseInt(args[0]);
				ServerSocket server = new ServerSocket(port);
				while (true) {
					Socket s = server.accept();
					
					new Thread(new HttpServer(s)).start();
				}
			}
			catch(NumberFormatException e){
				System.out.println("Port number must be a numeric value.");
				System.exit(-1);
			}
			catch(Exception e){
				System.out.println("Invalid port number is given.");
				System.exit(-1);
			}
		}
	}
	
	public void run(){
		try {
			InputStream input  = socket.getInputStream();
			String Method = "";
			String URI = null;
			String host = null;
			int Code = -1;
			byte[] body = null;
			
			try{
				Scanner in = new Scanner(input);
				String request = "";            	
				String Data = in.nextLine();
				request+=Data+"\n";
				Scanner Line = new Scanner(Data);
				Method = Line.next();
				URI = Line.next();
				
				String hostLine = null;
				parseHeader: while(in.hasNextLine()){
					String SecondLine = in.nextLine();
					
					if(SecondLine.length()>5 && SecondLine.substring(0,5).equals("Host:"))
							hostLine = SecondLine;
					request+=SecondLine+"\n";
						
					if(SecondLine.length()==0 || SecondLine.equals("\n"))
						break parseHeader;
				}

				System.out.println(request);
					
				if(hostLine!=null){ 
					Scanner hostScan = new Scanner(hostLine);
					if(hostScan.hasNext() && hostScan.next().equals("Host:")){
						host = hostScan.next();
						hostScan.close();
					} 
					else{
						Code = 400;
					}
				}
				else
				{
					Code = 400;
				}
			} 
			catch (Error e){
				Code = 400;
			}
			OutputStream output = socket.getOutputStream();
			
			if(Code!=400){
				switch(Method){
					case "GET":
						body = getResource(URI);
						if(body==null)
							Code= 404;
						else{
							Code = 200;
						}
						break;
					case "HEAD":
						body = getResource(URI);
						if(body==null)
							Code= 404;
						else{
							Code = 200;
						}
						break;
					case "OPTIONS":
						Code = 501;
						break;
					case "POST":
						Code = 501;
						break;
					case "PUT":
						Code = 501;
						break;
					case "DELETE":
						Code = 501;
						break;
					case "TRACE":
						Code = 501;
						break;
					case "CONNECT":
						Code = 501;
						break;
					default:
						Code = 400;
				}
			}
			
			 byte[] message = null;
				String headerString =("HTTP/1.1 "+Code+" "+getReasonPhrase(Code)+"\n"+
						"Server: CS6333HttpServer/1.0.0\n"+
						"Content-Length: "+lengthOfResource(body)+"\n"+
						"Content-Type: "+typeOfResource(URI)+"\n"+
						"\n");
				System.out.println(headerString);
				
				byte[] header = headerString.getBytes();
				if(Code==200 && Method.equals("GET")){
					message = new byte[header.length+body.length];
					ByteBuffer buf = ByteBuffer.wrap(message);
					buf.put(header);
					buf.put(body);
				}else{ 
					message = header;
				}
				output.write(message);
				output.close();
				input.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    } 

	public byte[] getResource(String URI){
		try {
			if(URI.substring(1).contains("/")){
				System.out.println("Server can not access subfolder in public_html.");
				return null; 
			}
			FileInputStream file = new FileInputStream ("public_html"+URI);			
			byte[] result = new byte[file.available()];
			file.read(result);
			return result;
			
		} 
		catch (IOException e) {
			System.out.println("couldn't find:"+("/public_html"+URI));
			return null;
		}
	}
	
	public static String typeOfResource(String URI){
		if(URI.matches(".*\\.html")||URI.matches(".*\\.htm"))
			return "text/html";
		if(URI.matches(".*\\.gif"))
			return "image/gif";
		if(URI.matches(".*\\.jpeg")||URI.matches(".*\\.jpg"))
			return "image/jpeg";
		if(URI.matches(".*\\.pdf"))
			return "application/pdf";
		return "text";
	}
	
	public static int lengthOfResource(byte[] body){
		if(body==null)
			return 0;
		else
			return body.length;
	}
	
	public static String getReasonPhrase(int Code){
		switch(Code){
			case 200: return "OK";
			case 400: return "Bad Request";
			case 404: return "Not Found";
			case 501: return "Not Implemented";
		}
		return null;
	}
}


