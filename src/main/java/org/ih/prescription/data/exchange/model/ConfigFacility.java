package org.ih.prescription.data.exchange.model;

public class ConfigFacility {

	private Long id;
	private String facilityName;
	private String facilityUuid;
	private Boolean status;
	private String prescriptionApi;
	private String referralApi;
	private String labApi;
	private String appointmentApi;
	private String uuid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getFacilityUuid() {
		return facilityUuid;
	}

	public void setFacilityUuid(String facilityUuid) {
		this.facilityUuid = facilityUuid;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getPrescriptionApi() {
		return prescriptionApi;
	}

	public void setPrescriptionApi(String prescriptionApi) {
		this.prescriptionApi = prescriptionApi;
	}

	public String getReferralApi() {
		return referralApi;
	}

	public void setReferralApi(String referralApi) {
		this.referralApi = referralApi;
	}

	public String getLabApi() {
		return labApi;
	}

	public void setLabApi(String labApi) {
		this.labApi = labApi;
	}

	public String getAppointmentApi() {
		return appointmentApi;
	}

	public void setAppointmentApi(String appointmentApi) {
		this.appointmentApi = appointmentApi;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "ConfigFacility [id=" + id + ", facilityName=" + facilityName + ", facilityUuid=" + facilityUuid
				+ ", status=" + status + ", prescriptionApi=" + prescriptionApi + ", referralApi=" + referralApi
				+ ", labApi=" + labApi + ", appointmentApi=" + appointmentApi + ", uuid=" + uuid + "]";
	}

}
