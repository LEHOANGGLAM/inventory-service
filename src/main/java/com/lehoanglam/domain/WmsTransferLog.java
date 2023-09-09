package com.yes4all.domain;

import com.yes4all.common.utils.DateUtil;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "wms_transfer_log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class WmsTransferLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "type")
    private String type;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "transaction_date")
    private Instant transactionDate;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    protected void onCreated() {
        transactionDate = DateUtil.currentInstantUTC();
    }
}
