package com.srs.projectOb;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.srs.projectOb.Utils.AppConstant;
import com.srs.projectOb.Utils.AppUtils;
import com.srs.projectOb.network.NetworkRequester;
import com.srs.projectOb.pojo.ClientPojo;
import com.srs.projectOb.pojo.ProjectPojo;
import com.srs.projectOb.pojo.UserPojo;

/**
 *
 * @author srs152 On project selection trigger snap capturing service
 */
public class ProjectListFrame extends javax.swing.JFrame {

	private ArrayList<ProjectPojo> projectArrayList;
	private ArrayList<ProjectPojo> tempProjectsList;
	private ArrayList<ProjectPojo> clientProjectsList;
	private ArrayList<JRadioButton> radioArrayList;
	private ArrayList<ClientPojo> clientArrayList;
	private List<NameValuePair> param;
	private JPanel mRadioButtonPanel;
	private JScrollPane mJScrollPane;
	private int selectionPosition = -1;
	private int selectedClient = -1;

	/**
	 * Creates new form ProjectListFrame
	 */
	@SuppressWarnings("unchecked")
	public ProjectListFrame(ArrayList<ProjectPojo> arrayList) {
		projectArrayList = arrayList;
		if (projectArrayList != null) {
			tempProjectsList = (ArrayList<ProjectPojo>) projectArrayList.clone();
		}
		radioArrayList = new ArrayList<JRadioButton>();

		clientArrayList = AppUtils.getClientList();

		if (arrayList == null) {
			projectArrayList = AppUtils.getProjectList(-1);
			tempProjectsList = (ArrayList<ProjectPojo>) projectArrayList.clone();
		}

		if (tempProjectsList != null && tempProjectsList.size() == 1) {
			selectionPosition = 0;
			okButtonClick();
		}

		if (clientProjectsList == null) {
			clientProjectsList = (ArrayList<ProjectPojo>) projectArrayList.clone();
		}

		File memoFile = new File(AppConstant.MEMO_FILE);
		if (memoFile.exists()) {
			memoFile.delete();
		}
		initComponents();
		for (int i = 0; i < clientArrayList.size(); i++) {
			// memoTaskList[i] = taskList.get(i).taskSummary;
			clientDropDown.addItem(clientArrayList.get(i).clientName);
		}
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		mJScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mJScrollPane.setBounds(5, 100, 215, 290);
		add(mJScrollPane);

		mRadioButtonPanel = new JPanel(new GridLayout(1, 1));

		// radioButtonPanel.setBackground(Color.red);
		// addProjectToPanel();
		// mJScrollPane.setBorder(null);
		// mRadioButtonPanel.setBorder(null);
		// jScrollPane.setBackground(Color.BLUE);

		mJScrollPane.setViewportView(mRadioButtonPanel);

		JButton okButton = new JButton("OK");
		okButton.setBounds(90, 400, 50, 30);
		add(okButton);
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonClick();
			}
		});

		jRefreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				login();
			}
		});

		jSearchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// Filter list and show on panel
				String serachText = jSearchTextField.getText().trim().toLowerCase();
				if (serachText.isEmpty()) {
					projectArrayList = (ArrayList<ProjectPojo>) clientProjectsList.clone();
				} else {
					projectArrayList.clear();
					for (ProjectPojo pojo : clientProjectsList) {
						if (pojo.projectName.trim().toLowerCase().contains(serachText)) {
							projectArrayList.add(pojo);
						}
					}
				}
				radioArrayList.clear();
				addProjectToPanel();
				System.out
						.println("Size = " + clientProjectsList.size() + " Original Size = " + projectArrayList.size());
			}
		});

		clientDropDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				refreshProjectPanel();
			}
		});
	}

	protected void refreshProjectPanel() {
		// Filter list and show on panel
		jSearchTextField.setText("");
		if(clientDropDown.getSelectedIndex() != -1)
		{
			selectedClient = clientArrayList.get(clientDropDown.getSelectedIndex()).clientId;
		}else{
			selectedClient = -1;
		}
		
		projectArrayList.clear();
		projectArrayList = AppUtils.getProjectList(selectedClient);
		tempProjectsList = (ArrayList<ProjectPojo>) projectArrayList.clone();
		projectArrayList.clear();
		for (ProjectPojo pojo : tempProjectsList) {
			if (pojo.clientId == selectedClient) {
				projectArrayList.add(pojo);
			}
		}
		clientProjectsList = (ArrayList<ProjectPojo>) projectArrayList.clone();
		radioArrayList.clear();
		addProjectToPanel();
		System.out.println("Size = " + tempProjectsList.size() + " Original Size = " + projectArrayList.size());
		
	}

	private void okButtonClick() {

		try {

			if (selectedClient == -1) {
				JOptionPane.showMessageDialog(null, "Please select client.");
				return;
			}

			for (int i = 0; i < radioArrayList.size(); i++) {
				if (radioArrayList.get(i).isSelected()) {
					selectionPosition = i;
				}
			}

			if (selectionPosition > -1) {
				if (AppConstant.MEMO_REQUEST_FLAG) {
					if (AppUtils.netIsAvailable()) {
						UserPojo userPojo = AppUtils.getUserPojo(null);
						param = new ArrayList<NameValuePair>();
						param.add(new BasicNameValuePair("project_id",
								String.valueOf(projectArrayList.get(selectionPosition).id)));
						param.add(new BasicNameValuePair("authkey", userPojo.authKey));
						param.add(new BasicNameValuePair("user_id", userPojo.userId));

						NetworkRequester.sendPost(AppConstant.URL + AppConstant.PROJECT_TASKS, param,
								new NetworkRequester.NetworkInterface() {

									@Override
									public void inResponseSuccess(String response) {
										AppUtils.writeResponse(AppConstant.MEMO_LIST_BY_PROJECT_FILE, response);
									}
								});
					}
				}

				AppUtils.writeResponse(AppConstant.PROJECT_MEMO_FILE,
						"" + AppUtils.setWritePojectPojo(projectArrayList.get(selectionPosition)));
				if (AppConstant.timeForMemo == null) {
					AppConstant.timeForMemo = new Timer(); // Instantiate Timer Object
					MemoRepeatTask st = new MemoRepeatTask(); // Instantiate SheduledTask class
					AppConstant.timeForMemo.schedule(st, 0, AppConstant.MEMO_REPEAT_TIME);
					// CreateRepetitively task for every 30 secs
				} else {
					AppConstant.timeForMemo.cancel();
					AppConstant.timeForMemo.purge();
					AppConstant.timeForMemo = new Timer(); // Instantiate Timer Object
					MemoRepeatTask st = new MemoRepeatTask(); // Instantiate SheduledTask class
					AppConstant.timeForMemo.schedule(st, 0, AppConstant.MEMO_REPEAT_TIME); 
					// Create repetitively task for every 30 min
				}
				AppUtils.snapCaptureService();
				dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Please select project.");
			}

		} catch (Exception e) {
			AppUtils.logger.error("Exception in ProjectListFrame :: " + e.getMessage());
		}

	}

	private void login() {
		try {
			if (AppUtils.netIsAvailable()) {
				
				param = new ArrayList<NameValuePair>();
				if (AppConstant.ACTIVE_DIRECTORY) {
					AppConstant.username = System.getProperty("user.name");
					param.add(new BasicNameValuePair("username", AppConstant.username));
					param.add(new BasicNameValuePair(AppConstant.ACTIVE_DIRECTORY_KEY,
							AppConstant.ACTIVE_DIRECTORY_VALUE));
				} else {
					String response = AppUtils.readResponse(AppConstant.PROPERTIES_FILE);
					UserPojo userPojo = null;
					if (response != null && !response.trim().equals("")) {
						String prevPath = (String) ((JSONObject) JSONValue.parse(response)).get(AppConstant.PREV_PATH);
						userPojo = AppUtils.getUserPojo(prevPath);
					}
					if (userPojo != null) {
						AppConstant.username = userPojo.emailId;
						String password = userPojo.password;
						param.add(new BasicNameValuePair("username", AppConstant.username));
						param.add(new BasicNameValuePair("password", password));
					}
				}
				NetworkRequester.sendPost(AppConstant.URL + AppConstant.NEW_LOGIN_URL, param,
						new NetworkRequester.NetworkInterface() {

							@Override
							public void inResponseSuccess(String response) {
								System.out.println("Username :: " + AppConstant.username);
								if (parseResponse(response)) {
									AppUtils.writeResponse(AppConstant.LOGIN_FILE, response);
									clientArrayList = AppUtils.getClientList();
									if (clientDropDown.getItemCount() > 0)
									{
										clientDropDown.removeAllItems();
									}
//									refreshProjectPanel();
									for (int i = 0; i < clientArrayList.size(); i++) {
										// memoTaskList[i] =
										// taskList.get(i).taskSummary;
										clientDropDown.addItem(clientArrayList.get(i).clientName);
									}
								}
							}
						});
			}
		} catch (Exception e) {
			AppUtils.logger.error("Exception in Projectlist login request ::" + e.getMessage());
		}
	}

	private boolean parseResponse(String response) {
		// Json parsing
		JSONObject jSONObject = (JSONObject) JSONValue.parse(response);
		if (!((String) jSONObject.get("status")).equals("") && !((String) jSONObject.get("status")).equals("error")) {
			return true;
		} else {
			return false;
		}
	}

	private void addProjectToPanel() {
		getContentPane().invalidate();
		mJScrollPane.setBorder(null);
		mRadioButtonPanel.setBorder(null);
		mRadioButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		Box box = Box.createVerticalBox();
		for (int i = 0; i < projectArrayList.size(); i++) {
			JRadioButton jRadioButton = new JRadioButton(projectArrayList.get(i).projectName);
			radioArrayList.add(jRadioButton);
			// jRadioButton.setBounds(10, 25 * (i + 2), 200, 25);
			box.add(jRadioButton);
			radioButtonGroup.add(jRadioButton);
		}
		mRadioButtonPanel.removeAll();
		mRadioButtonPanel.add(box);
		mJScrollPane.add(mRadioButtonPanel);
		mJScrollPane.revalidate();
		mJScrollPane.setViewportView(mRadioButtonPanel);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		jSearchTextField = new javax.swing.JTextField();
		jSearchButton = new javax.swing.JButton();
		jRefreshButton = new JButton();
		radioButtonGroup = new javax.swing.ButtonGroup();
		jLabel1 = new javax.swing.JLabel();
		clientDropDown = new javax.swing.JComboBox();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		// jLabel1.setFont(new java.awt.Font("Droid Serif", 1, 15)); // NOI18N
		jRefreshButton.setIcon(new ImageIcon(AppUtils.createImage("/images/refresh.png", "Refresh")));
		jSearchButton.setText("Search");
		jLabel1.setText("Select Project");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(72, 72, 72).addComponent(jLabel1).addContainerGap(77,
						Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addContainerGap(419,
						Short.MAX_VALUE)));
		getContentPane().add(jSearchTextField);
		getContentPane().add(jSearchButton);
		getContentPane().add(jRefreshButton);
		getContentPane().add(clientDropDown);
		clientDropDown.setBounds(5, 25, 195, 25);
		jSearchTextField.setBounds(5, 60, 140, 25);
		jSearchButton.setBounds(150, 60, 75, 25);
		jRefreshButton.setBounds(199, 25, 25, 25);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel1;
	private javax.swing.ButtonGroup radioButtonGroup;
	private javax.swing.JButton jSearchButton;
	private javax.swing.JButton jRefreshButton;
	private javax.swing.JTextField jSearchTextField;
	private javax.swing.JComboBox clientDropDown;
	// End of variables declaration//GEN-END:variables
}
