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
		this.baseDirectory = baseDirectory;
		this.patchArchive = patchArchive;
		this.multiple = multiple;
	}

}
