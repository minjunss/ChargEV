package ChargEV.ChargEV.chargingStation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChargingStation is a Querydsl query type for ChargingStation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChargingStation extends EntityPathBase<ChargingStation> {

    private static final long serialVersionUID = 1351066524L;

    public static final QChargingStation chargingStation = new QChargingStation("chargingStation");

    public final StringPath address = createString("address");

    public final StringPath chargerId = createString("chargerId");

    public final EnumPath<ChargerType> chargerType = createEnum("chargerType", ChargerType.class);

    public final StringPath delYn = createString("delYn");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kind = createString("kind");

    public final StringPath kindDetail = createString("kindDetail");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final StringPath limitDetail = createString("limitDetail");

    public final StringPath limitYn = createString("limitYn");

    public final StringPath location = createString("location");

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath method = createString("method");

    public final StringPath name = createString("name");

    public final StringPath note = createString("note");

    public final StringPath output = createString("output");

    public final EnumPath<Stat> stat = createEnum("stat", Stat.class);

    public final StringPath statId = createString("statId");

    public final StringPath updatedDate = createString("updatedDate");

    public final StringPath useTime = createString("useTime");

    public final EnumPath<ZCode> zcode = createEnum("zcode", ZCode.class);

    public QChargingStation(String variable) {
        super(ChargingStation.class, forVariable(variable));
    }

    public QChargingStation(Path<? extends ChargingStation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChargingStation(PathMetadata metadata) {
        super(ChargingStation.class, metadata);
    }

}

