package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AdjustmentItem.
 */
@Entity
@Table(name = "adjustment_item")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdjustmentItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "asin")
    private String asin;

    @Column(name = "inbound_code")
    private String inboundCode;

    @Column(name = "outbound_code")
    private String outboundCode;

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "sku")
    private String sku;

    @Column(name = "wip_quantity_before")
    private Integer wipQuantityBefore;

    @Column(name = "pku_quantity_before")
    private Integer pkuQuantityBefore;

    @Column(name = "total_quantity_before")
    private Integer totalQuantityBefore;

    @Column(name = "wip_quantity_after")
    private Integer wipQuantityAfter;

    @Column(name = "pku_quantity_after")
    private Integer pkuQuantityAfter;

    @Column(name = "total_quantity_after")
    private Integer totalQuantityAfter;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = { "issueNote", "receiptNote", "adjustmentItems" }, allowSetters = true)
    private Adjustment adjustment;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AdjustmentItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAsin() {
        return this.asin;
    }

    public AdjustmentItem asin(String asin) {
        this.setAsin(asin);
        return this;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getInboundCode() {
        return this.inboundCode;
    }

    public AdjustmentItem inboundCode(String inboundCode) {
        this.setInboundCode(inboundCode);
        return this;
    }

    public void setInboundCode(String inboundCode) {
        this.inboundCode = inboundCode;
    }

    public String getOutboundCode() {
        return this.outboundCode;
    }

    public AdjustmentItem outboundCode(String outboundCode) {
        this.setOutboundCode(outboundCode);
        return this;
    }

    public void setOutboundCode(String outboundCode) {
        this.outboundCode = outboundCode;
    }

    public String getProductTitle() {
        return this.productTitle;
    }

    public AdjustmentItem productTitle(String productTitle) {
        this.setProductTitle(productTitle);
        return this;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getSku() {
        return this.sku;
    }

    public AdjustmentItem sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getWipQuantityBefore() {
        return this.wipQuantityBefore;
    }

    public AdjustmentItem wipQuantityBefore(Integer wipQuantityBefore) {
        this.setWipQuantityBefore(wipQuantityBefore);
        return this;
    }

    public void setWipQuantityBefore(Integer wipQuantityBefore) {
        this.wipQuantityBefore = wipQuantityBefore;
    }

    public Integer getPkuQuantityBefore() {
        return this.pkuQuantityBefore;
    }

    public AdjustmentItem pkuQuantityBefore(Integer pkuQuantityBefore) {
        this.setPkuQuantityBefore(pkuQuantityBefore);
        return this;
    }

    public void setPkuQuantityBefore(Integer pkuQuantityBefore) {
        this.pkuQuantityBefore = pkuQuantityBefore;
    }

    public Integer getTotalQuantityBefore() {
        return this.totalQuantityBefore;
    }

    public AdjustmentItem totalQuantityBefore(Integer totalQuantityBefore) {
        this.setTotalQuantityBefore(totalQuantityBefore);
        return this;
    }

    public void setTotalQuantityBefore(Integer totalQuantityBefore) {
        this.totalQuantityBefore = totalQuantityBefore;
    }

    public Integer getWipQuantityAfter() {
        return this.wipQuantityAfter;
    }

    public AdjustmentItem wipQuantityAfter(Integer wipQuantityAfter) {
        this.setWipQuantityAfter(wipQuantityAfter);
        return this;
    }

    public void setWipQuantityAfter(Integer wipQuantityAfter) {
        this.wipQuantityAfter = wipQuantityAfter;
    }

    public Integer getPkuQuantityAfter() {
        return this.pkuQuantityAfter;
    }

    public AdjustmentItem pkuQuantityAfter(Integer pkuQuantityAfter) {
        this.setPkuQuantityAfter(pkuQuantityAfter);
        return this;
    }

    public void setPkuQuantityAfter(Integer pkuQuantityAfter) {
        this.pkuQuantityAfter = pkuQuantityAfter;
    }

    public Integer getTotalQuantityAfter() {
        return this.totalQuantityAfter;
    }

    public AdjustmentItem totalQuantityAfter(Integer totalQuantityAfter) {
        this.setTotalQuantityAfter(totalQuantityAfter);
        return this;
    }

    public void setTotalQuantityAfter(Integer totalQuantityAfter) {
        this.totalQuantityAfter = totalQuantityAfter;
    }

    public Adjustment getAdjustment() {
        return this.adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        this.adjustment = adjustment;
    }

    public AdjustmentItem adjustment(Adjustment adjustment) {
        this.setAdjustment(adjustment);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdjustmentItem)) {
            return false;
        }
        return id != null && id.equals(((AdjustmentItem) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdjustmentItem{" +
            "id=" + getId() +
            ", asin='" + getAsin() + "'" +
            ", inboundCode='" + getInboundCode() + "'" +
            ", outboundCode='" + getOutboundCode() + "'" +
            ", productTitle='" + getProductTitle() + "'" +
            ", sku='" + getSku() + "'" +
            ", wipQuantityBefore=" + getWipQuantityBefore() +
            ", pkuQuantityBefore=" + getPkuQuantityBefore() +
            ", totalQuantityBefore=" + getTotalQuantityBefore() +
            ", wipQuantityAfter=" + getWipQuantityAfter() +
            ", pkuQuantityAfter=" + getPkuQuantityAfter() +
            ", totalQuantityAfter=" + getTotalQuantityAfter() +
            "}";
    }
}
