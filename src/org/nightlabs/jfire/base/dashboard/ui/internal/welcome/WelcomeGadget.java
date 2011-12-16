/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.welcome;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;

/**
 * @author sschefczyk
 *
 */
public class WelcomeGadget extends ViewPart {

	private FormToolkit toolkit;
	private Form form;


	public WelcomeGadget() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setText("JFire v1.3 is the most easiest, native to use use ERP/CRM software ever seen in the JFire universe!");
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);
		Hyperlink linkJDireNews = toolkit.createHyperlink(form.getBody(), "Get the latest JFire news.", SWT.WRAP);
		linkJDireNews.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});
		Hyperlink linkJFireDevNews = toolkit.createHyperlink(form.getBody(), "Get the latest JFire developer news.", SWT.WRAP);
		linkJFireDevNews.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});
		
//		XComposite.configureLayout(LayoutMode.LEFT_RIGHT_WRAPPER, layout);
//		layout.numColumns = 2;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}

	/**
	 * Disposes the toolkit
	 */
	@Override
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

}
