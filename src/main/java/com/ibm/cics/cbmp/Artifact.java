package com.ibm.cics.cbmp;

public class Artifact {
	
	private String groupId;
	private String artifactId;
	private String version;
	private String type;
	private String classifier;
	
	public Artifact() {
	}
	
	public String getClassifier() {
		return classifier;
	}
	
	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	boolean matches(org.apache.maven.artifact.Artifact target) {
		if (artifactId == null) throw new IllegalStateException("Artifacts must supply at least the artifact Id");
		
		if (groupId != null && !groupId.equals(target.getGroupId())) return false;
		if (artifactId != null && !artifactId.equals(target.getArtifactId())) return false;
		if (version != null && !version.equals(target.getBaseVersion())) return false;
		if (classifier != null && !classifier.equals(target.getClassifier())) return false;
		if (type != null && !type.equals(target.getType())) return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "Artifact [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type=" + type
				+ ", classifier=" + classifier + "]";
	}
	
}