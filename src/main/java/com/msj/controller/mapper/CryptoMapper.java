package com.msj.controller.mapper;

import com.msj.controller.dto.CreateCryptoRequest;
import com.msj.controller.dto.CryptoResponse;
import com.msj.controller.dto.UpdateCryptoRequest;
import com.msj.domain.crypto.Crypto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Crypto domain objects
 * Configured to work with Lombok builders
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CryptoMapper {

    /**
     * Convert CreateCryptoRequest to Crypto domain object
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Crypto toCrypto(CreateCryptoRequest request);

    /**
     * Convert Crypto domain object to CryptoResponse DTO
     */
    @Mapping(target = "id", source = "id.value", qualifiedByName = "tsidToString")
    CryptoResponse toResponse(Crypto crypto);

    @org.mapstruct.Named("tsidToString")
    default String tsidToString(io.hypersistence.tsid.TSID tsid) {
        return tsid != null ? tsid.toString() : null;
    }

    /**
     * Update Crypto entity from UpdateCryptoRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "symbol", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCryptoFromRequest(UpdateCryptoRequest request, @MappingTarget Crypto crypto);
}
