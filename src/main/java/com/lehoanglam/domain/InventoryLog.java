package com.yes4all.domain;

import com.yes4all.common.utils.DateUtil;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A InventoryLog.
 */
@Entity
@Table(name = "inventory_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "sku")
    private String sku;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @Column(name = "type")
    private String type;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InventoryLog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public InventoryLog productId(Long productId) {
        this.setProductId(productId);
        return this;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return this.sku;
    }

    public InventoryLog sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public InventoryLog warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Long getWarehouseId() {
        return this.warehouseId;
    }

    public InventoryLog warehouseId(Long warehouseId) {
        this.setWarehouseId(warehouseId);
        return this;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getUserId() {
        return this.userId;
    }

    public InventoryLog userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getQuantityBefore() {
        return this.quantityBefore;
    }

    public InventoryLog quantityBefore(Integer quantityBefore) {
        this.setQuantityBefore(quantityBefore);
        return this;
    }

    public void setQuantityBefore(Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public Integer getQuantityAfter() {
        return this.quantityAfter;
    }

    public InventoryLog quantityAfter(Integer quantityAfter) {
        this.setQuantityAfter(quantityAfter);
        return this;
    }

    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public String getType() {
        return this.type;
    }

    public InventoryLog type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return this.referenceId;
    }

    public InventoryLog referenceId(Long referenceId) {
        this.setReferenceId(referenceId);
        return this;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getNote() {
        return this.note;
    }

    public InventoryLog note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public InventoryLog updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryLog)) {
            return false;
        }
        return id != null && id.equals(((InventoryLog) o).id);
    }

    @PrePersist
    @PreUpdate
    protected void onCreateAndUpdate() {
        updatedAt = DateUtil.currentInstantUTC();
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventoryLog{" +
            "id=" + getId() +
            ", productId=" + getProductId() +
            ", sku='" + getSku() + "'" +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", warehouseId=" + getWarehouseId() +
            ", userId='" + getUserId() + "'" +
            ", quantityBefore=" + getQuantityBefore() +
            ", quantityAfter=" + getQuantityAfter() +
            ", type='" + getType() + "'" +
            ", referenceId=" + getReferenceId() +
            ", note='" + getNote() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
