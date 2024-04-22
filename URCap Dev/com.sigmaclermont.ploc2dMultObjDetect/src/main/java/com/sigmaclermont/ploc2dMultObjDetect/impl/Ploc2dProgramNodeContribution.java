package com.sigmaclermont.ploc2dMultObjDetect.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

public class Ploc2dProgramNodeContribution implements ProgramNodeContribution{
	
	private final ProgramAPIProvider apiProvider;
	private final Ploc2dProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	public Ploc2dProgramNodeContribution(ProgramAPIProvider apiProvider, Ploc2dProgramNodeView view,
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
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
	}

	@Override
	public void closeView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		return "Ploc2D MultiObj";
	}

	@Override
	public boolean isDefined() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// TODO Auto-generated method stub
		
	}

}
