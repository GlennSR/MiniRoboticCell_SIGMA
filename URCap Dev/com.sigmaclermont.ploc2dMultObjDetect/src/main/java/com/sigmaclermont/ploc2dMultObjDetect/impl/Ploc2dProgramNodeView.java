package com.sigmaclermont.ploc2dMultObjDetect.impl;

import java.awt.Component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalCheckBoxIcon;
import javax.swing.JList;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;


public class Ploc2dProgramNodeView implements SwingProgramNodeView<Ploc2dProgramNodeContribution>{
	
	private final ViewAPIProvider apiProvider;
	private List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
	
	public Ploc2dProgramNodeView(ViewAPIProvider apiProvider) {
		
		for (int i=0; i < 64; i++) {
			checkBoxList.add(new JCheckBox(Integer.toString(i+1)));
		}
		
		this.apiProvider = apiProvider;
	}
	
	//private List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
	
	/*private class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

	    public Component getListCellRendererComponent(JList list, Object value, int index, 
	            boolean isSelected, boolean cellHasFocus) {

	        setComponentOrientation(list.getComponentOrientation());
	        setFont(list.getFont());
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	        setSelected(isSelected);
	        setEnabled(list.isEnabled());

	        setText(value == null ? "" : value.toString());  

	        return this;
	    }
	}
	
	private CheckboxListCellRenderer checkBoxListRend = new CheckboxListCellRenderer();*/
	//private JCheckBox[] checkBoxList = new JCheckBox[8];
	
	
	//DefaultListModel<JCheckBox> model = new DefaultListModel<JCheckBox>();
	//JCheckBoxList checkBoxLt = new JCheckBoxList(model);
	private JCheckBox checkBoxOrig = new JCheckBox();
	/*private JComboBox<Integer> ioComboBox = new JComboBox<Integer>();
	private JSlider durationSlider = new JSlider();*/

	@Override
	public void buildUI(JPanel panel, ContributionProvider<Ploc2dProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createDescription("Select the active Jobs:"));
		panel.add(createJobsPanel(checkBoxList));
		panel.add(createDescription("test"));
		
		
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
	
	public void setIOCheckBoxItems(Integer[] items) {
		
		for (int i=0; i < 64; i++) {
			checkBoxList.get(i).setIcon(new MetalCheckBoxIcon() {
				protected int getControlSize() {return 40;}
			});
			checkBoxList.get(i).setFont(new java.awt.Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 25));
			checkBoxList.get(i).setText("1");
		}
		
	}
	
	private Box createDescription(String desc) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label = new JLabel(desc);
		box.add(label);
		
		return box;
	}
	
	/*private Box createJobsPanel(final JCheckBox checkBox) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel panel = new JPanel();
		panel.add(checkBox);
		panel.add(checkBox);
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(300,300));
		scrollPane.setMaximumSize(scrollPane.getPreferredSize());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		checkBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		box.add(scrollPane);
		
		return box;
	}*/
	
	private Box createJobsPanel(final List<JCheckBox> checkBoxList) {
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
		
		
		for (JCheckBox checkBox: checkBoxList) {
			checkBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					// TODO Auto-generated method stub
					
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
	}
	
	
	private Component createSpacer(int height) {
		return Box.createRigidArea(new Dimension(0, height));
	}*/

}
