package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.nightlabs.jfire.security.Authority;
import org.nightlabs.progress.ProgressMonitor;

public interface InheritedAuthorityResolver {
	Authority getInheritedAuthority(ProgressMonitor monitor);
}
