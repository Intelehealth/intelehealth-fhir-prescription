package org.ih.prescription.data.exchange.scheduler;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Resource;
import org.ih.prescription.data.exchange.config.FhirConfig;
import org.ih.prescription.data.exchange.datatype.ConfigFacilityDataType;
import org.ih.prescription.data.exchange.datatype.OrderType;
import org.ih.prescription.data.exchange.domain.CompletedRecord;
import org.ih.prescription.data.exchange.domain.ConfigDataSync;
import org.ih.prescription.data.exchange.domain.FhirResponse;
import org.ih.prescription.data.exchange.model.ConfigFacility;
import org.ih.prescription.data.exchange.model.DataExchangeAuditLog;
import org.ih.prescription.data.exchange.model.IHMarker;
import org.ih.prescription.data.exchange.service.CommonOperationService;
import org.ih.prescription.data.exchange.service.ConfigDataSyncService;
import org.ih.prescription.data.exchange.service.ConfigFacilityService;
import org.ih.prescription.data.exchange.service.DataExchangeAuditLogService;
import org.ih.prescription.data.exchange.service.IHMarkerService;
import org.ih.prescription.data.exchange.service.utils.DateUtils;
import org.ih.prescription.data.exchange.service.utils.HttpWebClient;
import org.ih.prescription.data.exchange.service.utils.IHConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

@Component
public class DataSendToFHIR extends IHConstant {

	FhirContext fhirContext = FhirContext.forR4();

	@Autowired
	private FhirConfig firFhirConfig;

	@Autowired
	private IHMarkerService ihMarkerService;

	@Autowired
	private CommonOperationService commonOperationService;

	@Autowired
	private ConfigDataSyncService configDataSyncService;

	@Autowired
	private ConfigFacilityService configFacilityService;

	@Autowired
	private DataExchangeAuditLogService dataExchangeService;

	@Scheduled(fixedDelay = 60000, initialDelay = 60000)
	public void scheduleTaskUsingCronExpression()
			throws ParseException, UnsupportedEncodingException, DataFormatException {

		ConfigDataSync medicationSync = configDataSyncService
				.getConfigDataSync(ConfigFacilityDataType.PRESCRIPTION_SHARING);

		if (medicationSync.getStatus()) {
			transferMedication();
			transferMedicationRequest();
		} else {
			System.err.println("Prescription sharing is disabled");
		}
	}

	private void transferMedication() throws UnsupportedEncodingException, DataFormatException, ParseException {
		IHMarker medicationMarker = ihMarkerService.findByName(exportMedication);

		List<CompletedRecord> medications = commonOperationService
				.getCompletedMedication(medicationMarker.getLastSyncTime());

		System.err.println("Total medication to send " + medications.size());

		List<ConfigFacility> configFacilities = configFacilityService.getConfigFacility();

		System.err.println("Total config facilites found : " + configFacilities.size());

		int medicationSendingError = 0;

		for (CompletedRecord theMedication : medications) {
			try {
				send("Medication", theMedication.getUuid(), configFacilities);
			} catch (Exception e) {
				System.err.println(e);
				medicationSendingError++;
			}
		}

		System.err.format("Total Medication found: %d, Successfully Send %d, Error %d\n", medications.size(),
				medications.size() - medicationSendingError, medicationSendingError);

		if (medications.size() > 0) {

			ihMarkerService.updateMarkerByName(exportMedication);
		}
	}

	private void transferMedicationRequest() throws UnsupportedEncodingException, DataFormatException, ParseException {

		IHMarker marker = ihMarkerService.findByName(exportMedicationExternalRequest);

		List<CompletedRecord> medicationRequestList = commonOperationService
				.getCompletedServiceRequest(OrderType.DRUG_ORDER.getValue(), marker.getLastSyncTime());

		System.err.println("Total medication request to send: " + medicationRequestList.size());

		List<ConfigFacility> configFacilities = configFacilityService.getConfigFacility();

		System.err.println("Total config facilites found : " + configFacilities.size());

		int medicationRequestSendingError = 0;

		for (CompletedRecord theMedicationRequest : medicationRequestList) {
			try {
				send("MedicationRequest", theMedicationRequest.getUuid(), configFacilities);
			} catch (Exception e) {
				System.err.println(e);
				medicationRequestSendingError++;
			}
		}

		System.err.format("Total Medication Request found: %d, Successfully Send %d, Error %d\n",
				medicationRequestList.size(), medicationRequestList.size() - medicationRequestSendingError,
				medicationRequestSendingError);

		if (medicationRequestList.size() > 0) {
			ihMarkerService.updateMarkerByName(exportMedicationExternalRequest);
		}
	}

