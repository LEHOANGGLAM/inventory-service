package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "proforma_invoice_detail")
public class ProformaInvoiceDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;


    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "ctn")
    private Double ctn;

    @Column(name = "pcs")
    private Integer pcs;

    @Column(name = "cbm_unit")
    private Double cbmUnit;

    @Column(name = "cbm_total")
    private Double cbmTotal;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "ship_date")
    private LocalDate ship_date;

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

    @Column(name = "from_so")
    private String fromSo;

    @Column(name = "onboard_qty")
    private Integer onboardQty;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "a_sin")
    private String aSin;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "proforma_invoice_id", referencedColumnName = "id")
    private ProformaInvoice proformaInvoice;

    public String getaSin() {
        return aSin;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public Integer getOnboardQty() {
        return onboardQty;
    }

    public void setOnboardQty(Integer onboardQty) {
        this.onboardQty = onboardQty;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }




    public void setFromSo(String fromSo) {
        this.fromSo = fromSo;
    }

    public String getFromSo() {
        return fromSo;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void setCdcVersion(Long cdcVersion) {
        this.cdcVersion = cdcVersion;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public Long getCdcVersion() {
        return cdcVersion;
    }

    public void setProformaInvoice(ProformaInvoice proformaInvoice) {
        this.proformaInvoice = proformaInvoice;
    }

    public ProformaInvoice getProformaInvoice() {
        return proformaInvoice;
    }

    public Integer getId() {
        return this.id;
    }

    public ProformaInvoiceDetail id(Integer id) {
        this.setId(id);
        return this;
    }

    public Double getCbmTotal() {
        return cbmTotal;
    }

    public void setCbmTotal(Double cbmTotal) {
        this.cbmTotal = cbmTotal;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public ProformaInvoiceDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }



    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setCtn(Double ctn) {
        this.ctn = ctn;
    }

    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }

    public void setCbmUnit(Double cbmUnit) {
        this.cbmUnit = cbmUnit;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setShip_date(LocalDate ship_date) {
        this.ship_date = ship_date;
    }


    public String getProductTitle() {
        return productTitle;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getQty() {
        return qty;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getCtn() {
        return ctn;
    }


    public Integer getPcs() {
        return pcs;
    }

    public Double getCbmUnit() {
        return cbmUnit;
    }

    public Double getGrossWeight() {
        return grossWeight;
    }

    public LocalDate getShip_date() {
        return ship_date;
    }

    public ProformaInvoiceDetail proformaInvoice(ProformaInvoice proformaInvoice) {
        this.setProformaInvoice(proformaInvoice);
        return this;
    }



    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProformaInvoiceDetail)) {
            return false;
        }
        return id != null && id.equals(((ProformaInvoiceDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
