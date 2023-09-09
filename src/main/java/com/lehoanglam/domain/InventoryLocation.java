package com.yes4all.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A InventoryLocation.
 */
@Entity
@Table(name = "inventory_location")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventoryLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "ovs_quantity")
    private Integer ovsQuantity;

    @Column(name = "pku_quantity")
    private Integer pkuQuantity;

    @Column(name = "wip_quantity")
    private Integer wipQuantity;

    @Column(name = "total")
    private Integer total;

    @Column(name = "pickup_row")
    private String pickupRow;

    @Column(name = "pku_location")
    private String pkuLocation;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InventoryLocation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWarehouseCode() {
        return this.warehouseCode;
    }

    public InventoryLocation warehouseCode(String warehouseCode) {
        this.setWarehouseCode(warehouseCode);
        return this;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getSku() {
        return this.sku;
    }

    public InventoryLocation sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductTitle() {
        return this.productTitle;
    }

    public InventoryLocation productTitle(String productTitle) {
        this.setProductTitle(productTitle);
        return this;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public Integer getOvsQuantity() {
        return this.ovsQuantity;
    }

    public InventoryLocation ovsQuantity(Integer ovsQuantity) {
        this.setOvsQuantity(ovsQuantity);
        return this;
    }

    public void setOvsQuantity(Integer ovsQuantity) {
        this.ovsQuantity = ovsQuantity;
    }

    public Integer getPkuQuantity() {
        return this.pkuQuantity;
    }

    public InventoryLocation pkuQuantity(Integer pkuQuantity) {
        this.setPkuQuantity(pkuQuantity);
        return this;
    }

    public void setPkuQuantity(Integer pkuQuantity) {
        this.pkuQuantity = pkuQuantity;
    }

    public Integer getWipQuantity() {
        return this.wipQuantity;
    }

    public InventoryLocation wipQuantity(Integer wipQuantity) {
        this.setWipQuantity(wipQuantity);
        return this;
    }

    public void setWipQuantity(Integer wipQuantity) {
        this.wipQuantity = wipQuantity;
    }

    public Integer getTotal() {
        return this.total;
    }

    public InventoryLocation total(Integer total) {
        this.setTotal(total);
        return this;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getPickupRow() {
        return this.pickupRow;
    }

    public InventoryLocation pickupRow(String pickupRow) {
        this.setPickupRow(pickupRow);
        return this;
    }

    public void setPickupRow(String pickupRow) {
        this.pickupRow = pickupRow;
    }

    public String getPkuLocation() {
        return this.pkuLocation;
    }

    public InventoryLocation pkuLocation(String pkuLocation) {
        this.setPkuLocation(pkuLocation);
        return this;
    }

    public void setPkuLocation(String pkuLocation) {
        this.pkuLocation = pkuLocation;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryLocation)) {
            return false;
        }
        return id != null && id.equals(((InventoryLocation) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventoryLocation{" +
            "id=" + getId() +
            ", warehouseCode='" + getWarehouseCode() + "'" +
            ", sku='" + getSku() + "'" +
            ", productTitle='" + getProductTitle() + "'" +
            ", ovsQuantity=" + getOvsQuantity() +
            ", pkuQuantity=" + getPkuQuantity() +
            ", wipQuantity=" + getWipQuantity() +
            ", total=" + getTotal() +
            ", pickupRow='" + getPickupRow() + "'" +
            ", pkuLocation='" + getPkuLocation() + "'" +
            "}";
    }
}
