package com.sigmaclermont.ploc2dMultObjDetect.impl;

import java.awt.Component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalCheckBoxIcon;
import javax.swing.text.NumberFormatter;
import javax.swing.JList;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.variable.GlobalVariable;


public class Ploc2dProgramNodeView implements SwingProgramNodeView<Ploc2dProgramNodeContribution>{
	
	private final ViewAPIProvider apiProvider;
	private List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
	private JTextField ipFieldText = new JTextField();
	private JLabel isConnected = new JLabel();
	
	public Ploc2dProgramNodeView(ViewAPIProvider apiProvider) {
		
		for (int i = 0; i < 64; i++) {
			checkBoxList.add(new JCheckBox());
		}
		
		this.apiProvider = apiProvider;
	}
	
	@Override
	public void buildUI(JPanel panel, ContributionProvider<Ploc2dProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(connectToCamera(provider));
		panel.add(connectionStatusLabel());
		panel.add(createSpacer(15));
		panel.add(createDescription("Select the active Jobs:"));
		panel.add(createJobsPanel(checkBoxList, provider));
		
		/*panel.add(createSpacer(5));
		panel.add(createIOComboBox(ioComboBox, provider));
		panel.add(createSpacer(20));
		panel.add(createDescription("Select the duration of the Light Up:"));
		panel.add(createSpacer(5));*/
	}
	
	/*// Letting the Contribution set the list of items in the ComboBox
	public void setIOComboBoxItems(Integer[] items) {
		ioComboBox.removeAllItems();
		ioComboBox.setModel(new DefaultComboBoxModel<Integer>(items));
	}
	
	// Letting the Contribution set the actually selected item into the ComboBox
	public void setIOComboBoxSelection(Integer item) {
		ioComboBox.setSelectedItem(item);
	}
	
	}*/
	
	private Box connectToCamera(final ContributionProvider<Ploc2dProgramNodeContribution> provider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton button = new JButton("Connect");
		
		//ipFieldText.setFocusable(false);
		ipFieldText.setPreferredSize(new Dimension(250,50));
		ipFieldText.setMaximumSize(ipFieldText.getPreferredSize());
		
		ipFieldText.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				KeyboardTextInput keyboardInput = provider.get().getKeyboardForIpAddress();
				keyboardInput.show(ipFieldText, provider.get().getCallbackForIpAddress());
			}
		});
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				provider.get().openSocketConnection(14158);
				GlobalVariable variable = provider.get().createGlobalVariable("1");
				provider.get().setVariable(variable);
			}
		});
		
		box.add(button);
		box.add(Box.createRigidArea(new Dimension(15, 0)));
		box.add(ipFieldText);
		
		return box;
	}
	
	private Box connectionStatusLabel() {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		ImageIcon imageIcon = new ImageIcon("/home/ur/workspace/com.sigmaclermont.ploc2dMultObjDetect/Unchecked.png");
		isConnected.setText("Offline");
		isConnected.setIcon(imageIcon);
		
		box.add(isConnected);
		
		return box;
	}
	
	public void setConnectionStatus(String txt, String imagePath) {
		isConnected.setText(txt);
		isConnected.setIcon(new ImageIcon(imagePath));
	}
	
	public void setIpAddress(String value) {
		ipFieldText.setText(value);
	}
	
	public void setIOCheckBoxItems(Integer[] items) {
		for (Integer item: items) {
			checkBoxList.get(item).setText(Integer.toString(item+1));
			checkBoxList.get(item).setIcon(new MetalCheckBoxIcon() {
				protected int getControlSize() {return 40;}
			});
			checkBoxList.get(item).setFont(new java.awt.Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 25));
		}
	}
	
	public void setIOCheckBoxSelection(Set<Integer> selectedJobs) {
		
		for (JCheckBox checkbox: checkBoxList) {
			if(selectedJobs.contains(Integer.parseInt(checkbox.getText()))){
				checkbox.setSelected(true);
			} else {
				checkbox.setSelected(false);
			}
		}
	}
	
	private Box createDescription(String desc) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label = new JLabel(desc);
		box.add(label);
		
		return box;
	}
	
	Box createJobsPanel(final List<JCheckBox> checkBoxList,
			final ContributionProvider<Ploc2dProgramNodeContribution> provider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		
		for (JCheckBox checkBox: checkBoxList) {
			panel.add(checkBox);
		}
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(300,300));
		scrollPane.setMaximumSize(scrollPane.getPreferredSize());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		for (final JCheckBox checkBox: checkBoxList) {
			checkBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						provider.get().onOutputCheck(Integer.parseInt(checkBox.getText()));
					} else {
						provider.get().onOutputUncheck(Integer.parseInt(checkBox.getText()));
					}
				}
			});
		}
		
		box.add(scrollPane);
		
		return box;
	}
	
	/*private Box createIOComboBox(final JComboBox<Integer> combo,
			final ContributionProvider<Ploc2dProgramNodeContribution> provider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel label = new JLabel(" digital_out ");
		
		combo.setPreferredSize(new Dimension(104,30));
		combo.setMaximumSize(combo.getPreferredSize());
		
		combo.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					//provider.get().onOutputSelection((Integer) e.getItem());
				}
			}
		});
		
		box.add(label);
		box.add(combo);
		
		return box;
	}*/
	
	
	private Component createSpacer(int height) {
		return Box.createRigidArea(new Dimension(0, height));
	}

}
