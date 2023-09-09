package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "booking_purchase_order")
public class BookingPurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;


    @Column(name = "sku")
    private String sku;


    @Column(name = "po_number")
    private String poNumber;
    @Column(name = "asin")
    private String aSin;

    @Column(name = "title")
    private String title;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "quantity_ctns")
    private Long quantityCtns;

    @Column(name = "fob_price")
    private Double fobPrice;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "cbm")
    private Double cbm;

    @Column(name = "ship_location")
    private String shipLocation;

    @Column(name = "supplier")
    private String supplier;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"booking"}, allowSetters = true)
    @JoinColumn(name = "booking_id", referencedColumnName = "id")
    private Booking booking;

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getShipLocation() {
        return shipLocation;
    }

    public void setShipLocation(String shipLocation) {
        this.shipLocation = shipLocation;
    }

    public Double getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public Double getCbm() {
        return cbm;
    }

    public void setCbm(Double cbm) {
        this.cbm = cbm;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
    public BookingPurchaseOrder booking(Booking booking) {
        this.setBooking(booking);
        return this;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void setQuantityCtns(Long quantityCtns) {
        this.quantityCtns = quantityCtns;
    }

    public void setFobPrice(Double fobPrice) {
        this.fobPrice = fobPrice;
    }


    public Integer getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public String getaSin() {
        return aSin;
    }

    public String getTitle() {
        return title;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getQuantityCtns() {
        return quantityCtns;
    }

    public Double getFobPrice() {
        return fobPrice;
    }

}
