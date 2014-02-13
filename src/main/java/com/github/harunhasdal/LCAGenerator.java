package com.github.harunhasdal;

import java.io.File;

public class LCAGenerator
{
	private File baseDirectory;
	private boolean patchArchive;
	private boolean multiple;

	public LCAGenerator(File baseDirectory) {
		this(baseDirectory, false, false);
	}

	public LCAGenerator(File baseDirectory, boolean patchArchive) {
		this(baseDirectory, patchArchive, false);
	}

	public LCAGenerator(File baseDirectory, boolean patchArchive, boolean multiple) {
		if(baseDirectory == null || !baseDirectory.exists()){
			throw new IllegalArgumentException("Base directory should be provided.");
		}
		this.baseDirectory = baseDirectory;
		this.patchArchive = patchArchive;
		this.multiple = multiple;
	}

}
