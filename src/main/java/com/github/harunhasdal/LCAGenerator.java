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

		root.addElement(TYPE).addText(single?LCA_TYPE.SIMPLE:LCA_TYPE.MULTIPLE);
		root.addElement(DESCRIPTION).addText(description);
		root.addElement(CREATED_BY).addText(creator);
		root.addElement(CREATED_DATE).addText(TIMESTAMP_FORMAT.format(new Date()));

		generateApplicationElements(root);

		return doc;
	}

	private void generateApplicationElements(Element root) {
		File[] applicationDirs = baseDirectory.listFiles(getApplicationFilter());
		for(File applicationDir : applicationDirs){
			File[] versionDirs = applicationDir.listFiles(getApplicationVersionFilter());
			for(File versionDir : versionDirs){
				String [] versions = versionDir.getName().split("\\.");
				Element applicationElement = root.addElement(APPLICATION_INFO);
				applicationElement.addElement(ACTION).addText(patchArchive? ACTION_TYPE.UPDATE: ACTION_TYPE.CREATE);
				applicationElement.addElement(NAME).addText(applicationDir.getName());
				applicationElement.addElement(TYPE).addText(patchArchive? APPLICATION_TYPE.PATCH: APPLICATION_TYPE.WHOLE);
				applicationElement.addElement(IMPLEMENTATION_VERSION).addText(DEFAULT_IMPLEMENTATION_VERSION);
				applicationElement.addElement(MAJOR_VERSION).addText(versions[0]);
				applicationElement.addElement(MINOR_VERSION).addText(versions[1]);
				applicationElement.addElement(DESCRIPTION).addText(applicationDir.getName() + " - " + versionDir.getName());
				applicationElement.addElement(CREATED_DATE).addText(TIMESTAMP_FORMAT.format(new Date()));
			}
		}
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
