package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.progress.ProgressMonitor;

public interface InheritedAuthorityResolver {
	AuthorityID getInheritedAuthorityID(ProgressMonitor monitor);
}
