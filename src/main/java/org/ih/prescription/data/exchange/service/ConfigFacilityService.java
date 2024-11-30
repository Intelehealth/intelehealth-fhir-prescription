package org.ih.prescription.data.exchange.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.ih.prescription.data.exchange.model.ConfigFacility;
import org.springframework.stereotype.Service;

@Service
public class ConfigFacilityService {

	@PersistenceContext
	private EntityManager em;

	public List<ConfigFacility> getConfigFacility() {
		String sql = "select c.id, c.facility_name, c.facility_uuid, c.status, c.prescription_api, "
				+ "c.referral_api,c.lab_api,c.appointment_api from config_fcility c";

		Query q = em.createNativeQuery(sql);

		List rows = q.getResultList();

		ArrayList<ConfigFacility> list = new ArrayList<ConfigFacility>();

		Iterator iter = null;
		for (iter = rows.iterator(); iter.hasNext();) {
			ConfigFacility dto = new ConfigFacility();
			Object[] rs = (Object[]) iter.next();

			dto.setId((Long.parseLong(rs[0].toString())));
			dto.setFacilityName(rs[1].toString());
			dto.setFacilityUuid(rs[2].toString());
			dto.setStatus(Boolean.valueOf(rs[3].toString()));
			dto.setPrescriptionApi(rs[4] != null ? rs[4].toString() : null);
			dto.setReferralApi(rs[5] != null ? rs[5].toString() : null);
			dto.setLabApi(rs[6] != null ? rs[6].toString() : null);
			dto.setAppointmentApi(rs[7] != null ? rs[7].toString() : null);
			list.add(dto);
		}
		return list;
	}
}
