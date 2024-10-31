package com.ChargEV.ChargEV.chargingStation.converter;

import com.ChargEV.ChargEV.chargingStation.domain.Kind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class KindConverter implements AttributeConverter<Kind, String> {

    @Override
    public String convertToDatabaseColumn(Kind kindType) {
        return kindType != null ? kindType.name() : null;
    }

    @Override
    public Kind convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Kind.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid kind code: " + dbData, e);
        }
    }
}
