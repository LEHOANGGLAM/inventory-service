package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.domain.enumeration.AdjustmentStatus;
import com.yes4all.domain.enumeration.Reason;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Adjustment.
 */
@Entity
@Table(name = "adjustment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Adjustment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdjustmentStatus status;

    @Column(name = "adjustment_code")
    private String adjustmentCode;

    @Column(name = "total_sku")
    private Integer totalSku;

    @Column(name = "date_created")
    private Instant dateCreated;

    @Column(name = "created_by")
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private Reason reason;

    @Column(name = "note")
    private String note;

    @JsonIgnoreProperties(value = { "issueItems", "adjustment", "warehouseFrom", "warehouseTo" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private IssueNote issueNote;

    @JsonIgnoreProperties(value = { "receiptItems", "adjustment", "warehouse" }, allowSetters = true)
    @OneToOne(cascade = { CascadeType.ALL })
    @JoinColumn(unique = true)
    private ReceiptNote receiptNote;

    @OneToMany(mappedBy = "adjustment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "adjustment" }, allowSetters = true)
    private Set<AdjustmentItem> adjustmentItems = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Adjustment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public Adjustment warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public AdjustmentStatus getStatus() {
        return this.status;
    }

    public Adjustment status(AdjustmentStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AdjustmentStatus status) {
        this.status = status;
    }

    public String getAdjustmentCode() {
        return this.adjustmentCode;
    }

    public Adjustment adjustmentCode(String adjustmentCode) {
        this.setAdjustmentCode(adjustmentCode);
        return this;
    }

    public void setAdjustmentCode(String adjustmentCode) {
        this.adjustmentCode = adjustmentCode;
    }

    public Integer getTotalSku() {
        return this.totalSku;
    }

    public Adjustment totalSku(Integer totalSku) {
        this.setTotalSku(totalSku);
        return this;
    }

    public void setTotalSku(Integer totalSku) {
        this.totalSku = totalSku;
    }

    public Instant getDateCreated() {
        return this.dateCreated;
    }

    public Adjustment dateCreated(Instant dateCreated) {
        this.setDateCreated(dateCreated);
        return this;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Adjustment createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Reason getReason() {
        return this.reason;
    }

    public Adjustment reason(Reason reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public String getNote() {
        return this.note;
    }

    public Adjustment note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public IssueNote getIssueNote() {
        return this.issueNote;
    }

    public void setIssueNote(IssueNote issueNote) {
        this.issueNote = issueNote;
    }

    public Adjustment issueNote(IssueNote issueNote) {
        this.setIssueNote(issueNote);
        return this;
    }

    public ReceiptNote getReceiptNote() {
        return this.receiptNote;
    }

    public void setReceiptNote(ReceiptNote receiptNote) {
        this.receiptNote = receiptNote;
    }

    public Adjustment receiptNote(ReceiptNote receiptNote) {
        this.setReceiptNote(receiptNote);
        return this;
    }

    public Set<AdjustmentItem> getAdjustmentItems() {
        return this.adjustmentItems;
    }

    public void setAdjustmentItems(Set<AdjustmentItem> adjustmentItems) {
        if (this.adjustmentItems != null) {
            this.adjustmentItems.forEach(i -> i.setAdjustment(null));
        }
        if (adjustmentItems != null) {
            adjustmentItems.forEach(i -> i.setAdjustment(this));
        }
        this.adjustmentItems = adjustmentItems;
    }

    public Adjustment adjustmentItems(Set<AdjustmentItem> adjustmentItems) {
        this.setAdjustmentItems(adjustmentItems);
        return this;
    }

    public Adjustment addAdjustmentItems(AdjustmentItem adjustmentItem) {
        this.adjustmentItems.add(adjustmentItem);
        adjustmentItem.setAdjustment(this);
        return this;
    }

    public Adjustment removeAdjustmentItems(AdjustmentItem adjustmentItem) {
        this.adjustmentItems.remove(adjustmentItem);
        adjustmentItem.setAdjustment(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Adjustment)) {
            return false;
        }
        return id != null && id.equals(((Adjustment) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Adjustment{" +
            "id=" + getId() +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", status='" + getStatus() + "'" +
            ", adjustmentCode='" + getAdjustmentCode() + "'" +
            ", totalSku=" + getTotalSku() +
            ", dateCreated='" + getDateCreated() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", reason='" + getReason() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
