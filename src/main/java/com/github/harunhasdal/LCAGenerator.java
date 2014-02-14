package com.github.harunhasdal;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

public class LCAGenerator implements AppInfoNameSpaceConsumer
{
	private File baseDirectory;
	private boolean patchArchive;
	private boolean single;

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


		FilenameFilter applicationFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if(s.startsWith(".") || !file.isDirectory()){
					return false;
				}
				return true;
			}
		};

		FilenameFilter applicationVersionFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if(file.isDirectory() && s.matches("[1-9][0-9]*.[0-9]+")){
					return true;
				}
				return false;
			}
		};

		File[] applicationDirs = baseDirectory.listFiles(applicationFilter);
		for(File applicationDir : applicationDirs){
			File[] versionDirs = applicationDir.listFiles(applicationVersionFilter);
			for(File versionDir : versionDirs){
				String [] versions = versionDir.getName().split("\\.");
				Element applicationElement = root.addElement(APPLICATION_INFO);
				applicationElement.addElement(ACTION).addText(patchArchive?ACTION_TYPE.UPDATE:ACTION_TYPE.CREATE);
				applicationElement.addElement(NAME).addText(applicationDir.getName());
				applicationElement.addElement(TYPE).addText(patchArchive?APPLICATION_TYPE.PATCH:APPLICATION_TYPE.WHOLE);
				applicationElement.addElement("implementation-version").addText("9.0");
				applicationElement.addElement("major-version").addText(versions[0]);
				applicationElement.addElement("minor-version").addText(versions[1]);
				applicationElement.addElement("description").addText(applicationDir.getName() + " version " + versionDir.getName());
				applicationElement.addElement(CREATED_DATE).addText(TIMESTAMP_FORMAT.format(new Date()));
			}
		}

		return doc;
	}
}
