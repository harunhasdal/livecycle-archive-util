package com.github.harunhasdal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface AppInfoNameSpaceConsumer {
	public static final String NAMESPACE_URI = "http://adobe.com/idp/applicationmanager/appinfo";

	public static final String LCA_INFO = "lca_info";
	public static final String APPLICATION_INFO = "application-info";

	public static final String TYPE = "type";
	public static final String DESCRIPTION = "description";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE = "createdDate";
	public static final String IMPLEMENTATION_VERSION = "implementation-version";
	public static final String MAJOR_VERSION = "major-version";
	public static final String MINOR_VERSION = "minor-version";

	public static final String NAME = "name";
	public static final String ACTION = "action";

	public static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

	public static interface LCA_TYPE {
		public static final String MULTIPLE = "Multiple";
		public static final String SIMPLE = "Simple";
	}

	public static interface ACTION_TYPE{
		public static final String UPDATE = "Update";
		public static final String CREATE = "Create";
	}

	public static interface APPLICATION_TYPE{
		public static final String WHOLE = "WHOLE";
		public static final String PATCH = "PATCH";
	}
}
