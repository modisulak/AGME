package com.wed18305.assignment1.Requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wed18305.assignment1.model.User;
import com.wed18305.assignment1.services.User_Service;

public class Booking_Request {

    /// Variables
    // @NotBlank(message = "A StartDate is required.")
    protected LocalDateTime startDateTime;
    // @NotBlank(message = "A EndDate is required.")
    protected LocalDateTime endDateTime;
    // @NotBlank(message = "Please assign a customer to this booking.")
    protected Long[] customer_ids;
    // @NotBlank(message = "Please assign a worker to this booking.")
    protected Long[] worker_ids;
    @NotNull(message = "request ID is required.")
    protected Long requestID;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // "Day, Month, Year, Hour, Minute."
    
    //Constructors
    public Booking_Request(String startDateTime,
                           String endDateTime,
                           Long[] customer_ids,
                           Long[] worker_ids,
                           Long requestID) {
        this.startDateTime  = LocalDateTime.parse(startDateTime, formatter);
        this.endDateTime    = LocalDateTime.parse(endDateTime, formatter);
        this.customer_ids   = customer_ids;
        this.worker_ids     = worker_ids;
        this.requestID      = requestID;
    }

    // Getters
    public LocalDateTime getStartDate()      { return startDateTime;     }
    public LocalDateTime getEndDate()        { return endDateTime;       }
    public Long[] getCustomerIds()           { return customer_ids;      }
    public Long[] getWorkerIds()             { return worker_ids;        }
    public Long getRequestID()               { return requestID;         }
}