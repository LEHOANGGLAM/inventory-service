package com.yes4all.service.impl;

import com.yes4all.web.rest.payload.RestResponse;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service("requestHolderService")
public class RequestHolderService {

    private String ticketId;
    private String serviceId;
    private Object restRequest;
    private RestResponse restResponse;
    private long beginRequest;
    private long processTime;
    private Date requestDate;

    public String getTicketId() {
        if (ticketId == null) {
            ticketId = UUID.randomUUID().toString();
        }
        return ticketId;
    }

    public long calculateProcessTime() {
        if (this.processTime > 0) return processTime;
        long responseTime = System.nanoTime() - getBeginRequest();
        setProcessTime(responseTime / 1000000);
        return responseTime;
    }
}
