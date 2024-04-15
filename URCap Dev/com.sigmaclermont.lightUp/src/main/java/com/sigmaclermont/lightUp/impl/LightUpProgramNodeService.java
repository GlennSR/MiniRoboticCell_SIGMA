package com.sigmaclermont.lightUp.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class LightUpProgramNodeService implements SwingProgramNodeService<LightUpProgramNodeContribution, LightUpProgramNodeView>{

	@Override
	public String getId() {
		return "LightUpNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Light Up";
	}

	@Override
	public LightUpProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new LightUpProgramNodeView(apiProvider);
	}

	@Override
	public LightUpProgramNodeContribution createNode(ProgramAPIProvider apiProvider, LightUpProgramNodeView view,
			DataModel model, CreationContext context) {
		return new LightUpProgramNodeContribution(apiProvider, view, model);
	}

}
