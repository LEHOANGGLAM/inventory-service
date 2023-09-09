package com.yes4all.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A InventoryLock.
 */
@Entity
@Table(name = "inventory_lock")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventoryLock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "lock")
    private Integer lock;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "issue_code")
    private String issueCode;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InventoryLock id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLock() {
        return this.lock;
    }

    public InventoryLock lock(Integer lock) {
        this.setLock(lock);
        return this;
    }

    public void setLock(Integer lock) {
        this.lock = lock;
    }

    public String getSku() {
        return this.sku;
    }

    public InventoryLock sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getProductId() {
        return this.productId;
    }

    public InventoryLock productId(Long productId) {
        this.setProductId(productId);
        return this;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public InventoryLock warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Long getWarehouseId() {
        return this.warehouseId;
    }

    public InventoryLock warehouseId(Long warehouseId) {
        this.setWarehouseId(warehouseId);
        return this;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getIssueCode() {
        return this.issueCode;
    }

    public InventoryLock issueCode(String issueCode) {
        this.setIssueCode(issueCode);
        return this;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryLock)) {
            return false;
        }
        return id != null && id.equals(((InventoryLock) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventoryLock{" +
            "id=" + getId() +
            ", lock=" + getLock() +
            ", sku='" + getSku() + "'" +
            ", productId=" + getProductId() +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", warehouseId=" + getWarehouseId() +
            ", issueCode='" + getIssueCode() + "'" +
            "}";
    }
}
