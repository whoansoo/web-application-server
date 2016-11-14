package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        
        String ReponsString = "";
        
        try (InputStream   in = connection.getInputStream(); 
             OutputStream out = connection.getOutputStream()) {
        	
        	String uriFile = parseURL(in);
        	if (uriFile.equals("error")) {
        		log.error("null error");
        		ReponsString = "error";
        	} else {
        		uriFile = "./" + uriFile;
        		ReponsString = ReadIndexFile(uriFile);
        	}
        	
        	sendResponse(ReponsString, out);
                    
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void  sendResponse(String ReponsString, OutputStream out) {
    	
    	DataOutputStream dos = new DataOutputStream(out);
        byte[] body = ReponsString.getBytes();
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    
    private String parseURL(InputStream in) {
    	String line = "";
    	String tmpLine[];
    	
		try {
			    			
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        
	        StringBuffer request = new StringBuffer();
	        while(true) {
	        	int ch = br.read();
	        	if ((ch < 0) || (ch == '\n')) {
                    break;
                }
                request.append((char) ch);
	    	} 
	    		
	    	line = request.toString();
	    	System.out.println(line);
	    	
	    	tmpLine = line.split(" ");
    	
	    	if (!tmpLine[1].equals("/index.html") || tmpLine[1] == "" )
	    		return "error";
	    	else 
	    		return tmpLine[1];
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "error";	
		    
    }
    
    private String ReadIndexFile(String uriFile) {

        int i;
        String line = "";
		try {
			InputStream is;
			is = new FileInputStream(uriFile);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        
	        StringBuffer request = new StringBuffer();
	    	while((line = br.readLine()) != null) {
	    		request.append(line);	
	    		request.append("\r");
	    	}
	    	line = request.toString();
	    	
		} catch (Exception e) {			
			e.printStackTrace();
		}    	
    	return line;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
