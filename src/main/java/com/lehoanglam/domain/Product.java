package com.yes4all.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Product.
 */
@Entity
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "sku")
    private String sku;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "company")
    private String company;

    @Column(name = "asin")
    private String asin;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Product id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductTitle() {
        return this.productTitle;
    }

    public Product productTitle(String productTitle) {
        this.setProductTitle(productTitle);
        return this;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getSku() {
        return this.sku;
    }

    public Product sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Product createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompany() {
        return this.company;
    }

    public Product company(String company) {
        this.setCompany(company);
        return this;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAsin() {
        return this.asin;
    }

    public Product asin(String asin) {
        this.setAsin(asin);
        return this;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        return id != null && id.equals(((Product) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Product{" +
            "id=" + getId() +
            ", productTitle='" + getProductTitle() + "'" +
            ", sku='" + getSku() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", company='" + getCompany() + "'" +
            ", asin='" + getAsin() + "'" +
            "}";
    }
}
