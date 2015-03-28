package com.norway240.slack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

public class Comms {

	private ServerSocket server;
    private int PORT;
	private String WEBHOOK;
	private String VERIFICATION;
	
	public Comms(String w, String v, int p){
		WEBHOOK = w;
		VERIFICATION = v;
		PORT = p;
	}
	
	@SuppressWarnings("unchecked")
	public void send(String name, String msg) throws IOException{
		URL object=new URL(WEBHOOK);
		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		
		JSONObject jobj = new JSONObject();
		jobj.put("username", name);
		jobj.put("icon_url", "http://signaturecraft.us/avatars/5/face/"+name+".png");
		jobj.put("text", msg);

		OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
		wr.write(jobj.toString());
		wr.flush();
		
		//display what returns the POST request
		StringBuilder sb = new StringBuilder();  
		int HttpResult =con.getResponseCode(); 
		if(HttpResult ==HttpURLConnection.HTTP_OK){
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		    sb.append(line + "\n");  
		    }  
		    br.close();  
		}
	}
    
	public void receive(){
		new Thread(new Runnable() {
	        public void run(){
	        	try {
	                server = new ServerSocket(PORT);
	                System.out.println("[SlackChat] message receiving server active " + PORT);
	                while (true) {
	                    new ThreadSocket(server.accept());
	                }
	            }catch(Exception e){
	            	System.out.println("[SlackChat] " + e.getStackTrace());
	            }
	       }
	    }).start();
	}
	public void close(){
		try {
			server.close();
		} catch (IOException e) {
        	System.out.println("[SlackChat] " + e.getStackTrace());
		}
	}
	
	public void chat(String s){
		//System.out.println(s);
		String token = "";
		String name = "", msg = "";
		String vars[] = s.split("&");
		for(String d : vars){
			String data[] = d.split("=");
			if(data[0].contains("user_name")){
				name = data[1];
			}else if(data[0].contains("text")){
				msg = data[1];
				try{msg = java.net.URLDecoder.decode(msg, "UTF-8");
				}catch(UnsupportedEncodingException e){e.printStackTrace();}
    			msg = msg.replace("&amp;", "&");
			}else if(data[0].contains("token")){
				token = data[1];
			}
		}
		
		if(token.contains(VERIFICATION)){
	    	boolean slackbot = (name.toString().equals("slackbot"));
		    if(!slackbot){
		    	Bukkit.getServer().broadcastMessage("[Slack] " + name + ": " + msg);
		    }
		}else{
			System.out.println("[SlackChat] Incoming message not verified");
		}
	}

}
class ThreadSocket extends Thread {
    private Socket insocket;
    ThreadSocket(Socket insocket) {
        this.insocket = insocket;
        this.start();
    }
    @Override
    public void run() {
        try {
            InputStream is = insocket.getInputStream();
            PrintWriter out = new PrintWriter(insocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            line = in.readLine();
            String request_method = line;
            //System.out.println("HTTP-HEADER: " + line);
            line = "";
            // looks for post data
            int postDataI = -1;
            while ((line = in.readLine()) != null && (line.length() != 0)) {
                //System.out.println("HTTP-HEADER: " + line);
                if (line.indexOf("Content-Length:") > -1) {
                    postDataI = new Integer(
                            line.substring(
                                    line.indexOf("Content-Length:") + 16,
                                    line.length())).intValue();
                }
            }
            String postData = "";
            // read the post data
            if (postDataI > 0) {
                char[] charArray = new char[postDataI];
                in.read(charArray, 0, postDataI);
                postData = new String(charArray);
            }
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html; charset=utf-8");
            out.println("Server: MINISERVER");
            // this blank line signals the end of the headers
            out.println("");
            // Send the HTML page
            out.println("<h1>Shenanigans</h1>");
            out.println("<h2>Request Method->" + request_method + "</h2>");
            out.println("<h2>Post->" + postData + "</h2>");
            out.close();
            insocket.close();
            
            Slack.comm.chat(postData);
        } catch (IOException e) {
        	System.out.println("[SlackChat] " + e.getStackTrace());
        }
    }
}