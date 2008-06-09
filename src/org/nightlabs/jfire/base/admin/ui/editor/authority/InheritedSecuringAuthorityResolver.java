package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * This interface defines an object that is able to find out the parent's <code>securingAuthorityID</code>, if the
 * {@link SecuredObject} managed by the authority-page (see {@link AbstractAuthorityPage}) supports data inheritance
 * (see {@link InheritanceManager}).
 * <p>
 * For details about how to use this, please read
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/UI%20for%20editing%20the%20Authority%20of%20a%20SecuredObject">UI for editing the Authority of a SecuredObject</a>
 * in our wiki.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface InheritedSecuringAuthorityResolver {
	AuthorityID getInheritedSecuringAuthorityID(ProgressMonitor monitor);
}
