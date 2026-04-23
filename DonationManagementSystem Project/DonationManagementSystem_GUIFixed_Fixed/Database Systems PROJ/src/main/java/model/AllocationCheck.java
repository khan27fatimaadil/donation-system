package model;

import java.math.BigDecimal;

/**
 * AllocationCheck — read-only result object from the vw_allocation_check view.
 * Shows how much of a donation has been allocated vs. how much remains.
 */
public class AllocationCheck {
    private final String     donationId;
    private final BigDecimal donationAmount;
    private final BigDecimal allocatedAmount;
    private final BigDecimal unallocatedAmount;

    public AllocationCheck(String donationId,
                           BigDecimal donationAmount,
                           BigDecimal allocatedAmount,
                           BigDecimal unallocatedAmount) {
        this.donationId        = donationId;
        this.donationAmount    = donationAmount    != null ? donationAmount    : BigDecimal.ZERO;
        this.allocatedAmount   = allocatedAmount   != null ? allocatedAmount   : BigDecimal.ZERO;
        this.unallocatedAmount = unallocatedAmount != null ? unallocatedAmount : BigDecimal.ZERO;
    }

    public String     getDonationId()        { return donationId; }
    public BigDecimal getDonationAmount()    { return donationAmount; }
    public BigDecimal getAllocatedAmount()   { return allocatedAmount; }
    public BigDecimal getUnallocatedAmount(){ return unallocatedAmount; }

    @Override
    public String toString() {
        return "AllocationCheck{donationId='" + donationId + "', donation=" + donationAmount
             + ", allocated=" + allocatedAmount + ", unallocated=" + unallocatedAmount + "}";
    }
}
