package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.NLLocale;

public class AuthorityTable extends AbstractTableComposite<Authority>
{
	private static class LabelProvider extends TableLabelProvider
	{
		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return ((Authority)element).getName().getText();
				case 1:
					return ((Authority)element).getDescription().getText();
				default:
					return "";
			}
		}
	}

	private AuthorityTypeID authorityTypeID;
	private Collection<Authority> authorities = new ArrayList<Authority>();

	public AuthorityTable(Composite parent) {
		super(parent, SWT.NONE);

		setInput(authorities);
	}

	public void setAuthorityTypeID(final AuthorityTypeID authorityTypeID) {
		this.authorityTypeID = authorityTypeID;

		authorities.clear();
		if (authorityTypeID != null) {
			AuthorityType dummyAT = new AuthorityType("dummy");
			Authority dummy = new Authority("dummy", "dummy", dummyAT);
			dummy.getName().setText(NLLocale.getDefault().getLanguage(), "Loading...");
			authorities.add(dummy);

			Job loadJob = new Job("Loading authorities") {
				@Override
				protected IStatus run(ProgressMonitor monitor) throws Exception
				{
					final Collection<Authority> newAuthorities = AuthorityDAO.sharedInstance().getAuthorities(
							authorityTypeID,
							new String[] {
									javax.jdo.FetchPlan.DEFAULT,
									Authority.FETCH_GROUP_NAME,
									Authority.FETCH_GROUP_DESCRIPTION
							},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							monitor
					);

					final Job thisJob = this;
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (isDisposed())
								return;

							if (thisJob != currentLoadJob)
								return;

							authorities.clear();
							authorities.addAll(newAuthorities);
							refresh();
						}
					});

					return Status.OK_STATUS;
				}
			};
			currentLoadJob = loadJob;
			loadJob.setPriority(Job.SHORT);
			loadJob.schedule();
		}
		refresh();
	}

	public AuthorityTypeID getAuthorityTypeID() {
		return authorityTypeID;
	}

	private Job currentLoadJob;

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
//		TableLayout tl = new TableLayout();
		TableColumn tc;

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Name");
//		tl.addColumnData(new ColumnWeightData(30));

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Description");
//		tl.addColumnData(new ColumnWeightData(70));

//		table.setLayout(tl);
		table.setLayout(new WeightedTableLayout(new int[] { 30, 70 }));
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
	}
}
