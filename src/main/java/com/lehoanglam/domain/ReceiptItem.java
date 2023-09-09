package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ReceiptItem.
 */
@Entity
@Table(name = "receipt_item")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReceiptItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "confirmed_qty")
    private Integer confirmedQty;

    @Column(name = "actual_imported_qty")
    private Integer actualImportedQty;

    @Column(name = "difference_qty")
    private Integer differenceQty;

    @Column(name = "note")
    private String note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "receiptItems", "warehouse", "adjustment" }, allowSetters = true)
    private ReceiptNote receiptNote;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ReceiptItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public ReceiptItem sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getConfirmedQty() {
        return this.confirmedQty;
    }

    public ReceiptItem transferredQty(Integer transferredQty) {
        this.setConfirmedQty(transferredQty);
        return this;
    }

    public void setConfirmedQty(Integer confirmedQty) {
        this.confirmedQty = confirmedQty;
    }

    public Integer getActualImportedQty() {
        return this.actualImportedQty;
    }

    public ReceiptItem actualImportedQty(Integer actualImportedQty) {
        this.setActualImportedQty(actualImportedQty);
        return this;
    }

    public void setActualImportedQty(Integer actualImportedQty) {
        this.actualImportedQty = actualImportedQty;
    }

    public Integer getDifferenceQty() {
        return this.differenceQty;
    }

    public ReceiptItem differenceQty(Integer differenceQty) {
        this.setDifferenceQty(differenceQty);
        return this;
    }

    public void setDifferenceQty(Integer differenceQty) {
        this.differenceQty = differenceQty;
    }

    public String getNote() {
        return this.note;
    }

    public ReceiptItem note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Product getProduct() {
        return this.product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ReceiptItem product(Product product) {
        this.setProduct(product);
        return this;
    }

    public ReceiptNote getReceiptNote() {
        return this.receiptNote;
    }

    public void setReceiptNote(ReceiptNote receiptNote) {
        this.receiptNote = receiptNote;
    }

    public ReceiptItem receiptNote(ReceiptNote receiptNote) {
        this.setReceiptNote(receiptNote);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReceiptItem)) {
            return false;
        }
        return id != null && id.equals(((ReceiptItem) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReceiptItem{" +
            "id=" + getId() +
            ", sku='" + getSku() + "'" +
            ", transferredQty=" + getConfirmedQty() +
            ", countedQty=" + getActualImportedQty() +
            ", differenceQty=" + getDifferenceQty() +
            ", note='" + getNote() + "'" +
            "}";
    }
}
