package org.ih.prescription.data.exchange.api;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.ih.prescription.data.exchange.datatype.ConfigFacilityDataType;
import org.ih.prescription.data.exchange.domain.ConfigDataSync;
import org.ih.prescription.data.exchange.service.ConfigDataSyncService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prescription/api/v1/control")
public class ActivityController {

	@Autowired
	private ConfigDataSyncService configDataSyncService;

	@GetMapping("/activity")
	public ResponseEntity<?> activity() throws ParseException, JSONException, IOException {
		ConfigDataSync prescriptionSync = configDataSyncService
				.getConfigDataSync(ConfigFacilityDataType.PRESCRIPTION_SHARING);

		HashMap<String, Object> object = new HashMap<>();
		object.put("status", HttpStatus.OK);
		object.put("message", "Prescription module is alive");
		object.put("responseTime", new Date());
		object.put("configDataSyncStatus", prescriptionSync);

		return new ResponseEntity<>(object, HttpStatus.OK);
	}

}
