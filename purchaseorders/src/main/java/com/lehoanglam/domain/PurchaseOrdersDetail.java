package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "purchase_orders_detail")
public class PurchaseOrdersDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;

    @NotNull
    @Column(name = "from_so")
    private String fromSo;
    @Column(name = "a_sin")
    private String asin;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "qty_ordered")
    private Long qtyOrdered;

    @Column(name = "make_to_stock")
    private Long makeToStock;


    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    @Column(name = "cdc_version")
    private Long cdcVersion;


    @Column(name = "unit_measure")
    private String unitMeasure;

    @Column(name = "unit_cost")
    private Double unitCost;
    @Column(name = "qty_used")
    private Long qtyUsed;
    @Column(name = "amount")
    private Double amount;

    @Column(name = "pcs")
    private Integer pcs;


    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "total_box")
    private Double totalBox;

    @Column(name = "total_box_cross_weight")
    private Double totalBoxCrossWeight;

    @Column(name = "total_box_net_weight")
    private Double totalBoxNetWeight;

    @Column(name = "total_box_net_weight_previous")
    private Double totalBoxNetWeightPrevious;

    @Column(name = "unit_cost_previous")
    private Double unitCostPrevious;

    @Column(name = "qty_ordered_previous")
    private Long qtyOrderedPrevious;
    @Column(name = "amount_previous")
    private Double amountPrevious;

    @Column(name = "pcs_previous")
    private Integer pcsPrevious;

    @Column(name = "total_volume_previous")
    private Double totalVolumePrevious;

    @Column(name = "total_box_previous")
    private Double totalBoxPrevious;

    @Column(name = "total_box_cross_weight_previous")
    private Double totalBoxCrossWeightPrevious;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "booking_number")
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"purchaseOrders"}, allowSetters = true)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id")
    private PurchaseOrders purchaseOrders;


    public Double getTotalBoxNetWeightPrevious() {
        return totalBoxNetWeightPrevious;
    }

    public void setTotalBoxNetWeightPrevious(Double totalBoxNetWeightPrevious) {
        this.totalBoxNetWeightPrevious = totalBoxNetWeightPrevious;
    }

    public PurchaseOrdersDetail(String sku, String productName, Long qtyOrdered) {
        this.sku = sku;
        this.productName = productName;
        this.qtyOrdered = qtyOrdered;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public void setUnitMeasure(String unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getUnitCostPrevious() {
        return unitCostPrevious;
    }

    public void setUnitCostPrevious(Double unitCostPrevious) {
        this.unitCostPrevious = unitCostPrevious;
    }

    public Long getQtyOrderedPrevious() {
        return qtyOrderedPrevious;
    }

    public void setQtyOrderedPrevious(Long qtyOrderedPrevious) {
        this.qtyOrderedPrevious = qtyOrderedPrevious;
    }

    public Double getAmountPrevious() {
        return amountPrevious;
    }

    public void setAmountPrevious(Double amountPrevious) {
        this.amountPrevious = amountPrevious;
    }

    public Integer getPcsPrevious() {
        return pcsPrevious;
    }

    public void setPcsPrevious(Integer pcsPrevious) {
        this.pcsPrevious = pcsPrevious;
    }


    public Double getTotalVolumePrevious() {
        return totalVolumePrevious;
    }

    public void setTotalVolumePrevious(Double totalVolumePrevious) {
        this.totalVolumePrevious = totalVolumePrevious;
    }

    public Double getTotalBoxPrevious() {
        return totalBoxPrevious;
    }

    public void setTotalBoxPrevious(Double totalBoxPrevious) {
        this.totalBoxPrevious = totalBoxPrevious;
    }

    public Double getTotalBoxCrossWeightPrevious() {
        return totalBoxCrossWeightPrevious;
    }

    public void setTotalBoxCrossWeightPrevious(Double totalBoxCrossWeightPrevious) {
        this.totalBoxCrossWeightPrevious = totalBoxCrossWeightPrevious;
    }

    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }




    public void setTotalBoxCrossWeight(Double totalBoxCrossWeight) {
        this.totalBoxCrossWeight = totalBoxCrossWeight;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public void setTotalBox(Double totalBox) {
        this.totalBox = totalBox;
    }

    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public Double getAmount() {
        return amount;
    }

    public Integer getPcs() {
        return pcs;
    }



    public Double getTotalBoxNetWeight() {
        return totalBoxNetWeight;
    }

    public void setTotalBoxNetWeight(Double totalBoxNetWeight) {
        this.totalBoxNetWeight = totalBoxNetWeight;
    }

    public Double getTotalBoxCrossWeight() {
        return totalBoxCrossWeight;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public Double getTotalBox() {
        return totalBox;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public String getUnitMeasure() {
        return unitMeasure;
    }
    public void setQtyUsed(Long qtyUsed) {
        this.qtyUsed = qtyUsed;
    }

    public Long getQtyUsed() {
        return qtyUsed;
    }

    public PurchaseOrdersDetail() {
    }

    public void setPurchaseOrders(PurchaseOrders purchaseOrders) {
        this.purchaseOrders = purchaseOrders;
    }

    public PurchaseOrders getPurchaseOrders() {
        return purchaseOrders;
    }
    // jhipster-needle-entity-add-field - JHipster will add fields here

    public void setFromSo(String fromSo) {
        this.fromSo = fromSo;
    }

    public String getFromSo() {
        return fromSo;
    }

    public Integer getId() {
        return this.id;
    }

    public PurchaseOrdersDetail id(Integer id) {
        this.setId(id);
        return this;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public PurchaseOrdersDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }




    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date().toInstant();
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQtyOrdered(Long qtyOrdered) {
        this.qtyOrdered = qtyOrdered;
    }

    public void setMakeToStock(Long makeToStock) {
        this.makeToStock = makeToStock;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }


    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }


    public void setCdcVersion(Long cdcVersion) {
        this.cdcVersion = cdcVersion;
    }

    public String getProductName() {
        return productName;
    }

    public Long getQtyOrdered() {
        return qtyOrdered;
    }

    public Long getMakeToStock() {
        return makeToStock;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }


    public Boolean getDeleted() {
        return isDeleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public Long getCdcVersion() {
        return cdcVersion;
    }

    public PurchaseOrdersDetail purchaseOrders(PurchaseOrders purchaseOrders) {
        this.setPurchaseOrders(purchaseOrders);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersDetail)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
