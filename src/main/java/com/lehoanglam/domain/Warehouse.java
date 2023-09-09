package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Warehouse.
 */
@Entity
@Table(name = "warehouse")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Warehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "warehouse_code", nullable = false, unique = true)
    private String warehouseCode;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "warehouse_status")
    private String warehouseStatus;

    @Column(name = "address")
    private String address;

    @Column(name = "pallet")
    private Integer pallet;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @OneToOne
    @JoinColumn(unique = true)
    private WarehouseInfo warehouseInfo;

    @OneToMany(mappedBy = "warehouse")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "receiptItems", "warehouse" }, allowSetters = true)
    private Set<ReceiptNote> receiptNotes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Warehouse id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public Warehouse warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseName() {
        return this.warehouseName;
    }

    public Warehouse warehouseName(String warehouseName) {
        this.setWarehouseName(warehouseName);
        return this;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseStatus() {
        return this.warehouseStatus;
    }

    public Warehouse warehouseStatus(String warehouseStatus) {
        this.setWarehouseStatus(warehouseStatus);
        return this;
    }

    public void setWarehouseStatus(String warehouseStatus) {
        this.warehouseStatus = warehouseStatus;
    }

    public String getAddress() {
        return this.address;
    }

    public Warehouse address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPallet() {
        return this.pallet;
    }

    public Warehouse pallet(Integer pallet) {
        this.setPallet(pallet);
        return this;
    }

    public void setPallet(Integer pallet) {
        this.pallet = pallet;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Warehouse createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Warehouse createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public WarehouseInfo getWarehouseInfo() {
        return this.warehouseInfo;
    }

    public void setWarehouseInfo(WarehouseInfo warehouseInfo) {
        this.warehouseInfo = warehouseInfo;
    }

    public Warehouse warehouseInfo(WarehouseInfo warehouseInfo) {
        this.setWarehouseInfo(warehouseInfo);
        return this;
    }

    public Set<ReceiptNote> getReceiptNotes() {
        return this.receiptNotes;
    }

    public void setReceiptNotes(Set<ReceiptNote> receiptNotes) {
        if (this.receiptNotes != null) {
            this.receiptNotes.forEach(i -> i.setWarehouse(null));
        }
        if (receiptNotes != null) {
            receiptNotes.forEach(i -> i.setWarehouse(this));
        }
        this.receiptNotes = receiptNotes;
    }

    public Warehouse receiptNotes(Set<ReceiptNote> receiptNotes) {
        this.setReceiptNotes(receiptNotes);
        return this;
    }

    public Warehouse addReceiptNote(ReceiptNote receiptNote) {
        this.receiptNotes.add(receiptNote);
        receiptNote.setWarehouse(this);
        return this;
    }

    public Warehouse removeReceiptNote(ReceiptNote receiptNote) {
        this.receiptNotes.remove(receiptNote);
        receiptNote.setWarehouse(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Warehouse)) {
            return false;
        }
        return id != null && id.equals(((Warehouse) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Warehouse{" +
            "id=" + getId() +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", warehouseName='" + getWarehouseName() + "'" +
            ", warehouseStatus='" + getWarehouseStatus() + "'" +
            ", address='" + getAddress() + "'" +
            ", pallet=" + getPallet() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            "}";
    }
}
