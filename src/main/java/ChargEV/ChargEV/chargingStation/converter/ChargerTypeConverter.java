package ChargEV.ChargEV.chargingStation.converter;

import ChargEV.ChargEV.chargingStation.domain.ChargerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ChargerTypeConverter implements AttributeConverter<ChargerType, String> {

    @Override
    public String convertToDatabaseColumn(ChargerType chargerType) {
        return chargerType != null? chargerType.getCode(): null;
    }

    @Override
    public ChargerType convertToEntityAttribute(String code) {
        for(ChargerType type : ChargerType.values()) {
            if(type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
