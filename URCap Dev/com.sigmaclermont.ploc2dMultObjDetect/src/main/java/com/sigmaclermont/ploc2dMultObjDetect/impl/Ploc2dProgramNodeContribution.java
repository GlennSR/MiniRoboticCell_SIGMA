package com.sigmaclermont.ploc2dMultObjDetect.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.value.expression.InvalidExpressionException;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableException;
import com.ur.urcap.api.domain.variable.VariableFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;

public class Ploc2dProgramNodeContribution implements ProgramNodeContribution{
	
	private final String ARTIFACTID = "multiobjectscript";
	public static final String SELECTED_VAR = "1";
	
	private final String IP_ADDRESS = "IpAddress";
	private Socket socket;
	private boolean isConnected = false;
	private String imagePathChecked = "/home/ur//workspace/com.sigmaclermont.ploc2dMultObjDetect/src/main/resources/com/sigma/urcap/multiobjectscript/impl/checked.png";
	private String imagePathUnchecked = "/home/ur//workspace/com.sigmaclermont.ploc2dMultObjDetect/src/main/resources/com/sigma/urcap/multiobjectscript/impl/Unchecked.png";
	
	private final ProgramAPIProvider apiProvider;
	private final Ploc2dProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	private final KeyboardInputFactory keyboardInputFactory;
	private final VariableFactory variableFactory;
	private final ProgramAPI programAPI;
	
	private Set<Integer> selectedJobs = new HashSet<Integer>();
	private int job_id;
	private String RESULT = "";
	
	public Ploc2dProgramNodeContribution(ProgramAPIProvider apiProvider, Ploc2dProgramNodeView view,
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.programAPI = apiProvider.getProgramAPI();
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
		this.keyboardInputFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.variableFactory = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
	}
	
	public void onOutputCheck(final Integer job) {
		undoRedoManager.recordChanges(new UndoableChanges() {

			@Override
			public void executeChanges() {
				selectedJobs.add(job);
			}
		});
	}
	
	public void onOutputUncheck(final Integer job) {
		undoRedoManager.recordChanges(new UndoableChanges() {

			@Override
			public void executeChanges() {
				selectedJobs.remove(job);
			}
			
		});
	}
	
	private String getIpAddress() {
		return model.get(IP_ADDRESS, "");
	}
	
	private String[] getConnection() {
		String[] c = new String[2];
		
		if(isConnected) {
			c[0] = "Online";
			c[1] = imagePathChecked;
			return c;
		}
		
		c[0] = "Offline";
		c[1] = imagePathUnchecked;
		return c;
	}
	
	public KeyboardTextInput getKeyboardForIpAddress() {
		KeyboardTextInput keyboard = keyboardInputFactory.createIPAddressKeyboardInput();
		keyboard.setInitialValue(model.get(IP_ADDRESS, ""));
		return keyboard;
	}
	
