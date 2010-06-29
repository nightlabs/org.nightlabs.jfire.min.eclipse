package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityMeta;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityMetaDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.progress.ProgressMonitor;

public class AuthorityTypeDetailPage extends EntityEditorPageWithProgress
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link AuthorityTypeDetailPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new AuthorityTypeDetailPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new AuthorityTypeDetailPageController(editor);
		}
	}

	private AuthorityTypeSection authorityTypeSection;
	private RoleGroupsSection roleGroupsSection;
	private AuthorityListSection authorityListSection;

	public AuthorityTypeDetailPage(FormEditor editor) {
		super(editor, AuthorityTypeDetailPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityTypeDetailPage.title.general")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		authorityTypeSection = new AuthorityTypeSection(this, parent);
		getManagedForm().addPart(authorityTypeSection);

		roleGroupsSection = new RoleGroupsSection(this, parent);
		getManagedForm().addPart(roleGroupsSection);

		authorityListSection = new AuthorityListSection(this, parent);
		getManagedForm().addPart(authorityListSection);

		authorityListSection.getAuthorityTable().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Authority authority = authorityListSection.getAuthorityTable().getFirstSelectedElement();
				authorityDoubleClicked(authority);
			}
		});
	}

	private AuthorityType authorityType;

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		getManagedForm().getForm().getDisplay().asyncExec(new Runnable() {
			public void run() {
				authorityType = ((AuthorityTypeDetailPageController)getPageController()).getAuthorityType();

				authorityTypeSection.setAuthorityType(authorityType);
				roleGroupsSection.setRoleGroups(authorityType.getRoleGroups());
				authorityListSection.setAuthorityType(authorityType);
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityTypeDetailPage.page.title.general"); //$NON-NLS-1$
	}

	protected void authorityDoubleClicked(final Authority authority)
	{
		Job job = new Job("Load Authority") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				AuthorityID authorityID = (AuthorityID) JDOHelper.getObjectId(authority);
				final AuthorityMeta authorityMeta = AuthorityMetaDAO.sharedInstance().getAuthorityMetaForAuthority(
						authorityID, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				Display display = getEditor().getActivePageInstance().getPartControl().getDisplay();
				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							AuthorityMetaID authorityMetaID = (AuthorityMetaID) JDOHelper.getObjectId(authorityMeta);
							JDOObjectEditorInput<AuthorityMetaID> input = new JDOObjectEditorInput<AuthorityMetaID>(authorityMetaID);
							try {
								RCPUtil.openEditor(input, AuthorityMetaEditor.EDITOR_ID);
							} catch (PartInitException e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
