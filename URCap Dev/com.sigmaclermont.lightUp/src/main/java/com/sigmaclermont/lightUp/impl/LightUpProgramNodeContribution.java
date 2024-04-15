package com.sigmaclermont.lightUp.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LightUpProgramNodeContribution implements ProgramNodeContribution{
	
	private final ProgramAPIProvider apiProvider;
	private final LightUpProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	private static final String OUTPUT_KEY = "output";
	private static final String DURATION_KEY = "duration";
	
	private static String RESULT = "";
	
	private static final Integer DEFAULT_OUTPUT = 0;
	private static final int DEFAULT_DURATION = 1;

	public LightUpProgramNodeContribution(ProgramAPIProvider apiProvider, LightUpProgramNodeView view,
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
	}
	
	public void onOutputSelection(final Integer output) {
		undoRedoManager.recordChanges(new UndoableChanges() {

			@Override
			public void executeChanges() {
				model.set(OUTPUT_KEY, output);
			}
			
		});
	}
	
	public void onDurationSelection(final int duration) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(DURATION_KEY, duration);
			}
		});
		
	}
	
	private Integer getOutput() {
		return model.get(OUTPUT_KEY, DEFAULT_OUTPUT);
	}
	
	private int getDuration() {
		return model.get(DURATION_KEY, DEFAULT_DURATION);
	}
	
	private Integer[] getOutputItems() {
		Integer[] items = new Integer[8];
		for(int i = 0; i<8; i++) {
			items[i] = i;
		}
		return items;
	}
	
	@Override
	public void openView() {
		
		view.setIOComboBoxItems(getOutputItems());
		view.setIOComboBoxSelection(getOutput());
		view.setDurationSlider(getDuration());
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "LightUp: DO"+getOutput()+" t="+getDuration();
	}

	@Override
	public boolean isDefined() {
		return true;
	}
	public static void dumpOutputAsHex(Socket s) throws IOException {
		try (InputStream in = s.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buf = new byte[8192];
		    int length;
		    while ((length = in.read(buf)) > 0) {
		        out.write(buf, 0, length);
		    }
			System.out.println(new String(out.toByteArray()));}}
	
	private static void socketCommunication(ScriptWriter writer, String ip, int port) {
		try{      
			Socket s=new Socket(ip,port);  
			//DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			//dout.writeBytes("Run.LocateAll,3");
			//dout.flush();
			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			out.println("Run.LocateAll,3");
			out.flush();
			
			try (InputStream in = s.getInputStream();
			ByteArrayOutputStream dout = new ByteArrayOutputStream()) {
					byte[] buf = new byte[8192];
					int length;
					while ((length = in.read(buf)) > 0) {
					     dout.write(buf, 0, length);
					}}
			//System.out.println(new String(dout.toByteArray()));}
			
			//DataInputStream din = new DataInputStream(s.getInputStream());
			/*char[] array = new char[999];
			InputStreamReader din = new InputStreamReader(s.getInputStream(), Charset.forName("UTF8"));
			din.read(array);*/
			
			//BufferedReader myInput = new BufferedReader(
		    //new InputStreamReader(Connection.getInputStream()));
			
			/*int charCode; 
            while ((charCode = din.read()) != -1) { 
                char character = (char) charCode;
                RESULT += character;
                System.out.print(character); 
                if (character == '\n')
                    break; 
            }*/
            
            //List<String> myList = new ArrayList<String>(Arrays.asList(RESULT.split(":")));
            //System.out.print(array); 
			
			
			//din.close();
			out.close();  
			s.close();  
			}
		catch(Exception e){System.out.println(e);}
	}
	
	/*private static void socketCommunication(ScriptWriter writer, String ip, int port) {
	try{      
		writer.appendLine("open = socket_open(\"" + ip + "\", " + port + ")");
		writer.appendLine("popup(\"connecting\",title=\"Popup#1\",blocking=True)");
		
		writer.appendLine("while (open == False): open = socket_open(\"" + ip + "\", " + port + ") end");
		writer.appendLine("popup(\"socket connected\",title=\"Popup#2\",blocking=True)");
		}
	catch(Exception e){System.out.println(e);}   
	}
	
	private static void locateAll(ScriptWriter writer) {
		writer.appendLine("sendToServer = \"Run.LocateAll,2\"");
		writer.appendLine("socket_send_string(sendToServer)");
		writer.appendLine("global result = socket_read_string()");
	}*/

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.appendLine("set_standard_digital_out("+getOutput()+", True)");
		writer.sleep(getDuration());
		writer.appendLine("set_standard_digital_out("+getOutput()+", False)");
		//socketCommunication(writer, "192.168.1.4", 14158);
		//locateAll(writer);
		
		socketCommunication(writer, "192.168.1.4", 14158);
	}

}
