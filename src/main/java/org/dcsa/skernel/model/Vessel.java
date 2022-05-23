package org.dcsa.skernel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.ForeignKey;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.skernel.validator.ValidVesselIMONumber;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.sql.Join;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Table("vessel")
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Vessel {

    private static final Carrier NULL_CARRIER = new Carrier();
    private static final Vessel NULL_VESSEL = new Vessel();
    private static final Vessel NULL_VESSEL_WITH_NULL_CARRIER = new Vessel();

    static {
        NULL_VESSEL_WITH_NULL_CARRIER.setCarrier(NULL_CARRIER);
    }

    @Id
    @JsonIgnore
    @Column("id")
    private UUID id;

    @NotNull
    @Column("vessel_imo_number")
    @ValidVesselIMONumber
    private String vesselIMONumber;

    @Size(max = 35)
    @Column("vessel_name")
    private String vesselName;

    @Size(max = 2)
    @Column("vessel_flag")
    private String vesselFlag;

    @Size(max = 10)
    @Column("vessel_call_sign")
    private String vesselCallSign;

    @Column("vessel_operator_carrier_id")
    @JsonIgnore
    private UUID vesselOperatorCarrierID;

    @ForeignKey(fromFieldName = "vesselOperatorCarrierID", foreignFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)
    @JsonIgnore
    @Transient
    private Carrier carrier;

    public void setCarrier(Carrier carrier) {
        if (carrier != null && !carrier.equals(NULL_CARRIER)) {
            if (carrier.getSmdgCode() != null) {
                vesselOperatorCarrierCode = carrier.getSmdgCode();
                vesselOperatorCarrierCodeListProvider = CarrierCodeListProvider.SMDG;
            } else if (carrier.getNmftaCode() != null) {
                vesselOperatorCarrierCode = carrier.getNmftaCode();
                vesselOperatorCarrierCodeListProvider = CarrierCodeListProvider.NMFTA;
            } else {
                throw new IllegalArgumentException("Unsupported carrier code list provider.");
            }
        } else {
            vesselOperatorCarrierCode = null;
            vesselOperatorCarrierCodeListProvider = null;
        }
        this.carrier = carrier;
    }

    @Transient
    private String vesselOperatorCarrierCode;

    @Transient
    private CarrierCodeListProvider vesselOperatorCarrierCodeListProvider;

    @JsonIgnore
    public boolean isNullVessel() {
        return this.equals(NULL_VESSEL_WITH_NULL_CARRIER) || this.equals(NULL_VESSEL);
    }
}
