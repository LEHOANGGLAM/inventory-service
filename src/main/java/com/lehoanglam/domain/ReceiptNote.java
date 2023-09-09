package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.enumeration.Department;
import com.yes4all.domain.enumeration.ReceiptNoteStatus;
import com.yes4all.domain.enumeration.ReceiptType;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ReceiptNote.
 */
@Entity
@Table(name = "receipt_note")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReceiptNote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "receipt_code", nullable = false, unique = true)
    private String receiptCode;

    @Column(name = "shipment_no")
    private String shipmentNo;

    @Column(name = "container_no")
    private String containerNo;

    @Column(name = "issue_code")
    private String issueCode;

    @Column(name = "total_confirmed_qty")
    private Integer totalConfirmedQty;

    @Column(name = "total_actual_imported_qty")
    private Integer totalActualImportedQty;

    @Column(name = "total_difference_qty")
    private Integer totalDifferenceQty;

    @Column(name = "created_by")
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type")
    private ReceiptType receiptType;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "is_manual_create")
    private Boolean isManualCreate;

    @Column(name = "general_note")
    private String generalNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReceiptNoteStatus status;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "receipt_date")
    private Instant receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Department department;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @OneToMany(mappedBy = "receiptNote", cascade = { CascadeType.ALL })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "product", "receiptNote" }, allowSetters = true)
    private Set<ReceiptItem> receiptItems = new HashSet<>();

    @JsonIgnoreProperties(value = { "issueNote", "receiptNote", "adjustmentItems" }, allowSetters = true)
    @OneToOne(mappedBy = "receiptNote")
    private Adjustment adjustment;

    @ManyToOne
    @JsonIgnoreProperties(value = { "warehouseInfo", "receiptNotes", "issueNoteFroms", "issueNoteTos" }, allowSetters = true)
    private Warehouse warehouse;

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = DateUtil.currentInstantUTC();
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ReceiptNote id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiptCode() {
        return this.receiptCode;
    }

    public ReceiptNote receiptCode(String receiptCode) {
        this.setReceiptCode(receiptCode);
        return this;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getShipmentNo() {
        return this.shipmentNo;
    }

    public ReceiptNote shipmentNo(String shipmentNo) {
        this.setShipmentNo(shipmentNo);
        return this;
    }

    public void setShipmentNo(String shipmentNo) {
        this.shipmentNo = shipmentNo;
    }

    public String getContainerNo() {
        return this.containerNo;
    }

    public ReceiptNote containerNo(String containerNo) {
        this.setContainerNo(containerNo);
        return this;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getIssueCode() {
        return this.issueCode;
    }

    public ReceiptNote issueCode(String issueCode) {
        this.setIssueCode(issueCode);
        return this;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public Integer getTotalConfirmedQty() {
        return this.totalConfirmedQty;
    }

    public ReceiptNote totalTransferredQty(Integer totalTransferredQty) {
        this.setTotalConfirmedQty(totalTransferredQty);
        return this;
    }

    public void setTotalConfirmedQty(Integer totalConfirmedQty) {
        this.totalConfirmedQty = totalConfirmedQty;
    }

    public Integer getTotalActualImportedQty() {
        return this.totalActualImportedQty;
    }

    public ReceiptNote totalActualImportedQty(Integer totalActualImportedQty) {
        this.setTotalActualImportedQty(totalActualImportedQty);
        return this;
    }

    public void setTotalActualImportedQty(Integer totalActualImportedQty) {
        this.totalActualImportedQty = totalActualImportedQty;
    }

    public Integer getTotalDifferenceQty() {
        return this.totalDifferenceQty;
    }

    public ReceiptNote shortageQty(Integer shortageQty) {
        this.setShortageQty(shortageQty);
        return this;
    }

    public void setShortageQty(Integer shortageQty) {
        this.totalDifferenceQty = shortageQty;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public ReceiptNote createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ReceiptType getReceiptType() {
        return this.receiptType;
    }

    public ReceiptNote receiptType(ReceiptType receiptType) {
        this.setReceiptType(receiptType);
        return this;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public ReceiptNote createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getIsManualCreate() {
        return this.isManualCreate;
    }

    public ReceiptNote isManualCreate(Boolean isManualCreate) {
        this.setIsManualCreate(isManualCreate);
        return this;
    }

    public void setIsManualCreate(Boolean isManualCreate) {
        this.isManualCreate = isManualCreate;
    }

    public String getGeneralNote() {
        return this.generalNote;
    }

    public ReceiptNote generalNote(String generalNote) {
        this.setGeneralNote(generalNote);
        return this;
    }

    public void setGeneralNote(String generalNote) {
        this.generalNote = generalNote;
    }

    public ReceiptNoteStatus getStatus() {
        return this.status;
    }

    public ReceiptNote status(ReceiptNoteStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(ReceiptNoteStatus status) {
        this.status = status;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public ReceiptNote modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Instant getModifiedDate() {
        return this.modifiedDate;
    }

    public ReceiptNote modifiedDate(Instant modifiedDate) {
        this.setModifiedDate(modifiedDate);
        return this;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public ReceiptNote isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getReceiptDate() {
        return this.receiptDate;
    }

    public ReceiptNote receiptDate(Instant receiptDate) {
        this.setReceiptDate(receiptDate);
        return this;
    }

    public void setReceiptDate(Instant receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Department getDepartment() {
        return this.department;
    }

    public ReceiptNote department(Department department) {
        this.setDepartment(department);
        return this;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public ReceiptNote warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Set<ReceiptItem> getReceiptItems() {
        return this.receiptItems;
    }

    public void setReceiptItems(Set<ReceiptItem> receiptItems) {
        if (this.receiptItems != null) {
            this.receiptItems.forEach(i -> i.setReceiptNote(null));
        }
        if (receiptItems != null) {
            receiptItems.forEach(i -> i.setReceiptNote(this));
        }
        this.receiptItems = receiptItems;
    }

    public ReceiptNote receiptItems(Set<ReceiptItem> receiptItems) {
        this.setReceiptItems(receiptItems);
        return this;
    }

    public ReceiptNote addReceiptItem(ReceiptItem receiptItem) {
        this.receiptItems.add(receiptItem);
        receiptItem.setReceiptNote(this);
        return this;
    }

    public ReceiptNote removeReceiptItem(ReceiptItem receiptItem) {
        this.receiptItems.remove(receiptItem);
        receiptItem.setReceiptNote(null);
        return this;
    }

    public Adjustment getAdjustment() {
        return this.adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        if (this.adjustment != null) {
            this.adjustment.setReceiptNote(null);
        }
        if (adjustment != null) {
            adjustment.setReceiptNote(this);
        }
        this.adjustment = adjustment;
    }

    public ReceiptNote adjustment(Adjustment adjustment) {
        this.setAdjustment(adjustment);
        return this;
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ReceiptNote warehouse(Warehouse warehouse) {
        this.setWarehouse(warehouse);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReceiptNote)) {
            return false;
        }
        return id != null && id.equals(((ReceiptNote) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReceiptNote{" +
            "id=" + getId() +
            ", receiptCode='" + getReceiptCode() + "'" +
            ", shipmentNo='" + getShipmentNo() + "'" +
            ", containerNo='" + getContainerNo() + "'" +
            ", issueCode='" + getIssueCode() + "'" +
            ", totalTransferredQty=" + getTotalConfirmedQty() +
            ", totalCountedQty=" + getTotalActualImportedQty() +
            ", shortageQty=" + getTotalDifferenceQty() +
            ", createdBy='" + getCreatedBy() + "'" +
            ", receiptType='" + getReceiptType() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", isManualCreate='" + getIsManualCreate() + "'" +
            ", generalNote='" + getGeneralNote() + "'" +
            ", status='" + getStatus() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            ", modifiedDate='" + getModifiedDate() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", receiptDate='" + getReceiptDate() + "'" +
            ", department='" + getDepartment() + "'" +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            "}";
    }
}
