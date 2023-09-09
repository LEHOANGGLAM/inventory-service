package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.enumeration.Channel;
import com.yes4all.domain.enumeration.Department;
import com.yes4all.domain.enumeration.IssueNoteStatus;
import com.yes4all.domain.enumeration.IssueType;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A IssueNote.
 */
@Entity
@Table(name = "issue_note")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IssueNote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "issue_code", nullable = false, unique = true)
    private String issueCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Channel channel;

    @Column(name = "total_confirmed_qty")
    private Integer totalConfirmedQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type")
    private IssueType issueType;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "is_manual_create")
    private Boolean isManualCreate;

    @Column(name = "issue_to_name")
    private String issueToName;

    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Department department;

    @Column(name = "receipt_code")
    private String receiptCode;

    @Column(name = "issue_to_address")
    private String issueToAddress;

    @Column(name = "issue_to_phone")
    private String issueToPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private IssueNoteStatus status;

    @Column(name = "general_note")
    private String generalNote;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "total_remaining_qty")
    private Integer totalRemainingQty;

    @Column(name = "total_actual_exported_qty")
    private Integer totalActualExportedQty;

    @Column(name = "issue_date")
    private Instant issueDate;

    @Column(name = "warehouse_from_code")
    private String warehouseFromCode;

    @Column(name = "warehouse_to_code")
    private String warehouseToCode;

    @OneToMany(mappedBy = "issueNote", cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "product", "issueNote" }, allowSetters = true)
    private Set<IssueItem> issueItems = new HashSet<>();

    @JsonIgnoreProperties(value = { "issueNote", "receiptNote", "adjustmentItems" }, allowSetters = true)
    @OneToOne(mappedBy = "issueNote")
    private Adjustment adjustment;

    @ManyToOne
    @JsonIgnoreProperties(value = { "warehouseInfo", "receiptNotes", "issueNoteFroms", "issueNoteTos" }, allowSetters = true)
    private Warehouse warehouseFrom;

    @ManyToOne
    @JsonIgnoreProperties(value = { "warehouseInfo", "receiptNotes", "issueNoteFroms", "issueNoteTos" }, allowSetters = true)
    private Warehouse warehouseTo;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public IssueNote id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIssueCode() {
        return this.issueCode;
    }

    public IssueNote issueCode(String issueCode) {
        this.setIssueCode(issueCode);
        return this;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public String getReceiptCode() {
        return this.receiptCode;
    }

    public IssueNote receiptCode(String receiptCode) {
        this.setReceiptCode(receiptCode);
        return this;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public IssueNote channel(Channel channel) {
        this.setChannel(channel);
        return this;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Integer getTotalConfirmedQty() {
        return this.totalConfirmedQty;
    }

    public IssueNote totalConfirmedQty(Integer totalConfirmedQty) {
        this.setTotalConfirmedQty(totalConfirmedQty);
        return this;
    }

    public void setTotalConfirmedQty(Integer totalConfirmedQty) {
        this.totalConfirmedQty = totalConfirmedQty;
    }

    public IssueType getIssueType() {
        return this.issueType;
    }

    public IssueNote issueType(IssueType issueType) {
        this.setIssueType(issueType);
        return this;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public IssueNote createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public IssueNote createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIsManualCreate() {
        return this.isManualCreate;
    }

    public IssueNote isManualCreate(Boolean isManualCreate) {
        this.setIsManualCreate(isManualCreate);
        return this;
    }

    public void setIsManualCreate(Boolean isManualCreate) {
        this.isManualCreate = isManualCreate;
    }

    public String getIssueToName() {
        return this.issueToName;
    }

    public IssueNote issueToName(String issueToName) {
        this.setIssueToName(issueToName);
        return this;
    }

    public void setIssueToName(String issueToName) {
        this.issueToName = issueToName;
    }

    public Department getDepartment() {
        return this.department;
    }

    public IssueNote department(Department department) {
        this.setDepartment(department);
        return this;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getIssueToAddress() {
        return this.issueToAddress;
    }

    public IssueNote issueToAddress(String issueToAddress) {
        this.setIssueToAddress(issueToAddress);
        return this;
    }

    public void setIssueToAddress(String issueToAddress) {
        this.issueToAddress = issueToAddress;
    }

    public String getIssueToPhone() {
        return this.issueToPhone;
    }

    public IssueNote issueToPhone(String issueToPhone) {
        this.setIssueToPhone(issueToPhone);
        return this;
    }

    public void setIssueToPhone(String issueToPhone) {
        this.issueToPhone = issueToPhone;
    }

    public IssueNoteStatus getStatus() {
        return this.status;
    }

    public IssueNote status(IssueNoteStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(IssueNoteStatus status) {
        this.status = status;
    }

    public String getGeneralNote() {
        return this.generalNote;
    }

    public IssueNote generalNote(String generalNote) {
        this.setGeneralNote(generalNote);
        return this;
    }

    public void setGeneralNote(String generalNote) {
        this.generalNote = generalNote;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public IssueNote modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Instant getModifiedDate() {
        return this.modifiedDate;
    }

    public IssueNote modifiedDate(Instant modifiedDate) {
        this.setModifiedDate(modifiedDate);
        return this;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public IssueNote isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getTotalRemainingQty() {
        return this.totalRemainingQty;
    }

    public IssueNote totalRemainingQty(Integer totalRemainingQty) {
        this.setTotalRemainingQty(totalRemainingQty);
        return this;
    }

    public void setTotalRemainingQty(Integer totalRemainingQty) {
        this.totalRemainingQty = totalRemainingQty;
    }

    public Integer getTotalActualExportedQty() {
        return this.totalActualExportedQty;
    }

    public IssueNote totalActualExportedQty(Integer totalActualExportedQty) {
        this.setTotalActualExportedQty(totalActualExportedQty);
        return this;
    }

    public void setTotalActualExportedQty(Integer totalActualExportedQty) {
        this.totalActualExportedQty = totalActualExportedQty;
    }

    public Instant getIssueDate() {
        return this.issueDate;
    }

    public IssueNote issueDate(Instant issueDate) {
        this.setIssueDate(issueDate);
        return this;
    }

    public void setIssueDate(Instant issueDate) {
        this.issueDate = issueDate;
    }

    public String getWarehouseFromCode() {
        return this.warehouseFromCode;
    }

    public IssueNote warehouseFromCode(String warehouseFromCode) {
        this.setWarehouseFromCode(warehouseFromCode);
        return this;
    }

    public void setWarehouseFromCode(String warehouseFromCode) {
        this.warehouseFromCode = warehouseFromCode;
    }

    public String getWarehouseToCode() {
        return this.warehouseToCode;
    }

    public IssueNote warehouseToCode(String warehouseToCode) {
        this.setWarehouseToCode(warehouseToCode);
        return this;
    }

    public void setWarehouseToCode(String warehouseToCode) {
        this.warehouseToCode = warehouseToCode;
    }

    public Set<IssueItem> getIssueItems() {
        return this.issueItems;
    }

    public void setIssueItems(Set<IssueItem> issueItems) {
        if (this.issueItems != null) {
            this.issueItems.forEach(i -> i.setIssueNote(null));
        }
        if (issueItems != null) {
            issueItems.forEach(i -> i.setIssueNote(this));
        }
        this.issueItems = issueItems;
    }

    public IssueNote issueItems(Set<IssueItem> issueItems) {
        this.setIssueItems(issueItems);
        return this;
    }

    public IssueNote addIssueItems(IssueItem issueItem) {
        this.issueItems.add(issueItem);
        issueItem.setIssueNote(this);
        return this;
    }

    public IssueNote removeIssueItems(IssueItem issueItem) {
        this.issueItems.remove(issueItem);
        issueItem.setIssueNote(null);
        return this;
    }

    public Adjustment getAdjustment() {
        return this.adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        if (this.adjustment != null) {
            this.adjustment.setIssueNote(null);
        }
        if (adjustment != null) {
            adjustment.setIssueNote(this);
        }
        this.adjustment = adjustment;
    }

    public IssueNote adjustment(Adjustment adjustment) {
        this.setAdjustment(adjustment);
        return this;
    }

    public Warehouse getWarehouseFrom() {
        return this.warehouseFrom;
    }

    public void setWarehouseFrom(Warehouse warehouse) {
        this.warehouseFrom = warehouse;
    }

    public IssueNote warehouseFrom(Warehouse warehouse) {
        this.setWarehouseFrom(warehouse);
        return this;
    }

    public Warehouse getWarehouseTo() {
        return this.warehouseTo;
    }

    public void setWarehouseTo(Warehouse warehouse) {
        this.warehouseTo = warehouse;
    }

    public IssueNote warehouseTo(Warehouse warehouse) {
        this.setWarehouseTo(warehouse);
        return this;
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = DateUtil.currentInstantUTC();
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssueNote)) {
            return false;
        }
        return id != null && id.equals(((IssueNote) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IssueNote{" +
            "id=" + getId() +
            ", issueCode='" + getIssueCode() + "'" +
            ", channel='" + getChannel() + "'" +
            ", totalConfirmedQty=" + getTotalConfirmedQty() +
            ", issueType='" + getIssueType() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", isManualCreate='" + getIsManualCreate() + "'" +
            ", issueToName='" + getIssueToName() + "'" +
            ", department='" + getDepartment() + "'" +
            ", issueToAddress='" + getIssueToAddress() + "'" +
            ", issueToPhone='" + getIssueToPhone() + "'" +
            ", status='" + getStatus() + "'" +
            ", generalNote='" + getGeneralNote() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            ", modifiedDate='" + getModifiedDate() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", totalRemainingQty=" + getTotalRemainingQty() +
            ", totalActualExportedQty=" + getTotalActualExportedQty() +
            ", issueDate='" + getIssueDate() + "'" +
            ", warehouseFromCode='" + getWarehouseFromCode() + "'" +
            ", warehouseToCode='" + getWarehouseToCode() + "'" +
            "}";
    }
}
