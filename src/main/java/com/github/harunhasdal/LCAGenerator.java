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
		File[] files = baseDirectory.listFiles(applicationFilter);
		for(File applicationDir : files){
			Element applicationElement = root.addElement(APPLICATION_INFO);
			applicationElement.addElement(NAME).addText(applicationDir.getName());
			applicationElement.addElement(ACTION).addText(patchArchive?ACTION_TYPE.UPDATE:ACTION_TYPE.CREATE);
			applicationElement.addElement(TYPE).addText(patchArchive?APPLICATION_TYPE.PATCH:APPLICATION_TYPE.WHOLE);
			applicationElement.addElement(CREATED_DATE).addText(TIMESTAMP_FORMAT.format(new Date()));
		}

		return doc;
	}
}
