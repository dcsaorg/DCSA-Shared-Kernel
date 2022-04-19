package org.dcsa.skernel.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.ForeignKey;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.base.AbstractFacility;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.sql.Join;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacilityTO extends AbstractFacility {

    private static final Address NULL_ADDRESS = new Address();
    private static final LocationTO NULL_LOCATION = new LocationTO();
    private static final LocationTO NULL_LOCATION_WITH_NULL_ADDRESS_AND_NULL_FACILITY = new LocationTO();
    private static final Facility NULL_FACILITY = new Facility();
    private static final FacilityTO NULL_FACILITY_TO = new FacilityTO();
    private static final FacilityTO NULL_FACILITY_TO_WITH_NULL_LOCATION_WITH_NULL_ADDRESS = new FacilityTO();

    static {
        NULL_LOCATION_WITH_NULL_ADDRESS_AND_NULL_FACILITY.setAddress(NULL_ADDRESS);
        NULL_FACILITY_TO_WITH_NULL_LOCATION_WITH_NULL_ADDRESS.setLocation(NULL_LOCATION_WITH_NULL_ADDRESS_AND_NULL_FACILITY);
        NULL_LOCATION_WITH_NULL_ADDRESS_AND_NULL_FACILITY.setFacility(NULL_FACILITY);
    }

    @ForeignKey(foreignFieldName = "id", fromFieldName = "locationID", joinType = Join.JoinType.LEFT_OUTER_JOIN)
    @Transient
    @JsonIgnore /* It is mapped via TransportCallTO and its getLocation */
    private LocationTO location;

    @JsonIgnore
    public boolean isNullFacility() {
        return this.equals(NULL_FACILITY_TO_WITH_NULL_LOCATION_WITH_NULL_ADDRESS) || this.equals(NULL_FACILITY_TO);
    }
}
