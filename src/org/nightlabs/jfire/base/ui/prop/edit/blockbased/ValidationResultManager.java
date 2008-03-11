package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.List;

import org.nightlabs.jfire.prop.validation.ValidationResult;

public abstract class ValidationResultManager implements IValidationResultManager
{

	@Override
	public void setValidationResults(List<ValidationResult> validationResults) {
		if (validationResults == null || validationResults.isEmpty()) {
			setValidationResult(null);
			return;
		}

		ValidationResult firstResult = validationResults.get(0);
		setValidationResult(firstResult);
	}

	public abstract void setValidationResult(ValidationResult validationResult);
}
