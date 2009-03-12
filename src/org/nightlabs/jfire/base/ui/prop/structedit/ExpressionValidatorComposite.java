/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.base.expression.AndCondition;
import org.nightlabs.jfire.base.expression.Composition;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.expression.Negation;
import org.nightlabs.jfire.base.expression.OrCondition;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.DataFieldExpression;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ExpressionValidatorComposite 
extends XComposite 
implements IExpressionValidatorUI
{	
	class ContentProvider extends TreeContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) 
		{
			if (inputElement instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) inputElement;
				return collection.toArray();
			}
			else if (inputElement instanceof Composition) {
				Composition composition = (Composition) inputElement;
				return composition.getExpressions().toArray();
			}
			return new Object[] {};
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object element) {
			return super.getParent(element);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#hasChildren(java.lang.Object)
		 */
		@Override
		public boolean hasChildren(Object element) 
		{
			if (element instanceof Composition) {
				Composition composition = (Composition) element;
				return !composition.getExpressions().isEmpty();
			}
			return false;
		}
		
	}
	
	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) 
		{
			if (element instanceof Composition) {
				return "Composition";
			}
			else if (element instanceof IExpression) {
				return "Expression";
			}
			else {
				return super.getText(element);	
			}
		}
	}

	public static final String NEGATION = "!";
	public static final String NOT_EMPTY = "NOT EMPTY";
	
	public enum Mode 
	{
		STRUCT_BLOCK,
		STRUCT_FIELD,
		STRUCT
	}
	
	private TreeViewer treeViewer;
	private IExpression expression;
	private I18nTextEditor i18nTextEditor;
	private I18nText message = new I18nTextBuffer();
	private ValidationResultTypeCombo validationResultTypeCombo;
	private ValidationResultType validationResultType;
	private IAddExpressionValidatorHandler addHandler;
	private IExpression selectedExpression;
	private Combo conditionOperatorCombo;
	private Button removeExpression;
	private IStruct struct;
	private Text expressionText;
	private Composite expressionDetailComposite;
	private Mode mode = Mode.STRUCT_BLOCK;
	private Composite buttonComp;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ExpressionValidatorComposite(Composite parent, int style, IExpression expression,
			IStruct struct, IAddExpressionValidatorHandler handler, Mode mode) 
	{	
		super(parent, style);
		this.expression = expression;
		this.struct = struct;
		this.mode = mode;
		this.addHandler = handler;
		addHandler.setExpressionValidatorComposite(this);
		createComposite(this);
	}

	private ISelectionChangedListener treeListener = new ISelectionChangedListener(){
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = treeViewer.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof IExpression) 
				{
					IExpression expression = (IExpression) firstElement;
					selectedExpression = expression;
					showExpression(selectedExpression);
					removeExpression.setEnabled(true);
				}
			}
			else {
				removeExpression.setEnabled(false);
			}
		}
	};
	
	protected void showExpression(IExpression expression) 
	{
		if (expression != null) {
			expressionText.setText(getText(expression));
		}
		createExpressionDetail(expression, this);
		createButtonComposite(expression, this);
		layout(true, true);
	}
	
	protected void createComposite(Composite parent) 
	{		
		i18nTextEditor = new I18nTextEditor(parent);
		message = i18nTextEditor.getI18nText();
		
		validationResultTypeCombo = new ValidationResultTypeCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		validationResultTypeCombo.selectElement(ValidationResultType.ERROR);
		if (validationResultType != null) {
			validationResultTypeCombo.selectElement(validationResultType);
		}
		validationResultType = validationResultTypeCombo.getSelectedElement();
		
		validationResultTypeCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				validationResultType = validationResultTypeCombo.getSelectedElement();
			}
		});		
 
		Composite treeAndTextComposite = new XComposite(parent, SWT.NONE);
		treeAndTextComposite.setLayout(new GridLayout(2, true));
		createTreeViewer(treeAndTextComposite);
		createExpressionText(expression, treeAndTextComposite);
		
		setExpression(expression);
	}

	protected void createExpressionDetail(IExpression expression, Composite parent) 
	{
		if (expressionDetailComposite != null && !expressionDetailComposite.isDisposed()) {
			expressionDetailComposite.dispose();
		}
		expressionDetailComposite = new XComposite(parent, SWT.BORDER);
		createExpressionComposite(expression, expressionDetailComposite);
	}

	protected void createExpressionComposite(IExpression expression, Composite parent) 
	{
		// TODO should come from extension-point for extensibility
		
		if (expression instanceof Composition) {
			createCompositionComposite((Composition)expression, parent);
		}
		else if (expression instanceof DataFieldExpression<?>) {
			createDataFieldExpressionComposite((DataFieldExpression<?>)expression, parent, mode);
		}
		else if (expression instanceof Negation) {
			createNegationCompoiste((Negation)expression, parent);
		}
	}
	
	protected void createDataFieldExpressionComposite(DataFieldExpression<?> expression, Composite parent, Mode mode) 
	{
		DataFieldExpressionComposite comp = new DataFieldExpressionComposite(parent, SWT.NONE, 
				LayoutMode.TIGHT_WRAPPER, expression, mode, struct, true, this);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void createCompositionComposite(final Composition composition, Composite parent) 
	{
		Composite comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Combo compositionKindCombo = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
		compositionKindCombo.setItems(new String[] {AndCondition.OPERATOR_TEXT, OrCondition.OPERATOR_TEXT});
		compositionKindCombo.setText(composition.getOperatorText());
		compositionKindCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String operator = compositionKindCombo.getItem(compositionKindCombo.getSelectionIndex());
				if (!operator.equals(composition.getOperatorText())) 
				{
					Composition newComposition = null;
					if (operator.equals(AndCondition.OPERATOR_TEXT)) {
						newComposition = new AndCondition(composition.getExpressions().toArray(
								new IExpression[composition.getExpressions().size()]));
					}
					else if (operator.equals(OrCondition.OPERATOR_TEXT)) {
						newComposition = new OrCondition(composition.getExpressions().toArray(
								new IExpression[composition.getExpressions().size()]));
					}
					Composition parent = getParentForExpression(expression, composition);
					if (parent != null) {
						parent.replaceExpression(composition, newComposition);
						refresh();
					}
				}
			}
		});
		Label amountLabel = new Label(comp, SWT.NONE);
		amountLabel.setText("Composition Operator");
		Group expressionsComp = new Group(parent, SWT.NONE);
		expressionsComp.setLayout(new GridLayout());
		expressionsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (IExpression expr : composition.getExpressions()) 
		{
			if (expr instanceof Composition) {
				createNestedCompositionComposite((Composition) expr, expressionsComp);
			}
			else {
				createExpressionComposite(expr, expressionsComp);	
			}
		}
	}
	
	protected void createNestedCompositionComposite(Composition composition, Composite parent) 
	{
		ExpandableComposite expandableComposite = new ExpandableComposite(parent, SWT.NONE);
		expandableComposite.setLayout(new GridLayout());
		expandableComposite.setText("Composition");
		expandableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Group comp = new Group(expandableComposite, SWT.NONE);
		comp.setLayout(new GridLayout());
		for (IExpression expr : composition.getExpressions()) {
			createExpressionComposite(expr, comp);
		}
		expandableComposite.setClient(comp);
		expandableComposite.addExpansionListener(new IExpansionListener(){
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				
			}
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				layout(true, true);
			}
		});
	}
	
	protected void createNegationCompoiste(final Negation negation, Composite parent) 
	{
		Composite comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Combo negationCombo = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
		negationCombo.setItems(new String[] {"", ExpressionValidatorComposite.NEGATION});
		negationCombo.select(1);
		negationCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String negationString = negationCombo.getText();
				if (!negationString.equals(ExpressionValidatorComposite.NEGATION)) {
					Composition parent = getParentForExpression(expression, negation);
					if (parent != null) {
						IExpression expression = negation.getExpression();
						parent.replaceExpression(negation, expression);
						refresh();
					}
				}
			}
		});
		createExpressionComposite(negation.getExpression(), comp);
	}
	
	protected void createTreeViewer(Composite parent) 
	{
		treeViewer = new TreeViewer(parent, getBorderStyle());
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		Tree tree = treeViewer.getTree();
		TreeColumn column = new TreeColumn(tree, SWT.NONE);
		column.setText("Name");
		WeightedTableLayout layout = new WeightedTableLayout(new int[] {1});
		tree.setLayout(layout);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.addSelectionChangedListener(treeListener);		
	}
	
	protected void createButtonComposite(IExpression expression, Composite parent) 
	{
		if (buttonComp != null && !buttonComp.isDisposed()) {
			buttonComp.dispose();
		}
		buttonComp = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, 4);
		buttonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button addExpression = new Button(buttonComp, SWT.NONE);
		addExpression.setText("Add Expression");
		addExpression.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				addExpressionPressed();
			}
		});
		removeExpression = new Button(buttonComp, SWT.NONE);
		removeExpression.setText("Remove Expression");
		removeExpression.setEnabled(false);
		removeExpression.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeExpressionPressed();
			}
		});
		Button addComposition = new Button(buttonComp, SWT.NONE);
		addComposition.setText("Add Composition");
		addComposition.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				addCompositionPressed();
			}
		});
		conditionOperatorCombo = new Combo(buttonComp, SWT.READ_ONLY | SWT.BORDER);
		conditionOperatorCombo.setItems(new String[] {AndCondition.OPERATOR_TEXT, OrCondition.OPERATOR_TEXT});
		conditionOperatorCombo.select(0);
	}
	
	protected void createExpressionText(IExpression expression, Composite parent) 
	{
		expressionText = new Text(parent, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		expressionText.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (expression != null) {
			expressionText.setText(getText(expression));
		}
	}
	
	protected String getText(IExpression expression) 
	{
		// TODO exchange this with extensible LabelProvider extension-point
		if (expression != null) 
		{
			if (expression instanceof Composition) 
			{
				Composition composition = (Composition) expression;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("(");
				for (int i=0; i<composition.getExpressions().size(); i++) {
					IExpression expr = composition.getExpressions().get(i);
					stringBuilder.append(getText(expr));
					if (i < composition.getExpressions().size()-1) {
						stringBuilder.append(" ");
						stringBuilder.append(composition.getOperatorText());
						stringBuilder.append(" ");						
					}
				}
				stringBuilder.append(")");
				return stringBuilder.toString();
			}
			else if (expression instanceof DataFieldExpression<?> && struct != null) {
				DataFieldExpression<?> dataFieldExpression = (DataFieldExpression<?>) expression;
				StructFieldID structFieldID = dataFieldExpression.getStructFieldID();			
				StructField<?> structField;
				StringBuilder stringBuilder = new StringBuilder();
				if (expression != null) {
					try {
						structField = struct.getStructField(structFieldID);
						String name = structField.getName().getText();
						stringBuilder.append(name);
						if (expression instanceof GenericDataFieldNotEmptyExpression) {
							stringBuilder.insert(0, NOT_EMPTY + " ");
						}
						return stringBuilder.toString();
					} catch (Exception e) {
						return expression.toString();	
					}				
				}
			}
			else if (expression instanceof Negation) 
			{
				Negation negation = (Negation) expression;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(NEGATION);
				stringBuilder.append(" ");
				stringBuilder.append(getText(negation.getExpression()));
				return stringBuilder.toString();
			}
			else {
				return expression.toString();	
			}			
		}
		return "";
	}
	
	public void setExpression(IExpression expression) 
	{
		this.expression = expression;		
		if (expression != null) {
			treeViewer.setInput(Collections.singleton(this.expression));
			treeViewer.expandAll();			
		}
		showExpression(expression);
	}

	public void refresh() 
	{
		ISelection oldSelection = treeViewer.getSelection();
		showExpression(expression);
		treeViewer.refresh();
		if (oldSelection != null) {
			treeViewer.setSelection(oldSelection, true);
		}
	}
	
	public IExpression getExpression() {
		return expression;
	}
	
	public IExpression getSelectedExpression() {
		return selectedExpression;
	}
	
	public void setMessage(I18nText message) {
		this.message.copyFrom(message);
		if (i18nTextEditor != null && !i18nTextEditor.isDisposed()) {
			i18nTextEditor.setI18nText(this.message, EditMode.DIRECT);
		}
	}

	public I18nText getMessage() {
		return message;
	}
	
	public void setValidationResultType(ValidationResultType type) {
		this.validationResultType = type;
		if (validationResultTypeCombo != null && !validationResultTypeCombo.isDisposed()) {
			validationResultTypeCombo.selectElement(type);
		}
	}
	
	public ValidationResultType getValidationResultType() {
		return validationResultType;
	}
	
	protected void addExpressionPressed() {
		if (addHandler != null) {
			addHandler.addExpressionPressed();
		}
	}
	
	protected void addCompositionPressed() {
		String selection = conditionOperatorCombo.getItem(conditionOperatorCombo.getSelectionIndex());
		Composition newComposition = null; 
		if (selection.equals(AndCondition.OPERATOR_TEXT)) {
			newComposition = new AndCondition();
		}
		else if (selection.equals(OrCondition.OPERATOR_TEXT)) {
			newComposition = new OrCondition();
		}
		
		if (expression instanceof Composition) {
			Composition root = (Composition) expression;
			root.addExpression(newComposition);
			refresh();
		}
		else if (expression == null) {
			expression = newComposition;
			setExpression(expression);
		}
	}
	
	protected void removeExpressionPressed() {
		if (selectedExpression != null) {
			Composition parent = getParentForExpression(expression, selectedExpression);
			if (parent != null) {
				parent.getExpressions().remove(selectedExpression);
				setExpression(expression);
			}
		}
	}
	
	protected Composition getParentForExpression(IExpression root, IExpression selectedExpression) {
		if (root instanceof Composition) {
			Composition composition = (Composition) root;
			for (IExpression expr : composition.getExpressions()) {
				if (expr.equals(selectedExpression)) {
					return composition;
				}
				Composition parent = getParentForExpression(expr, selectedExpression);
				if (parent != null) {
					return parent;
				}
			}
		}
		return null;
	}
}
