package com.devalvesg.transaction_service.adapters.mappers;

import com.devalvesg.transaction_service.adapters.dto.FraudRuleViolationResponse;
import com.devalvesg.transaction_service.adapters.dto.TransactionEvent;
import com.devalvesg.transaction_service.adapters.dto.TransactionRequest;
import com.devalvesg.transaction_service.adapters.dto.TransactionResponse;
import com.devalvesg.transaction_service.domain.models.entities.FraudRuleViolationEntity;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionEntity toEntity(TransactionRequest request);

    TransactionResponse toResponse(TransactionEntity entity);

    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "eventTimestamp", ignore = true)
    TransactionEvent toEvent(TransactionEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(TransactionRequest request, @MappingTarget TransactionEntity entity);
    FraudRuleViolationResponse toViolationResponse(FraudRuleViolationEntity entity);
}