	public KeyboardInputCallback<String> getCallbackForIpAddress() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				model.set(IP_ADDRESS, value);
				view.setIpAddress(value);
			}
		};
	}

	private Integer[] getOutputItems() {
		Integer[] items = new Integer[64];
		for(int i = 0; i<64; i++) {
			items[i] = i;
		}
		return items;
	}
	
	@Override
	public void openView() {
		view.setIOCheckBoxItems(getOutputItems());
		view.setIOCheckBoxSelection(selectedJobs);
		view.setIpAddress(getIpAddress());
		view.setConnectionStatus(getConnection()[0], getConnection()[1]);
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "Ploc2D MultiObj";
	}

	@Override
	public boolean isDefined() {
		return true;
	}
	
	public void openSocketConnection(int port) {
		try {
			socket = new Socket(getIpAddress(),port);
			isConnected = true;
			view.setConnectionStatus("Online", imagePathChecked);
		} catch (Exception e) {
			System.out.println(e);
			view.setConnectionStatus("Connection Failed", imagePathUnchecked);
			try {
				if(socket.isConnected()) {
					socket.close();
					System.out.println("Btn Closing");
				}
				isConnected = false;
				view.setConnectionStatus("Offline", imagePathUnchecked);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		System.out.println("Button1 " + socket.isClosed() + ", " + isConnected);
	}

	private String socketCommunication(ScriptWriter writer, int port) {
		try{   
			System.out.println("Trying " + socket.isClosed() + ", " + isConnected);
			if (socket.isClosed() & isConnected) {
				System.out.println("Openning socket");
				socket = new Socket(getIpAddress(),port);
			}
			System.out.println("Sending");
			// Send command
			// Extracting the selected jobs to be located and formatting to string
			String jobs = "";
			for (Integer job: selectedJobs) {
				jobs += " " + job;
			}
			
			DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
			dout.writeBytes("Run.LocateAll," + jobs + "\n");
			dout.flush();

			// Another way to read, it works with all versions of PLOC2D URCaps
			char[] array = new char[9999];
			InputStreamReader din = new InputStreamReader(socket.getInputStream(), Charset.forName("UTF8"));
			din.read(array);
			RESULT = new String(array);
			RESULT = RESULT.trim();

			// This reading method only works with PLOC2D URCap 3.0.0.47+
			// Reading response
			/*DataInputStream din = new DataInputStream(socket.getInputStream());
			
			RESULT = "";
			int charCode; 
            while ((charCode = din.read()) != -1) { 
                char character = (char) charCode;
                RESULT += character;
                if(character == '\n')
                	break;
            }*/
			
			// Treating response
            List<String> result = new ArrayList<String>(Arrays.asList(RESULT.split(",")));
            List<List<String>> resultList = new ArrayList<List<String>>();
           
            if (result.get(0).equals("Run.LocateAll.Ok")) {
            	resultList.add(result.subList(0, 2));
                for(int i = 2; i < result.size(); i+=11) {
                	result.set(i+2, Float.toString(Float.parseFloat(result.get(i+2))/1000f));
                	result.set(i+3, Float.toString(Float.parseFloat(result.get(i+3))/1000f));
                	result.set(i+7, Float.toString((float) Math.toRadians(Float.parseFloat(result.get(i+7)))));
                	resultList.add(result.subList(i, i+11));
                }
                
                for(int i = 1; i < resultList.size(); i++) {
                	writer.assign("global PosPart" + i, "p" + resultList.get(i).subList(2, 8).toString());
                	writer.assign("global Part" + i + "_INFO", "[" + resultList.get(i).get(0) + ","
                			+ resultList.get(i).get(1) + ","
                			+ resultList.get(i).get(9) + "]");
                }
                writer.appendLine("global MultObj_STATUS = \"Job(s) has been found\"");
            } else if (result.get(4).equals(" 9600\n")) {
            	writer.appendLine("global MultObj_STATUS = \"No job found\"");
            } else {
            	writer.appendLine("global MultObj_STATUS = \"Invalid job\"");
            }
            writer.sleep(0.5);
			// Debug only
			/*int i = 0;
			while(i < RESULT.length()) {
				System.out.println(": " + RESULT.charAt(i) + ": " + (int) RESULT.charAt(i));
				i++;
			}
			System.out.println(RESULT.length());*/
			socket.close();
            if(socket.isClosed()) {
            	System.out.println("Closing");
            } else {
            	System.out.println("Still Open");
            }
            
			din.close();
			dout.close();
			}
		catch(Exception e){System.out.println("send " + e);}
		return "";
	}

	private void socketCommunicationURScript(ScriptWriter writer, String ip, int port) {
		if (isConnected) {
			writer.appendLine("open = socket_open(\"" + ip + "\", " + port + ", \"socket_0\")");
			//writer.appendLine("popup(\"connecting\",title=\"Popup#1\",blocking=True)");
			
			writer.appendLine("while (open == False): open = socket_open(\"" + ip + "\", " + port + ", \"socket_0\") end");
			//writer.appendLine("popup(\"socket connected\",title=\"Popup#2\",blocking=True)");
		}  
	}
		
	private void locateAll(ScriptWriter writer) {
		String jobs = "";
		for (Integer job: selectedJobs) {
			jobs += " " + job;
		}
		
		writer.appendLine("sendToServer = \"Run.LocateAll," + jobs + "\"");
		writer.appendLine("socket_send_line(sendToServer)");
		writer.appendLine("results = socket_read_string()");
		//writer.appendLine("socket_close(socket_name=\"socket_0\")");
	}
	
	public String[] readScriptFile(String filename) {
		try {

			BufferedReader br = new BufferedReader(
					new InputStreamReader(this.getClass().getResourceAsStream(filename)));

			ArrayList<String> list = new ArrayList<String>();

			String addstr;
			while ((addstr = br.readLine()) != null) {
				list.add(addstr);
			}

			br.close();
			String[] res = list.toArray(new String[0]);
			return res;

		} catch (IOException e) {
			return null;
		}

	}
	
	// DELETE
	public GlobalVariable createGlobalVariable(String variableName) {
		GlobalVariable variable = null;
		try {
			variable = variableFactory.createGlobalVariable(variableName, programAPI.getValueFactoryProvider().createExpressionBuilder().append("0").build());
		} catch (VariableException e) {
			e.printStackTrace();
		} catch (InvalidExpressionException e) {
			e.printStackTrace();
		}
		return variable;
	}
	// DELETE
	public Collection<Variable> getGlobalVariables() {
		return programAPI.getVariableModel().get(new Filter<Variable>() {
			@Override
			public boolean accept(Variable element) {
				return element.getType().equals(Variable.Type.GLOBAL) || element.getType().equals(Variable.Type.VALUE_PERSISTED);
			}
		});
	}
	// DELETE
	public Variable getSelectedVariable() {
		return model.get(SELECTED_VAR, (Variable) null);
	}
	// DELETE
	public void setVariable(final Variable variable) {
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(SELECTED_VAR, variable);
			}
		});
	}
	
	@Override
	public void generateScript(ScriptWriter writer) {
		
		//socketCommunication(writer, 14158);
		
		//socketCommunicationURScript(writer, getIpAddress(), 14158);
		//locateAll(writer);
		
		if (isConnected) {
			socketCommunicationURScript(writer, getIpAddress(), 14158);
		}
		
		locateAll(writer);
		
		
		String[] script1 = readScriptFile("/com/sigma/urcap/" + ARTIFACTID + "/impl/Mult_obj_detection.script");
		for (String str : script1) {
			//str = str.replace("{jobs}", jobs.toString());
			writer.appendLine(str);
		}
		
		
		
		writer.appendLine("PosPart_i=p[X,Y,0,0,0,RZ]");
		writer.appendLine("Part_INFO_i = p[match,job,percentage,0,0,0]");
		writer.appendLine("PartPosition.append(PosPart_i)");
		writer.appendLine("Part_INFO.append(Part_INFO_i)");
		
		/*Variable variable = getSelectedVariable();
		
		if (variable != null) {
			//Use writer to resolve name, as the variable can be renamed at any time by the end user.
			String resolvedVariableName = writer.getResolvedVariableName(variable);
			writer.appendLine("global " + resolvedVariableName + "=" + resolvedVariableName + "+1");

			//Note that the two code lines above can be replaced by the code line below which will resolve the variable
			//and increment it:
			//
			//writer.incrementVariable(variable);
			//
			//but to demonstrate renaming concerns the above lines are used.
		}*/
		
		String[] script2 = readScriptFile("/com/sigma/urcap/" + ARTIFACTID + "/impl/Mult_obj_detection_2.script");
		for (String str : script2) {
			writer.appendLine(str);
		}
		
		writer.appendLine("socket_close(socket_name=\"socket_0\")");
	}

}
