package com.yes4all.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A WarehouseInfo.
 */
@Entity
@Table(name = "warehouse_info")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WarehouseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "wip")
    private Integer wip;

    @Column(name = "ovs_location")
    private Integer ovsLocation;

    @Column(name = "pallet")
    private Integer pallet;

    @Column(name = "pku_location")
    private Integer pkuLocation;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public WarehouseInfo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWip() {
        return this.wip;
    }

    public WarehouseInfo wip(Integer wip) {
        this.setWip(wip);
        return this;
    }

    public void setWip(Integer wip) {
        this.wip = wip;
    }

    public Integer getOvsLocation() {
        return this.ovsLocation;
    }

    public WarehouseInfo ovsLocation(Integer ovsLocation) {
        this.setOvsLocation(ovsLocation);
        return this;
    }

    public void setOvsLocation(Integer ovsLocation) {
        this.ovsLocation = ovsLocation;
    }

    public Integer getPallet() {
        return this.pallet;
    }

    public WarehouseInfo pallet(Integer pallet) {
        this.setPallet(pallet);
        return this;
    }

    public void setPallet(Integer pallet) {
        this.pallet = pallet;
    }

    public Integer getPkuLocation() {
        return this.pkuLocation;
    }

    public WarehouseInfo pkuLocation(Integer pkuLocation) {
        this.setPkuLocation(pkuLocation);
        return this;
    }

    public void setPkuLocation(Integer pkuLocation) {
        this.pkuLocation = pkuLocation;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WarehouseInfo)) {
            return false;
        }
        return id != null && id.equals(((WarehouseInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WarehouseInfo{" +
            "id=" + getId() +
            ", wip=" + getWip() +
            ", ovsLocation=" + getOvsLocation() +
            ", pallet=" + getPallet() +
            ", pkuLocation=" + getPkuLocation() +
            "}";
    }
}