	private void send(String resource, String uuid)
			throws ParseException, UnsupportedEncodingException, DataFormatException {

		System.err.println("resource: " + resource);

		String data = HttpWebClient.get(localOpenmrsOpenhimURL, "/ws/fhir2/R4/" + resource + "?_id=" + uuid,
				firFhirConfig.getOpenMRSCredentials()[0], firFhirConfig.getOpenMRSCredentials()[1]);
		Bundle theBundle = fhirContext.newJsonParser().parseResource(Bundle.class, data);

		sendFHIRBundle(theBundle, resource);

	}

	private void send(String resourceType, String uuid, List<ConfigFacility> configFacilities) throws Exception {
		System.err.println("resource: " + resourceType);

		String data = HttpWebClient.get(localOpenmrsOpenhimURL, "/ws/fhir2/R4/" + resourceType + "?_id=" + uuid,
				firFhirConfig.getOpenMRSCredentials()[0], firFhirConfig.getOpenMRSCredentials()[1]);

		System.err.println("sending >> " + data);

		Bundle theBundle = fhirContext.newJsonParser().parseResource(Bundle.class, data);

		sendFHIRBundle(theBundle, resourceType);

		sendToFacilityServer(theBundle, resourceType, configFacilities);

	}

	public void sendFHIRBundle(Bundle originalTasksBundle, String resourceType)
			throws ParseException, UnsupportedEncodingException, DataFormatException {

		if (originalTasksBundle.hasEntry()) {

			Bundle transactionBundle = new Bundle();
			transactionBundle.setType(Bundle.BundleType.TRANSACTION);
			for (BundleEntryComponent bundleEntry : originalTasksBundle.getEntry()) {
				Resource resource = (Resource) bundleEntry.getResource();
				String resourceId = resource.getIdElement().getIdPart();

				System.err.println("resource.getMeta().getLastUpdated():::" + resource.getMeta().getLastUpdated());

				Bundle.BundleEntryComponent component = transactionBundle.addEntry();
				component.setResource(resource);
				component.getRequest().setUrl(resource.fhirType() + "/" + resourceId).setMethod(Bundle.HTTPVerb.PUT);

				String payload = fhirContext.newJsonParser().setPrettyPrint(true)
						.encodeResourceToString(transactionBundle).toString();

				System.err.println("DDD>>>>>>>>" + payload);

				DataExchangeAuditLog log = new DataExchangeAuditLog();
				log.setResourceName(resourceType);
				log.setResourceUuid(resourceId);
				log.setRequest(payload);
				log.setRequestUrl(shrUrl + "rest/v1/bundle/save");

				DataExchangeAuditLog uLog = dataExchangeService.save(log);
				FhirResponse res = HttpWebClient.postWithBasicAuth(shrUrl, "rest/v1/bundle/save",
						firFhirConfig.getOpenMRSCredentials()[0], firFhirConfig.getOpenMRSCredentials()[1], payload);

				uLog.setResponse(res.getResponse());
				uLog.setResponseStatus(res.getStatusCode());
				if (res.getStatusCode().equals("200")) {
					Bundle remoteBundle = fhirContext.newJsonParser().parseResource(Bundle.class, res.getResponse());
					System.err.println("Response from central fhir: " + res.getResponse());
					uLog.setFhirId(extractResourceId(remoteBundle));
				} else {
					uLog.setStatus(false);
				}
				uLog.setChangedBy(1); // Admin-OpenMRS
				uLog.setDateChanged(DateUtils.toFormattedDateNow());
				dataExchangeService.update(uLog);
			}
		}
		System.err.println("done");
	}

