package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A IssueItem.
 */
@Entity
@Table(name = "issue_item")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IssueItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "sale_order_number")
    private String saleOrderNumber;

    @Column(name = "confirmed_qty")
    private Integer confirmedQty;

    @Column(name = "actual_exported_qty")
    private Integer actualExportedQty;

    @Column(name = "remaining_qty")
    private Integer remainingQty;

    @Column(name = "note")
    private String note;

    @Column(name = "sku")
    private String sku;

    @OneToOne
    @JoinColumn(unique = true)
    private Product product;

    @ManyToOne
    @JsonIgnoreProperties(value = { "issueItems", "adjustment", "warehouseFrom", "warehouseTo" }, allowSetters = true)
    private IssueNote issueNote;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public IssueItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSaleOrderNumber() {
        return this.saleOrderNumber;
    }

    public IssueItem saleOrderNumber(String saleOrderNumber) {
        this.setSaleOrderNumber(saleOrderNumber);
        return this;
    }

    public void setSaleOrderNumber(String saleOrderNumber) {
        this.saleOrderNumber = saleOrderNumber;
    }

    public Integer getConfirmedQty() {
        return this.confirmedQty;
    }

    public IssueItem confirmedQty(Integer confirmedQty) {
        this.setConfirmedQty(confirmedQty);
        return this;
    }

    public void setConfirmedQty(Integer confirmedQty) {
        this.confirmedQty = confirmedQty;
    }

    public Integer getActualExportedQty() {
        return this.actualExportedQty;
    }

    public IssueItem actualExportedQty(Integer actualExportedQty) {
        this.setActualExportedQty(actualExportedQty);
        return this;
    }

    public void setActualExportedQty(Integer actualExportedQty) {
        this.actualExportedQty = actualExportedQty;
    }

    public Integer getRemainingQty() {
        return this.remainingQty;
    }

    public IssueItem remainingQty(Integer remainingQty) {
        this.setRemainingQty(remainingQty);
        return this;
    }

    public void setRemainingQty(Integer remainingQty) {
        this.remainingQty = remainingQty;
    }

    public String getNote() {
        return this.note;
    }

    public IssueItem note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSku() {
        return this.sku;
    }

    public IssueItem sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Product getProduct() {
        return this.product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public IssueItem product(Product product) {
        this.setProduct(product);
        return this;
    }

    public IssueNote getIssueNote() {
        return this.issueNote;
    }

    public void setIssueNote(IssueNote issueNote) {
        this.issueNote = issueNote;
    }

    public IssueItem issueNote(IssueNote issueNote) {
        this.setIssueNote(issueNote);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssueItem)) {
            return false;
        }
        return id != null && id.equals(((IssueItem) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IssueItem{" +
            "id=" + getId() +
            ", saleOrderNumber='" + getSaleOrderNumber() + "'" +
            ", confirmedQty=" + getConfirmedQty() +
            ", actualExportedQty=" + getActualExportedQty() +
            ", remainingQty=" + getRemainingQty() +
            ", note='" + getNote() + "'" +
            ", sku='" + getSku() + "'" +
            "}";
    }
}
