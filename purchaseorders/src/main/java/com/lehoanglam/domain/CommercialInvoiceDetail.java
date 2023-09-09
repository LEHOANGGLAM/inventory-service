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
@Table(name = "commercial_invoice_detail")
public class CommercialInvoiceDetail implements Serializable {

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

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;


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
    @Column(name = "a_sin")
    private String aSin;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "commercial_invoice_id", referencedColumnName = "id")
    private CommercialInvoice commercialInvoice;


    public String getaSin() {
        return aSin;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
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

    public void setCommercialInvoice(CommercialInvoice commercialInvoice) {
        this.commercialInvoice = commercialInvoice;
    }

    public CommercialInvoice getCommercialInvoice() {
        return commercialInvoice;
    }

    public Integer getId() {
        return this.id;
    }

    public CommercialInvoiceDetail id(Integer id) {
        this.setId(id);
        return this;
    }



    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public CommercialInvoiceDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }



    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
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




    public String getProductTitle() {
        return productTitle;
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



        public CommercialInvoiceDetail commercialInvoice(CommercialInvoice commercialInvoice) {
        this.setCommercialInvoice(commercialInvoice);
        return this;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommercialInvoiceDetail)) {
            return false;
        }
        return id != null && id.equals(((CommercialInvoiceDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
