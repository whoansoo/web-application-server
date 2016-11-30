package webserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);	
	public Socket connection;	
	private BufferedReader br;
	private Map<String, String> Header;
	private String method;
	private String path;	
	private Map<String, String> Body;
	private boolean logined;
	
	public HttpRequest(InputStream in) {		        
		try {
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine();
			String tmpHeader = "";
	        if (line == null) {
	            return;
	        }        
	        
	        log.debug("request line : {}", line);
	        
	        String[] tokens = line.split(" ");
	        setMethod(tokens[0]);
	        String[] tmpGetPath = null ;
	        if (tokens[0].equals("GET")) {
	        	tmpGetPath = tokens[1].split("\\?");
	        	setPath(tmpGetPath[0]);
	        }
	        else
	        	setPath(tokens[1]);

	        int contentLength = 0;
	        logined = false;
	        tmpHeader = "";
	        
	        while (!line.equals("")) {
	            line = br.readLine();
	            tmpHeader = tmpHeader + line + "\n";
	            
	            if (line.contains("Content-Length")) {
	                contentLength = getContentLength(line);
	            }

	            if (line.contains("Cookie")) {
	                logined = isLogin(line);
	            }
	        }
	        	
	        this.Header = HeaderSplit(tmpHeader);	
	        String body = IOUtils.readData(br, contentLength);
	        if (getMethod().equals("GET")) {
		        // get parameter 일경우
	        	Body = HttpRequestUtils.parseQueryString(tmpGetPath[1]);
	            
	        } else {
	        	// post parameter 일 경우
	        	Body = HttpRequestUtils.parseQueryString(body);
	        }
            
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}

	private Map<String, String> HeaderSplit(String tmpheader) {		
		String[] tokens = tmpheader.split("\n");
		Map<String, String> mapHeader = new HashMap<String, String>();

		for(int i=0; i<tokens.length ; i++) {
			String[] tmpToken = tokens[i].split(":");
			mapHeader.put(tmpToken[0], tmpToken[1].trim());			
		}
			
		return mapHeader; 
	}
	
    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }
    

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }    
    
	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
	
	public String getParameter(String param) {
		return this.Body.get(param);
	}
	
	public String getURI() {
		String[] headerParameter = getPath().split("\\?");
		if (headerParameter.length > 0) return headerParameter[1].trim();
		else return "";
	}
	
	public String getURL() {
		String[] headerParameter = getPath().split("\\?");
		return headerParameter[0];
	}

	public String getHeader(String keyValue) {
        return Header.get(keyValue);

	}

	public boolean getLogined() {
		return this.logined;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}