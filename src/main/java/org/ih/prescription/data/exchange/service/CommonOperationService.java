package org.ih.prescription.data.exchange.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.ih.prescription.data.exchange.domain.CompletedRecord;
import org.springframework.stereotype.Service;

@Service
public class CommonOperationService {

	@PersistenceContext
	private EntityManager em;

	public List<CompletedRecord> getCompletedServiceRequest(int type, String date) {
		String sql = "SELECT o.uuid, o.order_id, o.date_created  from orders o  WHERE"
				+ " o.order_type_id=:type and o.date_created >=:date and o.voided=false";

		List<CompletedRecord> records = new ArrayList<CompletedRecord>();

		List resultList = em.createNativeQuery(sql).setParameter("type", type).setParameter("date", date)
				.getResultList();

		Iterator iter = null;
		for (iter = resultList.iterator(); iter.hasNext();) {
			CompletedRecord theRecord = new CompletedRecord();
			Object[] resultArray = (Object[]) iter.next();

			theRecord.setUuid(resultArray[0].toString());
			theRecord.setId(Integer.parseInt(resultArray[1].toString()));
			theRecord.setDateCreated(resultArray[2].toString());

			records.add(theRecord);
		}
		return records;
	}

	public List<CompletedRecord> getCompletedMedication(String date) {
		String sql = "select d.uuid ,d.drug_id, d.date_created  from drug d "
				+ " where  d.date_created > :date_created or d.date_changed > :date_created";

		List<CompletedRecord> records = new ArrayList<CompletedRecord>();

		List resultList = em.createNativeQuery(sql).setParameter("date_created", date).getResultList();
		Iterator iter = null;
		for (iter = resultList.iterator(); iter.hasNext();) {
			CompletedRecord theRecord = new CompletedRecord();
			Object[] resultArray = (Object[]) iter.next();
			theRecord.setId(Integer.parseInt(resultArray[1].toString()));
			theRecord.setUuid(resultArray[0].toString());
			theRecord.setDateCreated(resultArray[2].toString());

			records.add(theRecord);

		}
		return records;
	}

}
