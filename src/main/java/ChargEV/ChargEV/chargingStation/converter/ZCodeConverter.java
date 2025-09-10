package ChargEV.ChargEV.chargingStation.converter;

import ChargEV.ChargEV.chargingStation.domain.ZCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ZCodeConverter implements AttributeConverter<ZCode, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ZCode zCode) {
        return zCode != null ? zCode.getCode() : null;
    }

    @Override
    public ZCode convertToEntityAttribute(Integer code) {
        if (code == null) return null;

        for(ZCode zCode : ZCode.values()) {
            if (zCode.getCode() == code) {
                return zCode;
            }
        }
        throw new IllegalArgumentException("Unknown ZCode: " + code);
    }
}
