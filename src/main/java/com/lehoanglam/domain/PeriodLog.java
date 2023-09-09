package com.yes4all.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A PeriodLog.
 */
@Entity
@Table(name = "period_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PeriodLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "full_date")
    private Instant fullDate;

    @Column(name = "day")
    private Integer day;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PeriodLog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFullDate() {
        return this.fullDate;
    }

    public PeriodLog fullDate(Instant fullDate) {
        this.setFullDate(fullDate);
        return this;
    }

    public void setFullDate(Instant fullDate) {
        this.fullDate = fullDate;
    }

    public Integer getDay() {
        return this.day;
    }

    public PeriodLog day(Integer day) {
        this.setDay(day);
        return this;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return this.month;
    }

    public PeriodLog month(Integer month) {
        this.setMonth(month);
        return this;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return this.year;
    }

    public PeriodLog year(Integer year) {
        this.setYear(year);
        return this;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PeriodLog)) {
            return false;
        }
        return id != null && id.equals(((PeriodLog) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PeriodLog{" +
            "id=" + getId() +
            ", fullDate='" + getFullDate() + "'" +
            ", day=" + getDay() +
            ", month=" + getMonth() +
            ", year=" + getYear() +
            "}";
    }
}
