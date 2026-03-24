package com.imt.api.CentralAPI.client;

import com.imt.api.CentralAPI.dto.InvocationRecordResponse;
import com.imt.api.CentralAPI.dto.MonsterTemplateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class InvocationClient {

  private final WebClient web;

  public InvocationClient(@Value("${invocation.base-url}") String baseUrl, WebClient.Builder builder) {
    this.web = builder.baseUrl(baseUrl).build();
  }

  /** Lister tous les templates de monstres */
  public List<MonsterTemplateResponse> listTemplates() {
    return web.get().uri("/invocations/templates")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<MonsterTemplateResponse>>() {})
        .block();
  }

  /** Tirer un monstre au sort pour un joueur */
  public InvocationRecordResponse roll(String pseudo) {
    return web.post().uri("/invocations/roll/{pseudo}", pseudo)
        .retrieve()
        .bodyToMono(InvocationRecordResponse.class)
        .block();
  }

  /** Signaler que le monstre a été créé dans MonsterAPI */
  public InvocationRecordResponse markMonsterCreated(String recordId, String generatedMonsterId) {
    return web.put().uri(uriBuilder -> uriBuilder
            .path("/invocations/records/{recordId}/monster-created")
            .queryParam("generatedMonsterId", generatedMonsterId)
            .build(recordId))
        .retrieve()
        .bodyToMono(InvocationRecordResponse.class)
        .block();
  }

  /** Signaler que l'invocation est terminée */
  public InvocationRecordResponse markCompleted(String recordId) {
    return web.put().uri("/invocations/records/{recordId}/completed", recordId)
        .retrieve()
        .bodyToMono(InvocationRecordResponse.class)
        .block();
  }

  /** Signaler un échec */
  public InvocationRecordResponse markFailed(String recordId) {
    return web.put().uri("/invocations/records/{recordId}/failed", recordId)
        .retrieve()
        .bodyToMono(InvocationRecordResponse.class)
        .block();
  }

  /** Historique des invocations d'un joueur */
  public List<InvocationRecordResponse> recordsByPlayer(String pseudo) {
    return web.get().uri("/invocations/records/{pseudo}", pseudo)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<InvocationRecordResponse>>() {})
        .block();
  }

  /** Toutes les invocations incomplètes */
  public List<InvocationRecordResponse> incompleteRecords() {
    return web.get().uri("/invocations/records/incomplete")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<InvocationRecordResponse>>() {})
        .block();
  }
}
