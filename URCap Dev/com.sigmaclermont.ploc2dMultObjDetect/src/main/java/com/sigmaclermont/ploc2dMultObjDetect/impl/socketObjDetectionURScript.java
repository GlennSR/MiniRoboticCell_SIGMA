package com.sigmaclermont.ploc2dMultObjDetect.impl;

import com.ur.urcap.api.domain.script.ScriptWriter;

public class socketObjDetectionURScript {

	private String ip;
	private int port;
	private String jobs;

	public socketObjDetectionURScript(String ip, int port, String jobs) {
		this.ip = ip;
		this.port = port;
		this.jobs = jobs;
	}
	
	public void detect(ScriptWriter writer) {
		     
		writer.appendLine("global open = socket_open(\"" + ip + "\", " + port + ", \"socket_0\")");
		//writer.appendLine("popup(\"connecting\",title=\"Popup#1\",blocking=True)");
		
		writer.appendLine("global sendToServer = \"Run.LocateAll," + jobs + "\"");
		writer.appendLine("socket_send_line(sendToServer)");
		writer.appendLine("global result = socket_read_string()");
		writer.appendLine("socket_close(socket_name=\"socket_0\")");
		
		
	}
	
}
