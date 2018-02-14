package org.opendatakit.briefcase.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.http.HttpHost;
import org.opendatakit.briefcase.model.BriefcaseAnalytics;
import org.opendatakit.briefcase.model.BriefcasePreferences;
import org.opendatakit.briefcase.util.FileSystemUtils;
import org.opendatakit.briefcase.util.StringUtils;

public class SettingsPanel extends JPanel {

  public static final String TAB_NAME = "Settings";

  private final JTextField txtBriefcaseDir = new JTextField();
  private final JCheckBox chkProxy = new JCheckBox(MessageStrings.PROXY_TOGGLE);
  private final JTextField txtHost = new JTextField();
  private final JSpinner spinPort = new JIntegerSpinner(8080, 0, 65535, 1);
  private final JCheckBox chkParallel = new JCheckBox(MessageStrings.PARALLEL_PULLS);
  private final JCheckBox chkTrackingConsent = new JCheckBox(MessageStrings.TRACKING_CONSENT);
  private final JCheckBox chkStorePasswordsConsent = new JCheckBox("Remember passwords (unencrypted)");

  SettingsPanel(final MainBriefcaseWindow parentWindow) {
    txtBriefcaseDir.setFocusable(false);
    txtBriefcaseDir.setEditable(false);
    txtBriefcaseDir.setColumns(50);

    final JButton btnChoose = new JButton("Change...");
    btnChoose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        WrappedFileChooser fc = new WrappedFileChooser(parentWindow.frame,
            new BriefcaseFolderChooser(parentWindow.frame));
        // figure out the initial directory path...
        String candidateDir = txtBriefcaseDir.getText();
        File base = null;
        if (candidateDir == null || candidateDir.trim().length() == 0) {
          // nothing -- use default
          base = new File(BriefcasePreferences.appScoped().getBriefcaseDirectoryOrUserHome());
        } else {
          // start with candidate parent and move up the tree until we have a valid directory.
          base = new File(candidateDir).getParentFile();
          while (base != null && (!base.exists() || !base.isDirectory())) {
            base = base.getParentFile();
          }
        }
        if (base != null) {
          fc.setSelectedFile(base);
        }
        int retVal = fc.showDialog();
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File parentFolder = fc.getSelectedFile();
          if (parentFolder != null) {
            String briefcasePath = parentFolder.getAbsolutePath();
            txtBriefcaseDir.setText(briefcasePath);
            BriefcasePreferences.setBriefcaseDirectoryProperty(briefcasePath);
            FileSystemUtils.createFormCacheInBriefcaseFolder();
            parentWindow.storageLocation.establishBriefcaseStorageLocation(parentWindow.frame, parentWindow);
          }
        }
      }
    });

    ProxyChangeListener proxyChangeListener = new ProxyChangeListener();

    txtHost.setEnabled(false);
    txtHost.addFocusListener(proxyChangeListener);

    spinPort.setEnabled(false);
    spinPort.addChangeListener(proxyChangeListener);

    chkProxy.setSelected(false);
    chkProxy.addActionListener(new ProxyToggleListener());

    chkParallel.setSelected(BriefcasePreferences.getBriefcaseParallelPullsProperty());
    chkParallel.addActionListener(new ParallelPullToggleListener());

    chkTrackingConsent.setSelected(BriefcasePreferences.getBriefcaseTrackingConsentProperty());
    chkTrackingConsent.addActionListener(new TrackingConsentToggleListener(parentWindow.briefcaseAnalytics));

    chkStorePasswordsConsent.setSelected(BriefcasePreferences.getStorePasswordsConsentProperty());
    chkStorePasswordsConsent.addActionListener(e -> {
      if (e.getSource() == chkStorePasswordsConsent) {
        boolean isSelected = chkStorePasswordsConsent.isSelected();
        BriefcasePreferences.setStorePasswordsConsentProperty(isSelected);
        if (!isSelected) {
          PullTransferPanel.PREFERENCES.remove(BriefcasePreferences.PASSWORD);
          PushTransferPanel.PREFERENCES.remove(BriefcasePreferences.PASSWORD);
        }
      }
    });


    final JLabel lblBriefcaseDir = new JLabel(MessageStrings.BRIEFCASE_STORAGE_LOCATION);
    final JLabel lblHost = new JLabel(MessageStrings.PROXY_HOST);
    final JLabel lblPort = new JLabel(MessageStrings.PROXY_PORT);

    lblBriefcaseDir.setText("Storage Location");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblBriefcaseDir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBriefcaseDir, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChoose))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkProxy)
                            .addComponent(chkParallel)
                            .addComponent(chkStorePasswordsConsent)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblPort)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(spinPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblHost)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBriefcaseDir)
                    .addComponent(txtBriefcaseDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChoose))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkParallel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkStorePasswordsConsent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkProxy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lblHost)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPort)
                    .addComponent(spinPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    setCurrentProxySettings();
  }

  private void setCurrentProxySettings() {
    HttpHost currentProxy = BriefcasePreferences.getBriefCaseProxyConnection();
    if (currentProxy != null) {
      chkProxy.setSelected(true);
      txtHost.setText(currentProxy.getHostName());
      txtHost.setEnabled(true);
      spinPort.setValue(currentProxy.getPort());
      spinPort.setEnabled(true);
    } else {
      txtHost.setText("127.0.0.1");
    }
  }

  JTextField getTxtBriefcaseDir() {
    return txtBriefcaseDir;
  }

  private void updateProxySettings() {
    BriefcasePreferences.setBriefcaseProxyProperty(new HttpHost(txtHost.getText(), (int) spinPort.getValue()));
  }

  class ProxyToggleListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == chkProxy) {
        if (chkProxy.isSelected()) {
          txtHost.setEnabled(true);
          spinPort.setEnabled(true);
          if (!StringUtils.isNotEmptyNotNull(txtHost.getText())) {
            txtHost.setText("127.0.0.1");
          }
          updateProxySettings();
        } else {
          txtHost.setEnabled(false);
          spinPort.setEnabled(false);
          BriefcasePreferences.setBriefcaseProxyProperty(null);
        }
      }
    }

  }

  class ProxyChangeListener implements FocusListener, ChangeListener {

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
      updateProxySettings();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      updateProxySettings();
    }

  }

  private class ParallelPullToggleListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == chkParallel) {
        BriefcasePreferences.setBriefcaseParallelPullsProperty(
            !BriefcasePreferences.getBriefcaseParallelPullsProperty());
      }
    }
  }

  /**
   * This listener notifies BriefcaseAnalytics of the users' updated choice
   * of consent about being tracked.
   */
  public class TrackingConsentToggleListener implements ActionListener {
    private final BriefcaseAnalytics briefcaseAnalytics;

    TrackingConsentToggleListener(BriefcaseAnalytics briefcaseAnalytics) {
      this.briefcaseAnalytics = briefcaseAnalytics;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == chkTrackingConsent) {
        briefcaseAnalytics.trackConsentDecision(chkTrackingConsent.isSelected());
      }
    }
  }
}