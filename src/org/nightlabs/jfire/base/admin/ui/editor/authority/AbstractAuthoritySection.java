package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecuredObject;

/**
 * Abstract super class for the easy implementation of an {@link Authority}-section within an Authority-editor-page.
 * <p>
 * When implementing an editor-page for editing the <code>Authority</code> (or its assignment to a {@link SecuredObject}),
 * you are encouraged to subclass this section as first section within your page. Simply implement the abstract method
 * {@link #createInheritedAuthorityResolver()} and call
 * {@link #setAuthorityPageControllerHelper(AuthorityPageControllerHelper)} whenever
 * the <code>SecuredObject</code> you are managing is changed.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public abstract class AbstractAuthoritySection
extends ToolBarSectionPart
{
	private I18nTextEditor name;
	private I18nTextEditor description;

	public AbstractAuthoritySection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE, "Authority");
		((GridData)getSection().getLayoutData()).grabExcessVerticalSpace = false;

		name = new I18nTextEditor(getContainer());
		name.addModifyListener(markDirtyModifyListener);
		description = new I18nTextEditorMultiLine(getContainer());
		description.addModifyListener(markDirtyModifyListener);

		assignAuthorityAction.setEnabled(false);
		getToolBarManager().add(assignAuthorityAction);
		updateToolBarManager();

		name.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				setAuthorityPageControllerHelper(null);
			}
		});

		setEnabled(false);
	}

	private ModifyListener markDirtyModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent arg0) {
			markDirty();
		}
	};

	private AuthorityPageControllerHelper authorityPageControllerHelper;

	/**
	 * Get the object that has been set by {@link #setAuthorityPageControllerHelper(AuthorityPageControllerHelper)} before or <code>null</code>.
	 *
	 * @return an instance of <code>AuthorityPageControllerHelper</code> or <code>null</code>.
	 */
	protected AuthorityPageControllerHelper getAuthorityPageControllerHelper() {
		return authorityPageControllerHelper;
	}

	/**
	 * Get the {@link InheritedAuthorityResolver} which is used to find out the {@link Authority} that is assigned to the
	 * parent-{@link SecuredObject} of that <code>SecuredObject</code> that is edited by the current editor. This method
	 * can return <code>null</code> if there is no inheritance mechanism implemented
	 * for the <code>SecuredObject</code> in the concrete use case.
	 *
	 * @return an instance of <code>InheritedAuthorityResolver</code> or <code>null</code>, if there is no inheritance mechanism.
	 */
	protected abstract InheritedAuthorityResolver createInheritedAuthorityResolver();

	private Action assignAuthorityAction = new Action() {
		{
			setText("Assign authority");
		}

		@Override
		public void run() {
			if (authorityPageControllerHelper == null)
				return;

			final AssignAuthorityWizard assignAuthorityWizard = new AssignAuthorityWizard(
					authorityPageControllerHelper.getAuthorityTypeID(),
					createInheritedAuthorityResolver()
			);
			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(getSection().getShell(), assignAuthorityWizard);
			if (dialog.open() == Dialog.OK) {
				Job job = new Job("Loading authority") {
					protected org.eclipse.core.runtime.IStatus run(org.nightlabs.progress.ProgressMonitor monitor) throws Exception {
						authorityPageControllerHelper.load(
								assignAuthorityWizard.getAuthorityTypeID(),
								assignAuthorityWizard.getAuthorityID(),
								assignAuthorityWizard.getNewAuthority(),
								monitor);

						getSection().getDisplay().asyncExec(new Runnable() {
							public void run() {
								authorityChanged();
								markDirty();
							}
						});

						return Status.OK_STATUS;
					}
				};
				job.setPriority(Job.SHORT);
				job.schedule();
			}
		}
	};

	/**
	 * Set the {@link AuthorityPageControllerHelper} that is used for the current editor page. It is possible to
	 * pass <code>null</code> in order to indicate that there is nothing to be managed right now (and thus to clear
	 * the UI).
	 *
	 * @param authorityPageControllerHelper an instance of <code>AuthorityPageControllerHelper</code> or <code>null</code>.
	 */
	protected void setAuthorityPageControllerHelper(AuthorityPageControllerHelper authorityPageControllerHelper) {
		this.authorityPageControllerHelper = authorityPageControllerHelper;
		assignAuthorityAction.setEnabled(authorityPageControllerHelper != null);
		getSection().getDisplay().asyncExec(new Runnable() {
			public void run() {
				authorityChanged();
			}
		});
	}

	private void authorityChanged() {
		if (name.isDisposed())
			return;

		if (authorityPageControllerHelper == null || authorityPageControllerHelper.getAuthority() == null) {
			name.setI18nText(null, EditMode.DIRECT);
			description.setI18nText(null, EditMode.DIRECT);

			if (authorityPageControllerHelper == null)
				setMessage("There is no secured object selected at the moment.");
			else
				setMessage("There is no authority assigned to this product type.");

			setEnabled(false);
		}
		else {
			name.setI18nText(authorityPageControllerHelper.getAuthority().getName(), EditMode.DIRECT);
			description.setI18nText(authorityPageControllerHelper.getAuthority().getDescription(), EditMode.DIRECT);

			setMessage(null);
			setEnabled(true);
		}
	}

	private void setEnabled(boolean enabled) {
		name.setEnabled(enabled);
		description.setEnabled(enabled);
	}
}
