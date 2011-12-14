package org.nightlabs.jfire.base.dashboard.ui.welcome;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author abieber
 *
 */
public class DashboardGadgetFactoryWelcome extends AbstractDashboardGadgetFactory implements IDashboardGadgetFactory {

	Logger logger = LoggerFactory.getLogger(DashboardGadgetFactoryWelcome.class);
	
	/**
	 * 
	 */
	public DashboardGadgetFactoryWelcome() {
	}

	@Override
	public IDashboardGadgetConfigPage<?> createConfigurationWizardPage() {
		return new ConfigureWelcomeGadgetPage();
	}

	@Override
	public Composite createGadgetControl(Composite parent) {
		XComposite welcomeGadget = new XComposite(parent, SWT.NONE);
		Browser browser = new Browser(welcomeGadget, SWT.NONE);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 400;
		browser.setLayoutData(layoutData);
		
//		String localPathToWelcomePage = this.getClass().getResource("/res/html/welcome.html").toString();
//		URL url = getClass().getClassLoader().getResource("res/html/welcome.html");
//		logger.info("localPathToWelcomePage={}, url={}", localPathToWelcomePage, url);
//		browser.setUrl(localPathToWelcomePage);
	
		String readResourceToBuffer = readResourceToBuffer("res/html/welcome.html");
		browser.setText(readResourceToBuffer);
		
//		welcomeGadget.getGridLayout().numColumns = 2;
//		welcomeGadget.getGridLayout().makeColumnsEqualWidth = false;
//		appendNewRow(welcomeGadget, "icons/JFire-Logo.81x81.png", "Welcome to JFire ...");
//		appendNewRow(welcomeGadget, "icons/JFire-Logo.81x81.png", "Easy to use");
//		appendNewRow(welcomeGadget, "icons/JFire-Logo.81x81.png", "Easy to extend...");
		
		return welcomeGadget;
	}
	
//	private static void appendNewRow(Composite welcomeGadget, String iconPath, String rowText) {
//		Label icon0 = new Label(welcomeGadget, SWT.NONE);
//		icon0.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.nightlabs.jfire.base.dashboard.ui", iconPath).createImage());
//		icon0.setLayoutData(new GridData());
//		Text text = new Text(welcomeGadget, SWT.WRAP);
//		text.setText(rowText);
//		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//	}

	
	private String readResourceToBuffer(String res) {
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(res);
			Scanner scanner = new Scanner(in);
			StringBuilder sb = new StringBuilder();
			while(scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
				sb.append("\n"); //do not loose line breaks!
			}
			return sb.toString();
		} catch (Exception e) {
			logger.warn("Could not read from resource:", e);
			return "";
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}
}
