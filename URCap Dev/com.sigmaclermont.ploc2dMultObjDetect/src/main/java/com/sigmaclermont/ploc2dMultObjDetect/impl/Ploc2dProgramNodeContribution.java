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
import com.ur.urcap.api.domain.feature.Feature;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.nodes.builtin.FolderNode;
import com.ur.urcap.api.domain.program.nodes.builtin.MoveNode;
import com.ur.urcap.api.domain.program.nodes.builtin.WaypointNode;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.MoveNodeConfig;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.builder.MoveJConfigBuilder;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.builder.MoveLConfigBuilder;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.builder.MoveNodeConfigBuilders;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.BlendParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointMotionParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointNodeConfig;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointNodeConfigFactory;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.PoseFactory;
import com.ur.urcap.api.domain.value.Position;
import com.ur.urcap.api.domain.value.ValueFactoryProvider;
import com.ur.urcap.api.domain.value.expression.InvalidExpressionException;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableException;
import com.ur.urcap.api.domain.variable.VariableFactory;
import com.ur.urcap.api.domain.variable.Variable.Type;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;

public class Ploc2dProgramNodeContribution implements ProgramNodeContribution{
	
	private final String ARTIFACTID = "multiobjectscript";
	public static final String SELECTED_VAR = "1";
	
	private final String IP_ADDRESS = "IpAddress";
	private final String HEIGHT = "Height";
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
	private GlobalVariable prepick;
	private GlobalVariable pick;
	private GlobalVariable clear;
	
