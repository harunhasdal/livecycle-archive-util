package com.github.harunhasdal;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LCAGenerator implements AppInfoNameSpaceConsumer, LCAStructureConsumer
{
	public static final String DEFAULT_IMPLEMENTATION_VERSION = "9.0";
	public static final String DEFAULT_RESOURCE_REVISION = "1.0";
	public static final int FILE_BUFFER = 2048;

	private File baseDirectory;
	private boolean patchArchive;
	private boolean single;

	private FilenameFilter applicationFilter;
	private FilenameFilter applicationVersionFilter;
	private FilenameFilter lcaFilter;

	public LCAGenerator(File baseDirectory) {
		this(baseDirectory, false, false);
	}

	public LCAGenerator(File baseDirectory, boolean single) {
		this(baseDirectory, single, false);
	}

	public LCAGenerator(File baseDirectory, boolean single, boolean patchArchive) {
		if(baseDirectory == null || !baseDirectory.exists() || !baseDirectory.isDirectory()){
			throw new IllegalArgumentException("A valid base directory should be provided.");
		}
		this.baseDirectory = baseDirectory;
		this.patchArchive = patchArchive;
		this.single = single;
	}

	public Document generateArchiveInfo(String description, String creator) {
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(LCA_INFO, NAMESPACE_URI);

		generateApplicationElements(root);

		root.addElement(TYPE, NS).addText(single?LCA_TYPE.SIMPLE:LCA_TYPE.MULTIPLE);
		root.addElement(DESCRIPTION, NS).addText(description);
		root.addElement(CREATED_BY, NS).addText(creator);
		root.addElement(CREATED_DATE, NS).addText(TIMESTAMP_FORMAT.format(new Date()));

		return doc;
	}

	private void generateApplicationElements(Element root) {
		File[] applicationDirs = baseDirectory.listFiles(getApplicationFilter());
		for(File applicationDir : applicationDirs){
			File[] versionDirs = applicationDir.listFiles(getApplicationVersionFilter());
			for(File versionDir : versionDirs){
				String [] versions = versionDir.getName().split("\\.");
				Element applicationElement = root.addElement(APPLICATION_INFO);
				applicationElement.addElement(ACTION, NS).addText(patchArchive? ACTION_TYPE.UPDATE: ACTION_TYPE.CREATE);
				applicationElement.addElement(NAME, NS).addText(applicationDir.getName());
				applicationElement.addElement(TYPE, NS).addText(patchArchive? APPLICATION_TYPE.PATCH: APPLICATION_TYPE.WHOLE);
				applicationElement.addElement(IMPLEMENTATION_VERSION, NS).addText(DEFAULT_IMPLEMENTATION_VERSION);
				applicationElement.addElement(MAJOR_VERSION, NS).addText(versions[0]);
				applicationElement.addElement(MINOR_VERSION, NS).addText(versions[1]);
				applicationElement.addElement(CREATED_DATE, NS).addText(TIMESTAMP_FORMAT.format(new Date()));
				applicationElement.addElement(DESCRIPTION, NS).addText(applicationDir.getName() + " - " + versionDir.getName());

				generateTopLevelObjects(versionDir, applicationElement);
			}
		}
	}

	private void generateTopLevelObjects(File versionDir, Element applicationElement) {
		traverseVersionDirectory(versionDir, versionDir, applicationElement);
	}

	private void traverseVersionDirectory(File currentDir, File versionDir, Element applicationElement) {
		File [] children = currentDir.listFiles();
		for(File item: children){
			if(item.isDirectory())
				traverseVersionDirectory(item, versionDir, applicationElement);
			else {
				if(item.getName().endsWith(DEPENDENCY_EXTENSION) || item.getName().endsWith(DCI_EXTENSION))
					continue;
				else{
					Element tlo = applicationElement.addElement(TOP_LEVEL_OBJECT, NS);
					tlo.addElement(ACTION).addText(ACTION_TYPE.CREATE);
					String itemName = item.getPath().substring(versionDir.getPath().length() + 1);
					tlo.addElement(NAME).addText(itemName);
					String extension = item.getName().substring(item.getName().lastIndexOf(".") + 1);
					tlo.addElement(TYPE).addText(extension);
					tlo.addElement(REVISION).addText(DEFAULT_RESOURCE_REVISION);
					tlo.addElement(DESCRIPTION).addText("");
					addTopLevelObjectReferences(item, versionDir, tlo);
				}
			}
		}
	}

	private void addTopLevelObjectReferences(File item, File versionDir, Element tlo) {
		String itemName = item.getPath().substring(versionDir.getPath().length() + 1);
		String extension = item.getName().substring(item.getName().lastIndexOf(".") + 1);
		if(extension.equalsIgnoreCase(PROCESS_TYPE)){
			File dependency = new File(item.getPath() + DEPENDENCY_EXTENSION);
			if(dependency.exists()){
				Element so = tlo.addElement(SECONDARY_OBJECT);
				so.addElement(NAME).addText(itemName + DEPENDENCY_EXTENSION);
				so.addElement(TYPE).addText(extension + DEPENDENCY_EXTENSION);
			}
		} else if(extension.equalsIgnoreCase(XDP_TYPE) || extension.equalsIgnoreCase(PDF_TYPE)){
			File dependency = new File(item.getPath() + DCI_EXTENSION);
			if(dependency.exists()){
				Element so = tlo.addElement(SECONDARY_OBJECT);
				so.addElement(NAME).addText(itemName + DCI_EXTENSION);
				so.addElement(TYPE).addText(extension + DCI_EXTENSION);
			}
		}
		tlo.addElement(PROPERTIES).addText("");
	}

	protected FilenameFilter getApplicationFilter() {
		if(applicationFilter == null){
			applicationFilter = new FilenameFilter() {
				@Override
				public boolean accept(File file, String s) {
					if (s.startsWith(".") || !file.isDirectory()) {
						return false;
					}
					return true;
				}
			};
		}
		return applicationFilter;
	}

	protected FilenameFilter getApplicationVersionFilter() {
		if(applicationVersionFilter == null){
			applicationVersionFilter = new FilenameFilter() {
				@Override
				public boolean accept(File file, String s) {
					if (file.isDirectory() && s.matches("[1-9][0-9]*.[0-9]+")) {
						return true;
					}
					return false;
				}
			};
		}
		return applicationVersionFilter;
	}

	protected FilenameFilter getLCAFilter() {
		if(lcaFilter == null){
			lcaFilter = new FilenameFilter() {
				@Override
				public boolean accept(File file, String s) {
					if (s.startsWith(".")) {
						return false;
					}
					return true;
				}
			};
		}
		return lcaFilter;
	}

	public byte[] generateLCA(String description, String creator) throws IOException {
		ByteArrayOutputStream appInfoStream = new ByteArrayOutputStream();
		XMLWriter xmlWriter = new XMLWriter(appInfoStream);
		xmlWriter.write( generateArchiveInfo(description, creator) );
		xmlWriter.close();

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
		zipTraverse(baseDirectory, baseDirectory, zipOutputStream);
		zipOutputStream.putNextEntry(new ZipEntry(APP_INFO_FILE_NAME));
		zipOutputStream.write(appInfoStream.toByteArray());
		zipOutputStream.flush();
		zipOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	private void zipTraverse(File directory, File baseDirectory, ZipOutputStream zipOutputStream) throws IOException {
		File[] children = directory.listFiles(getApplicationFilter());
		for(File item : children){
			if(item.isDirectory())
				zipTraverse(item, baseDirectory, zipOutputStream);
			else {
				zipOutputStream.putNextEntry(new ZipEntry(item.getPath().substring(baseDirectory.getPath().length() + 1)));
				InputStream inputStream = new FileInputStream(item);
				int count;
				byte data[] = new byte[FILE_BUFFER];
				while ((count = inputStream.read(data, 0, FILE_BUFFER)) != -1) {
					zipOutputStream.write(data, 0, count);
				}
				zipOutputStream.closeEntry();
			}
		}
	}
}
