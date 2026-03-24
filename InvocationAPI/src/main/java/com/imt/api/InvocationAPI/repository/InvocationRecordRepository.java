package com.imt.api.InvocationAPI.repository;

import com.imt.api.InvocationAPI.model.InvocationRecord;
import com.imt.api.InvocationAPI.model.InvocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvocationRecordRepository extends MongoRepository<InvocationRecord, String> {
  List<InvocationRecord> findByPlayerPseudo(String playerPseudo);
  List<InvocationRecord> findByStatus(InvocationStatus status);
  List<InvocationRecord> findByStatusIn(List<InvocationStatus> statuses);
}
