/*
 * CreateBackupDialog.java - Jun 16, 2003
 * 
 * @author wolf
 */
package org.exist.backup;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

public class CreateBackupDialog extends JPanel {

	JComboBox collections;
	JTextField directory;
	String uri;
	String user;
	String passwd;

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 * @throws java.awt.HeadlessException
	 */
	public CreateBackupDialog(String uri, String user, String passwd, String backupDir)
		throws HeadlessException {
		super(false);
		this.uri = uri;
		this.user = user;
		this.passwd = passwd;
		setupComponents(backupDir);
		setSize(new Dimension(350, 200));
	}

	private void setupComponents(String backupDir) {
		GridBagLayout grid = new GridBagLayout();
		setLayout(grid);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);

		JLabel label = new JLabel("Collection:");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		grid.setConstraints(label, c);
		add(label);

		Vector v = getAllCollections();
		collections = new JComboBox(v);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		grid.setConstraints(collections, c);
		add(collections);

		label = new JLabel("Backup-Directory:");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		grid.setConstraints(label, c);
		add(label);

		directory = new JTextField(backupDir, 40);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		grid.setConstraints(directory, c);
		add(directory);

		JButton select = new JButton("Select");
		select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSelect();
			}
		});
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		grid.setConstraints(select, c);
		add(select);
	}

	private void actionSelect() {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(this, "Select directory for backup")
			== JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (f.exists()) {
				if (JOptionPane
					.showConfirmDialog(
						this,
						"Directory " + f.getAbsolutePath() + " exists. OK to delete?",
						"Confirm deletion",
						JOptionPane.YES_NO_OPTION)
					== JOptionPane.NO_OPTION)
					return;
			}
			directory.setText(f.getAbsolutePath());
		}
	}

	private Vector getAllCollections() {
		Vector list = new Vector();
		try {
			Collection root = DatabaseManager.getCollection(uri + "/db", user, passwd);
			getAllCollections(root, list);
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
		return list;
	}

	private void getAllCollections(Collection collection, Vector collections)
		throws XMLDBException {
		collections.add(collection.getName());
		String[] childCollections = collection.listChildCollections();
		Collection child;
		for (int i = 0; i < childCollections.length; i++) {
			child = collection.getChildCollection(childCollections[i]);
			getAllCollections(child, collections);
		}
	}

	public String getCollection() {
		return (String) collections.getSelectedItem();
	}

	public String getBackupDir() {
		return directory.getText();
	}
}
