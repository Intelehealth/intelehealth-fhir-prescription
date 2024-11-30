package org.ih.prescription.data.exchange.repository;


import org.ih.prescription.data.exchange.model.IHMarker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonOperationRepository extends JpaRepository<IHMarker, Long>{
	
	

}
