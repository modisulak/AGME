package com.wed18305.assignment1.Requests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.validation.constraints.NotNull;

public class Booking_Request {

    /// Variables
    // @NotBlank(message = "A StartDate is required.")
    protected LocalDateTime startDateTime;
    // @NotBlank(message = "A EndDate is required.")
    protected LocalDateTime endDateTime;
    // @NotBlank(message = "Please assign a customer to this booking.")
    protected Long[] customer_ids;
    // @NotBlank(message = "Please assign a worker to this booking.")
    protected Long[] employees_ids;

    protected Long service_id;
    @NotNull(message = "request ID is required.")
    protected Long requestID;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // "Day, Month, Year, Hour, Minute."
    
    //Constructors
    public Booking_Request(String startDateTime,
                           String endDateTime,
                           Long[] customer_ids,
                           Long[] employee_ids,
                           Long service_id,
                           Long requestID) {
        this.startDateTime  = LocalDateTime.parse(startDateTime, formatter);
        this.endDateTime    = LocalDateTime.parse(endDateTime, formatter);
        this.customer_ids   = customer_ids;
        this.employees_ids  = employee_ids;
        this.service_id     = service_id;
        this.requestID      = requestID;
    }

    // Getters
    public LocalDateTime getStartDate()      { return startDateTime;     }
    public LocalDateTime getEndDate()        { return endDateTime;       }
    public Long[] getCustomerIds()           { return customer_ids;      }
    public Long[] getEmployeeIds()           { return employees_ids;     }
    public Long getServiceId()               { return service_id;        }
    public Long getRequestID()               { return requestID;         }
}