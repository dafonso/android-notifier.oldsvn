package com.notifier.desktop.view;

import java.util.regex.*;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.google.common.base.*;
import com.notifier.desktop.*;
import com.notifier.desktop.app.*;

public class InstantMessagingDetailDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(InstantMessagingDetailDialog.class);

	private static final Pattern EMAIL_PATTERN = Pattern.compile("\\S+@\\S+\\.\\S+");

	private SwtManager swtManager;
	private String username;
	private String password;
	private String target;

	private Shell dialogShell;
	private Text usernameText;
	private Text passwordText;
	private Text targetText;
	private Button testButton;
	private Button okButton;
	private Button cancelButton;

	public InstantMessagingDetailDialog(SwtManager swtManager, String username, String password, String target) {
		super(swtManager.getShell(), SWT.APPLICATION_MODAL);
		this.swtManager = swtManager;
		this.username = username;
		this.password = password;
		this.target = target;
	}

	public void open(SubmitListener listener) {
		try {
			DialogListener dialogListener = new DialogListener(listener);

			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialogShell.setText("Instant Messaging Details");
			dialogShell.addListener(SWT.Close, dialogListener);

			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			dialogShell.setLayout(layout);

			Label usernameLabel = new Label(dialogShell, SWT.NONE);
			GridData usernameLabelLData = new GridData();
			usernameLabel.setLayoutData(usernameLabelLData);
			usernameLabel.setText("Username:");

			usernameText = new Text(dialogShell, SWT.BORDER | SWT.SINGLE);
			GridData usernameTextLData = new GridData();
			usernameTextLData.grabExcessHorizontalSpace = true;
			usernameTextLData.horizontalAlignment = SWT.FILL;
			usernameText.setLayoutData(usernameTextLData);
			usernameText.setMessage("Username that will be used to login");
			usernameText.addListener(SWT.DefaultSelection, dialogListener);

			Label passwordLabel = new Label(dialogShell, SWT.NONE);
			GridData passwordLabelLData = new GridData();
			passwordLabel.setLayoutData(passwordLabelLData);
			passwordLabel.setText("Password:");

			passwordText = new Text(dialogShell, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
			GridData passwordTextLData = new GridData();
			passwordTextLData.grabExcessHorizontalSpace = true;
			passwordTextLData.horizontalAlignment = SWT.FILL;
			passwordText.setLayoutData(passwordTextLData);
			passwordText.setMessage("Password that will be used to login");
			passwordText.addListener(SWT.DefaultSelection, dialogListener);

			Label targetLabel = new Label(dialogShell, SWT.NONE);
			GridData targetLabelLData = new GridData();
			targetLabel.setLayoutData(targetLabelLData);
			targetLabel.setText("Send to:");

			targetText = new Text(dialogShell, SWT.BORDER | SWT.SINGLE);
			GridData targetTextLData = new GridData();
			targetTextLData.grabExcessHorizontalSpace = true;
			targetTextLData.horizontalAlignment = SWT.FILL;
			targetText.setLayoutData(targetTextLData);
			targetText.setMessage("Username that will receive notifications");
			targetText.addListener(SWT.DefaultSelection, dialogListener);

			swtManager.update(new Runnable() {
				@Override
				public void run() {
					usernameText.setText(Strings.nullToEmpty(username));
					passwordText.setText(Strings.nullToEmpty(password));
					targetText.setText(Strings.nullToEmpty(target));
				}
			});

			testButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			GridData testButtonLData = new GridData(70, 25);
			testButton.setLayoutData(testButtonLData);
			testButton.setText("Test");
			testButton.addListener(SWT.Selection, dialogListener);

			Composite buttonsComposite = new Composite(dialogShell, SWT.NONE);
			GridData buttonsCompositeLData = new GridData();
			buttonsCompositeLData.horizontalAlignment = SWT.RIGHT;
			buttonsComposite.setLayoutData(buttonsCompositeLData);
			RowLayout buttonsCompositeLayout = new RowLayout();
			buttonsComposite.setLayout(buttonsCompositeLayout);

			if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.WINDOWS) {
				okButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData okButtonLData = new RowData(70, 25);
				okButton.setLayoutData(okButtonLData);
				okButton.setText("OK");
				okButton.addListener(SWT.Selection, dialogListener);
	
				cancelButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData cancelButtonLData = new RowData(70, 25);
				cancelButton.setLayoutData(cancelButtonLData);
				cancelButton.setText("Cancel");
				cancelButton.addListener(SWT.Selection, dialogListener);
			} else {
				cancelButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData cancelButtonLData = new RowData(70, 25);
				cancelButton.setLayoutData(cancelButtonLData);
				cancelButton.setText("Cancel");
				cancelButton.addListener(SWT.Selection, dialogListener);

				okButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData okButtonLData = new RowData(70, 25);
				okButton.setLayoutData(okButtonLData);
				okButton.setText("OK");
				okButton.addListener(SWT.Selection, dialogListener);
			}

			dialogShell.layout();
			dialogShell.pack();

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
		} catch (Exception e) {
			logger.error("Error showing IM detail dialog", e);
		}
	}

	public void close() {
		dialogShell.close();
	}

	public void testSuccessful() {
		testFinished();
		Dialogs.showInfo(swtManager, "Live Messenger Test", "Logged into Windows Live Messaging successfully.", false);
	}

	public void testFailed() {
		testFinished();
		Dialogs.showInfo(swtManager, "Live Messenger Test", "Could not log into Windows Live Messaging, please check username and password.", false);
	}

	protected void prepareForTesting() {
		usernameText.setEditable(false);
		passwordText.setEditable(false);
		targetText.setEditable(false);
		testButton.setText("Testing");
		testButton.setEnabled(false);
		okButton.setEnabled(false);
	}

	protected void testFinished() {
		usernameText.setEditable(true);
		passwordText.setEditable(true);
		targetText.setEditable(true);
		testButton.setText("Test");
		testButton.setEnabled(true);
		okButton.setEnabled(true);
	}

	protected boolean validate() {
		String usernameValue = usernameText.getText();
		String passwordValue = passwordText.getText();
		String targetValue = targetText.getText();

		if (usernameValue.trim().isEmpty()) {
			Dialogs.showError(swtManager, "Username required", "Username is required.", false);
			return false;
		} else if (passwordValue.trim().isEmpty()) {
			Dialogs.showError(swtManager, "Password required", "Password is required.", false);
			return false;
		} else if (targetValue.trim().isEmpty()) {
			Dialogs.showError(swtManager, "'Send to' username required", "'Send to' username is required.", false);
			return false;
		}

		if (!EMAIL_PATTERN.matcher(usernameValue).matches()) {
			Dialogs.showError(swtManager, "Invalid username", "Username must be a valid e-mail address.", false);
			return false;
		} else if (!EMAIL_PATTERN.matcher(targetValue).matches()) {
			Dialogs.showError(swtManager, "Invalid 'Send to' address", "'Send to' must be a valid e-mail address.", false);
			return false;
		}

		return true;
	}

	public interface SubmitListener {
		void onTest(String username, String password, String target);
		void onSubmit(String username, String password, String target);
		void onCancel();
	}

	class DialogListener implements Listener {
		private final SubmitListener listener;
		private boolean requestedClose;

		public DialogListener(SubmitListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleEvent(Event event) {
			if (event.widget == cancelButton || event.widget == dialogShell) {
				if (!requestedClose) {
					requestedClose = true;
					try {
						listener.onCancel();
					} finally {
						close();
					}
				}
			} else if (validate()) {
				String usernameValue = usernameText.getText();
				String passwordValue = passwordText.getText();
				String targetValue = targetText.getText();

				if (event.widget == testButton) {
					prepareForTesting();
					listener.onTest(usernameValue, passwordValue, targetValue);
				} else {
					try {
						listener.onSubmit(usernameValue, passwordValue, targetValue);
					} finally {
						close();
					}
				}
			}
		}
	}

}
