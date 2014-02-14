package com.github.harunhasdal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface AppInfoNameSpaceConsumer {

	public static final String LCA_INFO = "lca_info";
	public static final String NAMESPACE_URI = "http://adobe.com/idp/applicationmanager/appinfo";

	public static final String TYPE = "type";
	public static final String DESCRIPTION = "description";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE = "createdDate";

	public static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

	public static interface LCA_TYPE {
		public static final String MULTIPLE = "Multiple";
		public static final String SIMPLE = "Simple";
	}
}
