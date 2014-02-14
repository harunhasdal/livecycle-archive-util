package com.github.harunhasdal;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
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



		return doc;
	}
}