	private void sendToFacilityServer(Bundle bundle, String resourceType, List<ConfigFacility> configFacilites)
			throws Exception {

		Bundle transactionBundle = getTransactionBundle(bundle, resourceType);

		for (ConfigFacility facility : configFacilites) {

			if (facility.getStatus()) {

				if (facility.getPrescriptionApi() != null && !facility.getPrescriptionApi().isEmpty()) {

					String payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(transactionBundle).toString();
					String resourceId = extractResourceId(transactionBundle);

					System.err.println("Payload for hapi >>> " + facility.getPrescriptionApi() + "\n" + payload);

					DataExchangeAuditLog log = new DataExchangeAuditLog();
					log.setResourceName(resourceType);
					log.setResourceUuid(resourceId);
					log.setRequest(payload);
					log.setRequestUrl(facility.getPrescriptionApi() + "");

					DataExchangeAuditLog uLog = dataExchangeService.save(log);

					String username = opencrOpenhimAuthentication.split(":")[0];
					String password = opencrOpenhimAuthentication.split(":")[1];

					FhirResponse res = HttpWebClient.postWithBasicAuth(facility.getPrescriptionApi(), "/", username,
							password, payload);

					uLog.setResponse(res.getResponse());
					uLog.setResponseStatus(res.getStatusCode());

					if (res.getStatusCode().equals("200")) {
						Bundle remoteBundle = fhirContext.newJsonParser().parseResource(Bundle.class,
								res.getResponse());
						System.err.println("Response from central fhir: " + res.getResponse());
						uLog.setFhirId(extractResponseId(remoteBundle));
					} else {
						uLog.setStatus(false);
					}

					uLog.setChangedBy(1); // Admin-OpenMRS
					uLog.setDateChanged(DateUtils.toFormattedDateNow());
					dataExchangeService.update(uLog);

					System.err.println("Prescription Sharing Res  >>> " + res);
				}
			}
		}
	}

	private Bundle getTransactionBundle(Bundle originalTaskBundle, String resourceType) throws Exception {
		Bundle transactionBundle = new Bundle();
		transactionBundle.setType(Bundle.BundleType.TRANSACTION);
		for (BundleEntryComponent bundleEntry : originalTaskBundle.getEntry()) {
			Resource resource = (Resource) bundleEntry.getResource();
			String resourceId = resource.getIdElement().getIdPart();

			System.err.println("resource.getMeta().getLastUpdated():::" + resource.getMeta().getLastUpdated());

			Bundle.BundleEntryComponent component = transactionBundle.addEntry();

			if (resourceType.equals("MedicationRequest")) {
				resource = concatMedicationRequestFullURL(resource);
			}
			component.setResource(resource);
			component.getRequest().setUrl(resource.fhirType() + "/" + resourceId).setMethod(Bundle.HTTPVerb.PUT);
		}
		return transactionBundle;
	}

	private Resource concatMedicationRequestFullURL(Resource resource) {

		String baseURL = opencrOpenhimURL + "/";

		MedicationRequest request = (MedicationRequest) resource;

		// Medication
		String ref = request.getMedicationReference().getReference();
		request.getMedicationReference().setReference(baseURL + ref);

		// Patient
		String patient = request.getSubject().getReference();
		request.getSubject().setReference(baseURL + patient);

		// Encounter
		String encounter = request.getEncounter().getReference();
		request.getEncounter().setReference(baseURL + encounter);

		// Practitioner
		String practitioner = request.getRequester().getReference();
		request.getRequester().setReference(baseURL + practitioner);
		request.getRequester().setIdentifier(null);

		return request;
	}

	private String extractResourceId(Bundle bundle) {
		if (bundle.getEntry().size() != 1)
			return null;
		Resource resource = bundle.getEntryFirstRep().getResource();
		return resource.getIdElement().getIdPart();
	}

	private String extractResponseId(Bundle bundle) {
		if (bundle.getEntry().size() != 1)
			return null;
		BundleEntryResponseComponent response = bundle.getEntryFirstRep().getResponse();
		return response.getLocation().split("/")[1];
	}
}
