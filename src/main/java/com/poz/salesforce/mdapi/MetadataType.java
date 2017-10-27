package com.poz.salesforce.mdapi;

public enum MetadataType {

	CUSTOM_OBJECT("CustomObject"),
	APEX_CLASS("ApexClass"),
	AURA_DEF_BUNDLE("AuraDefinitionBundle"),
	ASSET("ContentAsset"),
	CUSTOM_TAB("CustomTab"),
	CUSTOM_APP("CustomApplication"),
	GLOBAL_VALUE_SET("GlobalValueSet"),
	INSTALLED_PACKAGE("InstalledPackage"),
	FLEXI_PAGE("FlexiPage"),
	CONTENT_ASSET("ContentAsset");
	
	private String name;
	
	MetadataType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
