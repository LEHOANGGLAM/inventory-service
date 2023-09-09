package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * A PurchaseOrders.
 */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;


    @NotNull
    @Column(name = "po_number")
    private String poNumber;


    @Column(name = "vendor_id")
    private String vendorId;

    @Column(name = "country")
    private String country;

    @Column(name = "fulfillment_center")
    private String fulfillmentCenter;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "channel")
    private Integer channel;

    @Column(name = "total_item")
    private Long totalItem;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "booking_number")
    private String bookingNumber;

    @Column(name = "expected_ship_date")
    private LocalDate expectedShipDate;

    @Column(name = "actual_ship_date")
    private LocalDate actualShipDate;

    @Column(name = "etd")
    private LocalDate etd;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "port_of_loading")
    private String portOfLoading;

    @Column(name = "atd")
    private LocalDate atd;

    @Column(name = "ship_window_start")
    private LocalDate shipWindowStart;

    @Column(name = "ship_window_end")
    private LocalDate shipWindowEnd;

    @Column(name = "us_broker")
    private String usBroker;

    @Column(name = "status")
    private Integer status;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "cdc_version")
    private Long cdcVersion;

    @Column(name = "from_id")
    private Integer fromId;



    @OneToMany(mappedBy = "purchaseOrders", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrders"}, allowSetters = true)
    private Set<PurchaseOrdersDetail> purchaseOrdersDetail = new HashSet<>();

    @OneToOne(mappedBy = "purchaseOrders", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ProformaInvoice proformaInvoice ;


    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getFromId() {
        return fromId;
    }



    public Integer getId() {
        return this.id;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public String getCreatedBy() {
        return createdBy;
    }


    public PurchaseOrders id(Integer id) {
        this.setId(id);
        return this;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public void setAtd(LocalDate atd) {
        this.atd = atd;
    }

    public void setShipWindowStart(LocalDate shipWindowStart) {
        this.shipWindowStart = shipWindowStart;
    }

    public void setShipWindowEnd(LocalDate shipWindowEnd) {
        this.shipWindowEnd = shipWindowEnd;
    }

    public String getPortOfLoading() {
        return portOfLoading;
    }

    public LocalDate getAtd() {
        return atd;
    }

    public LocalDate getShipWindowStart() {
        return shipWindowStart;
    }

    public LocalDate getShipWindowEnd() {
        return shipWindowEnd;
    }


    public Set<PurchaseOrdersDetail> getPurchaseOrdersDetail() {
        return purchaseOrdersDetail;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = updatedDate = new Date().toInstant();
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrders)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrders) o).id);
    }


    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setFulfillmentCenter(String fulfillmentCenter) {
        this.fulfillmentCenter = fulfillmentCenter;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public void setTotalItem(Long totalItem) {
        this.totalItem = totalItem;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }


    public void setUsBroker(String usBroker) {
        this.usBroker = usBroker;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDate getExpectedShipDate() {
        return expectedShipDate;
    }

    public void setExpectedShipDate(LocalDate expectedShipDate) {
        this.expectedShipDate = expectedShipDate;
    }

    public void setActualShipDate(LocalDate actualShipDate) {
        this.actualShipDate = actualShipDate;
    }

    public void setEtd(LocalDate etd) {
        this.etd = etd;
    }

    public void setEta(LocalDate eta) {
        this.eta = eta;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
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

    public LocalDate getActualShipDate() {
        return actualShipDate;
    }

    public LocalDate getEtd() {
        return etd;
    }

    public LocalDate getEta() {
        return eta;
    }

    public Instant getCreatedDate() {
        return createdDate;
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

    public String getDeletedBy() {
        return deletedBy;
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

    public String getPoNumber() {
        return poNumber;
    }


    public String getVendorId() {
        return vendorId;
    }

    public String getCountry() {
        return country;
    }

    public String getFulfillmentCenter() {
        return fulfillmentCenter;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public Integer getChannel() {
        return channel;
    }

    public Long getTotalItem() {
        return totalItem;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }


    public String getUsBroker() {
        return usBroker;
    }

    public Integer getStatus() {
        return status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }


    public Boolean getDeleted() {
        return isDeleted;
    }


    public String getDeleteBy() {
        return deletedBy;
    }

    public Long getCdcVersion() {
        return cdcVersion;
    }

    public void setPurchaseOrdersDetail(Set<PurchaseOrdersDetail> purchaseOrdersDetail) {
        if (this.purchaseOrdersDetail != null) {
            this.purchaseOrdersDetail.forEach(i -> i.setPurchaseOrders(null));
        }
        if (purchaseOrdersDetail != null) {
            purchaseOrdersDetail.forEach(i -> i.purchaseOrders(this));
        }
        this.purchaseOrdersDetail = purchaseOrdersDetail;
    }

    public ProformaInvoice getProformaInvoice() {
        return proformaInvoice;
    }

    public void setProformaInvoice(ProformaInvoice proformaInvoice) {
        this.proformaInvoice = proformaInvoice;
    }
}
