package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.NLLocale;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ImageDataFieldEditor
extends AbstractDataFieldEditor<ImageDataField>
{
	/**
	 * Use this before extension.
	 */
	private static final String EXTENSION_PREFIX = "*.";

	/**
	 * Separate extension in the file dialog using this string.
	 */
	private static final String EXTENSION_SEPARATOR = ";";

	public static class Factory extends AbstractDataFieldEditorFactory<ImageDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

		@Override
		public Class<? extends DataFieldEditor<ImageDataField>> getDataFieldEditorClass() {
			return ImageDataFieldEditor.class;
		}

		@Override
		public Class<ImageDataField> getPropDataFieldType() {
			return ImageDataField.class;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(ImageDataFieldEditor.class);
	
	private LanguageCf language;
	
	private Text filenameTextbox;
	private Button openFileChooserButton;
	private Button clearButton;
	private Group group;
	private Label imageLabel;
	private Label sizeLabel;
	private String fileDialogFilterPath;
	
	private static final int maxThumbnailWidth = 200;
	private static final int maxThumbnailHeight = 200;
	
	public ImageDataFieldEditor() {
		super();
		language = new LanguageCf(NLLocale.getDefault().getLanguage());
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#setDataField(org.nightlabs.jfire.prop.DataField)
	 */
	@Override
	protected void setDataField(ImageDataField dataField) {
		super.setDataField(dataField);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(final Composite parent) {
		group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(4, false));
		
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, group);
				
		GridData gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.LEFT;
		
		filenameTextbox = new Text(group, XComposite.getBorderStyle(parent));
		filenameTextbox.setEditable(false);
		filenameTextbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		openFileChooserButton = new Button(group, SWT.PUSH);
		openFileChooserButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.openFileChooserButton.text")); //$NON-NLS-1$
		openFileChooserButton.setToolTipText("Browse for image");
		openFileChooserButton.setLayoutData(new GridData());
		openFileChooserButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fileChooserButtonPressed();
			}
		});
		
		clearButton = new Button(group, SWT.PUSH);
		clearButton.setText("&Clear");
		clearButton.setToolTipText("Remove the image");
		clearButton.setLayoutData(new GridData());
		clearButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				clearButtonPressed();
			}
		});
		clearButton.setEnabled(false);
		
		sizeLabel = new Label(group, SWT.NONE);
		
		imageLabel = new Label(group, SWT.NONE);
		imageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel.getImage() != null)
					imageLabel.getImage().dispose();
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
//		gd.verticalIndent = 10;
		gd.heightHint = 0;
		imageLabel.setLayoutData(gd);
		
		return group;
	}
	
	private void displayImage(ImageData id) {
		if (imageLabel.getImage() != null)
			imageLabel.getImage().dispose();

		GridData imageLabelGD = ((GridData)imageLabel.getLayoutData());
		if(id != null) {
			int width = id.width;
			int height = id.height;
			double factor = 1.0;
			if (width > maxThumbnailWidth || height > maxThumbnailHeight)
				factor *= height > width ? 1.0*maxThumbnailHeight/height : 1.0*maxThumbnailHeight/width;
				
			id = id.scaledTo((int) (factor*width), (int) (factor*height));
			Image image = new Image(imageLabel.getDisplay(), id);
			imageLabel.setImage(image);
			imageLabelGD.heightHint = SWT.DEFAULT;
			clearButton.setEnabled(true);
		} else {
			imageLabel.setImage(null);
			imageLabelGD.heightHint = 0;
			clearButton.setEnabled(false);
		}
		
		// re-layout the top level container
		Composite top = imageLabel.getParent();
		while (top.getParent() != null)
			top = top.getParent();
		top.layout(true, true);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		ImageStructField imageStructField = (ImageStructField) getStructField();
		
		group.setText(imageStructField.getName().getText(language.getLanguageID()));
		
		if (!getDataField().isEmpty()) {
			filenameTextbox.setText(getDataField().getFileName());
			ImageData id = null;
			InputStream in = new InflaterInputStream(new ByteArrayInputStream(getDataField().getContent()));
			try {
				id = new ImageData(in);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						LOGGER.error(e);
					}
			}
			displayImage(id);
		}

		sizeLabel.setText(
				String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.sizeMaxKBLabel"), //$NON-NLS-1$
						new Object[] { new Long(imageStructField.getMaxSizeKB()) }));
		sizeLabel.pack();
		sizeLabel.getParent().layout(true, true);
	}

	/**
	 * Open the image file browse dialog.
	 * @param parent The parent shell
	 * @return the selected image file name or <code>null</code> if 
	 * 		no image file was selected
	 */
	private String openImageFileDialog(Shell parent)
	{
		ImageStructField imageStructField = (ImageStructField) getStructField();
		List<String> extList = imageStructField.getImageFormats();
		
		String[] extensions = new String[extList.size()+1];
		String[] names = new String[extList.size()+1];
		names[0] = "Image Files";
		int i = 1;
		for (String ext : extList) {
			String extension = EXTENSION_PREFIX + ext.toLowerCase() + EXTENSION_SEPARATOR + EXTENSION_PREFIX + ext.toUpperCase();
			if(extensions[0] == null)
				extensions[0] = extension;
			else
				extensions[0] += EXTENSION_SEPARATOR + extension;
			extensions[i] = extension;
			names[i] = String.format("%s Images", ext.toUpperCase());
			i++;
		}
		
		FileDialog fileDialog = new FileDialog(parent);
		fileDialog.setText("Choose an Image File");
		fileDialog.setFilterNames(names);
		fileDialog.setFilterExtensions(extensions);
		fileDialog.setFilterPath(fileDialogFilterPath);
		String filename = fileDialog.open();
		if(filename != null)
			fileDialogFilterPath = filename;
		return filename;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	@Override
	public Control getControl() {
		return group;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		if (!isChanged())
			return;

		ImageDataField dataField = getDataField();
		String path = filenameTextbox.getText();
		if (path == null || path.isEmpty()) {
			dataField.clear();
		} else {
			//FIXME: get content type somehow!
			final String contentType;
			final String lowerPath = path.toLowerCase();
			if(lowerPath.endsWith(".png")) //$NON-NLS-1$
				contentType = "image/png"; //$NON-NLS-1$
			else if(lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) //$NON-NLS-1$ //$NON-NLS-2$
				contentType = "image/jpeg"; //$NON-NLS-1$
			else if(lowerPath.endsWith(".gif")) //$NON-NLS-1$
				contentType = "image/gif"; //$NON-NLS-1$
			else
				contentType = "application/unknown"; //$NON-NLS-1$

			// store the image as in the data field.
			final File imageFile = new File(path);

			try {
				dataField.loadFile(imageFile, contentType);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	public LanguageCf getLanguage() {
		return language;
	}

	/**
	 * Called when the file chooser button was pressed.
	 */
	private void fileChooserButtonPressed() {
		String filename = openImageFileDialog(openFileChooserButton.getShell());
		if (filename != null) {
			File file = new File(filename);
			// check if the image fulfills the size requirements
			ImageStructField imageStructField = (ImageStructField) getStructField();
			if (!imageStructField.validateSize(file.length()/1024)) {
				MessageDialog.openError(
						openFileChooserButton.getShell(), 
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.text"), //$NON-NLS-1$
						String.format(
								Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.message"), //$NON-NLS-1$
								new Object[] { new Long(imageStructField.getMaxSizeKB()), new Long((file.length() / 1024))})
				);
				return;
			}
			
			try {
				ImageData data = new ImageData(filename);
				filenameTextbox.setText(filename);
				setChanged(true);
				displayImage(data);
				// there is already layout code in displayImage()... I moved this top-level layout stuff to this method, too. Marc
//						Composite top = parent;
//						while (top.getParent() != null)
//							top = top.getParent();
//						top.layout(true, true); // this is necessary, because otherwise a bigger image doesn't cause the widgets to grow and is therefore cut
			} catch(SWTException swtex) {
				MessageDialog.openError(
						openFileChooserButton.getShell(),
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.text"), //$NON-NLS-1$
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.message") //$NON-NLS-1$
				);
			}
		}
	}
	
	/**
	 * Called when the clear button was pressed.
	 */
	private void clearButtonPressed()
	{
		filenameTextbox.setText("");
		setChanged(true);
		displayImage(null);
	}
}


