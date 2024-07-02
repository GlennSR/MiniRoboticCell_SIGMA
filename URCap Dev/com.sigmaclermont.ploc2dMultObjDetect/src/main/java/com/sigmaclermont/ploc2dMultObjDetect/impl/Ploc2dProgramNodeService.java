package com.sigmaclermont.ploc2dMultObjDetect.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class Ploc2dProgramNodeService implements SwingProgramNodeService<Ploc2dProgramNodeContribution, Ploc2dProgramNodeView>{

	@Override
	public String getId() {
		return "Ploc2dNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Ploc2D Multobj detection";
	}

	@Override
	public Ploc2dProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new Ploc2dProgramNodeView(apiProvider);
	}

	@Override
	public Ploc2dProgramNodeContribution createNode(ProgramAPIProvider apiProvider, Ploc2dProgramNodeView view,
			DataModel model, CreationContext context) {
		return new Ploc2dProgramNodeContribution(apiProvider, view, model);
	}

}
