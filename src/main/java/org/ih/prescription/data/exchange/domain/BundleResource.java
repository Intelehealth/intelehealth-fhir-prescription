package org.ih.prescription.data.exchange.domain;

import org.hl7.fhir.r4.model.Resource;

public class BundleResource {
	
	Resource resource;
	String resourceId;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String toString() {
		return "BundleResource [resource=" + resource + ", resourceId=" + resourceId + "]";
	}

}
