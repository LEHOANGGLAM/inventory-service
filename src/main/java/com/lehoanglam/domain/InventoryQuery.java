package com.yes4all.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A InventoryQuery.
 */
@Entity
@Table(name = "inventory_query")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventoryQuery implements Serializable {

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

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "open_qty")
    private Integer openQty;

    @Column(name = "close_qty")
    private Integer closeQty;

    @Column(name = "receipt_qty")
    private Integer receiptQty;

    @Column(name = "issue_qty")
    private Integer issueQty;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InventoryQuery id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public InventoryQuery productId(Long productId) {
        this.setProductId(productId);
        return this;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return this.sku;
    }

    public InventoryQuery sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getPeriodId() {
        return this.periodId;
    }

    public InventoryQuery periodId(Long periodId) {
        this.setPeriodId(periodId);
        return this;
    }

    public void setPeriodId(Long periodId) {
        this.periodId = periodId;
    }

    public Long getWarehouseId() {
        return this.warehouseId;
    }

    public InventoryQuery warehouseId(Long warehouseId) {
        this.setWarehouseId(warehouseId);
        return this;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public InventoryQuery warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Integer getOpenQty() {
        return this.openQty;
    }

    public InventoryQuery openQty(Integer openQty) {
        this.setOpenQty(openQty);
        return this;
    }

    public void setOpenQty(Integer openQty) {
        this.openQty = openQty;
    }

    public Integer getCloseQty() {
        return this.closeQty;
    }

    public InventoryQuery closeQty(Integer closeQty) {
        this.setCloseQty(closeQty);
        return this;
    }

    public void setCloseQty(Integer closeQty) {
        this.closeQty = closeQty;
    }

    public Integer getReceiptQty() {
        return this.receiptQty;
    }

    public InventoryQuery receiptQty(Integer receiptQty) {
        this.setReceiptQty(receiptQty);
        return this;
    }

    public void setReceiptQty(Integer receiptQty) {
        this.receiptQty = receiptQty;
    }

    public Integer getIssueQty() {
        return this.issueQty;
    }

    public InventoryQuery issueQty(Integer issueQty) {
        this.setIssueQty(issueQty);
        return this;
    }

    public void setIssueQty(Integer issueQty) {
        this.issueQty = issueQty;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryQuery)) {
            return false;
        }
        return id != null && id.equals(((InventoryQuery) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventoryQuery{" +
            "id=" + getId() +
            ", productId=" + getProductId() +
            ", sku='" + getSku() + "'" +
            ", periodId=" + getPeriodId() +
            ", warehouseId=" + getWarehouseId() +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", openQty=" + getOpenQty() +
            ", closeQty=" + getCloseQty() +
            ", receiptQty=" + getReceiptQty() +
            ", issueQty=" + getIssueQty() +
            "}";
    }
}
