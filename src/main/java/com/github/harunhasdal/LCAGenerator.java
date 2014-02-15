package com.github.harunhasdal;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

public class LCAGenerator implements AppInfoNameSpaceConsumer
{
	public static final String DEFAULT_IMPLEMENTATION_VERSION = "9.0";

	private File baseDirectory;
	private boolean patchArchive;
	private boolean single;

	private FilenameFilter applicationFilter;
	private FilenameFilter applicationVersionFilter;

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
				if(item.getName().endsWith("_dependency") || item.getName().endsWith("_dci"))
					continue;
				else{
					Element tlo = applicationElement.addElement("top-level-object", NS);
					tlo.addElement(ACTION).addText(ACTION_TYPE.CREATE);
					String itemName = item.getPath().substring(versionDir.getPath().length() + 1);
					tlo.addElement(NAME).addText(itemName);
					String extension = item.getName().substring(item.getName().lastIndexOf(".") + 1);
					tlo.addElement(TYPE).addText(extension);
					tlo.addElement("version").addText("1.0");
					tlo.addElement("description").addText("");
					addTopLevelObjectReferences(item, versionDir, tlo);
				}
			}
		}
	}

	private void addTopLevelObjectReferences(File item, File versionDir, Element tlo) {
		String itemName = item.getPath().substring(versionDir.getPath().length() + 1);
		String extension = item.getName().substring(item.getName().lastIndexOf(".") + 1);
		if(extension.equalsIgnoreCase("process")){
			File dependency = new File(item.getPath() + "_dependency");
			if(dependency.exists()){
				Element so = tlo.addElement("secondary-object");
				so.addElement(NAME).addText(itemName + "_dependency");
				so.addElement(TYPE).addText(extension + "_dependency");
			}
		} else if(extension.equalsIgnoreCase("xdp") || extension.equalsIgnoreCase("pdf")){
			File dependency = new File(item.getPath() + "_dci");
			if(dependency.exists()){
				Element so = tlo.addElement("secondary-object");
				so.addElement(NAME).addText(itemName + "_dci");
				so.addElement(TYPE).addText(extension + "_dci");
			}
		}
		tlo.addElement("properties").addText("");
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

}