	public Ploc2dProgramNodeContribution(ProgramAPIProvider apiProvider, Ploc2dProgramNodeView view,
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.programAPI = apiProvider.getProgramAPI();
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
		this.keyboardInputFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.variableFactory = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
		
		// Create and initialize global variables
		createVariables();
		// Create children
		createSubtree();
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
	
	private Integer getHeight() {
		return model.get(HEIGHT, 0);
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
	
	public KeyboardNumberInput<Integer> getKeyboardForHeight() {
		KeyboardNumberInput<Integer> keyboard = keyboardInputFactory.createIntegerKeypadInput();
		keyboard.setInitialValue(model.get(HEIGHT, 0));
		return keyboard;
	}
	
	public KeyboardInputCallback<Integer> getCallbackForHeight() {
		return new KeyboardInputCallback<Integer>() {
			@Override
			public void onOk(Integer value) {
				model.set(HEIGHT, value);
				view.setHeight(value);
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
		view.setHeight(getHeight());
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
	
	public void createVariables() {
		// Criação da variável de instalação do tipo pose
		prepick = createGlobalVariable("Part_prepick", "p[0,0,0,0,0,0]");
		pick = createGlobalVariable("Part_pick", "p[0,0,0,0,0,0]");
		clear = createGlobalVariable("Part_clear", "p[0,0,0,0,0,0]");
		
		model.set("prepick", prepick);
		model.set("pick", pick);
		model.set("clear", clear);
	}
	
	public GlobalVariable createGlobalVariable(String variableName, String expression) {
		GlobalVariable variable = null;
		try {
			variable = variableFactory.createGlobalVariable(variableName, programAPI.getValueFactoryProvider().createExpressionBuilder().append(expression).build());
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
		
		if (isConnected) {
			socketCommunicationURScript(writer, getIpAddress(), 14158);
		}
		
		locateAll(writer);
		
		String[] script1 = readScriptFile("/com/sigma/urcap/" + ARTIFACTID + "/impl/Mult_obj_detection.script");
		for (String str : script1) {
			writer.appendLine(str);
		}
		
		float correction;
		if (getHeight()<0)
			correction = 1;
		else
			correction = -1;
		
		writer.appendLine("global Part_prepick=p[X,Y," + correction*0.05 + ",0,0,RZ]");
		writer.appendLine("global Part_clear=Part_prepick");
		writer.appendLine("global Part_pick=p[X,Y," + Float.parseFloat(getHeight().toString())/-1000.0 + ",0,0,RZ]");
		writer.appendLine("global Part_INFO_i = struct(match=m,job=jb,percentage=pctg,pose=Part_pick,prepick=Part_prepick)");
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
		
		writer.writeChildren();
	}
	
	private void createSubtree() {
		ProgramModel programModel = programAPI.getProgramModel();
		ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		
		try {
			// Create parent node
            TreeNode rootNode = programModel.getRootTreeNode(this);
            
            // Start Node
            // Create folder and add to parent
            FolderNode folderNode = nf.createFolderNode().setName("Start");
            TreeNode childNode = rootNode.addChild(folderNode);
            
            // Create MoveJ node
            MoveNode moveNode = nf.createMoveNodeNoTemplate();
            
            // Create "Start" waypoint
            WaypointNode waypointNode = nf.createWaypointNode("Start");
            
            // Add waypoint to MoveJ node
            TreeNode moveJTreeNode = childNode.addChild(moveNode);
            moveJTreeNode.addChild(waypointNode);
            
            //////////////////////////////////////////////////////////////////////////
            // Pre Pick Node
            folderNode = nf.createFolderNode().setName("Approach");
            childNode = rootNode.addChild(folderNode);
            
            // Create MoveJ node
            moveNode = nf.createMoveNodeNoTemplate();
            
            // Create "PrePick" waypoint and set as variable point
            waypointNode = nf.createWaypointNode("PrePick");
            WaypointNodeConfigFactory waypointNodeConfigFactory = waypointNode.getConfigFactory();
            BlendParameters blendParameters = waypointNodeConfigFactory.createSharedBlendParameters();
    		WaypointMotionParameters motionParameters = waypointNodeConfigFactory.createSharedMotionParameters();
            WaypointNodeConfig waypointNodeConfig = waypointNodeConfigFactory.createVariablePositionConfig(prepick, blendParameters, motionParameters);
            waypointNode.setConfig(waypointNodeConfig);
            
            moveJTreeNode = childNode.addChild(moveNode);
            moveJTreeNode.addChild(waypointNode);
            
			//////////////////////////////////////////////////////////////////////////
			// PICK Node
            folderNode = nf.createFolderNode().setName("Pick");
            childNode = rootNode.addChild(folderNode);

            // Create MoveL node
            moveNode = nf.createMoveNodeNoTemplate();
            MoveLConfigBuilder moveLConfigBuilder = moveNode.getConfigBuilders().createMoveLConfigBuilder();
            moveNode.setConfig(moveLConfigBuilder.build());
         
            // Create "Pick" waypoint and set as variable point
            waypointNode = nf.createWaypointNode("Pick");
            waypointNodeConfigFactory = waypointNode.getConfigFactory();
            blendParameters = waypointNodeConfigFactory.createSharedBlendParameters();
    		motionParameters = waypointNodeConfigFactory.createSharedMotionParameters();
            waypointNodeConfig = waypointNodeConfigFactory.createVariablePositionConfig(pick, blendParameters, motionParameters);
            waypointNode.setConfig(waypointNodeConfig);
            
            moveJTreeNode = childNode.addChild(moveNode);
            moveJTreeNode.addChild(waypointNode);

			//////////////////////////////////////////////////////////////////////////
			// Clear Node
			folderNode = nf.createFolderNode().setName("Clear");
			childNode = rootNode.addChild(folderNode);
			
			// Create MoveJ node
            moveNode = nf.createMoveNodeNoTemplate();
			
			// Create "Clear" waypoint and set as variable point
			waypointNode = nf.createWaypointNode("Clear");
			waypointNodeConfigFactory = waypointNode.getConfigFactory();
			blendParameters = waypointNodeConfigFactory.createSharedBlendParameters();
			motionParameters = waypointNodeConfigFactory.createSharedMotionParameters();
			waypointNodeConfig = waypointNodeConfigFactory.createVariablePositionConfig(clear, blendParameters, motionParameters);
			waypointNode.setConfig(waypointNodeConfig);
			
			moveJTreeNode = childNode.addChild(moveNode);
			moveJTreeNode.addChild(waypointNode);
			
			/////////////////////////////////////////////////////////////////////////
			// Start Node
            // Create folder and add to parent
            folderNode = nf.createFolderNode().setName("Place");
            childNode = rootNode.addChild(folderNode);
            
            // Create MoveJ node
            moveNode = nf.createMoveNodeNoTemplate();
            
            // Create "Start" waypoint
            waypointNode = nf.createWaypointNode("Place");
            
            // Add waypoint to MoveJ node
            moveJTreeNode = childNode.addChild(moveNode);
            moveJTreeNode.addChild(waypointNode);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
