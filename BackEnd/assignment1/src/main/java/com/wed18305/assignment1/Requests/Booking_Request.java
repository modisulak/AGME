package com.wed18305.assignment1.Requests;

import java.time.OffsetDateTime;
// import javax.validation.constraints.NotNull;

import com.wed18305.assignment1.config.DateTimeStatic;

public class Booking_Request {

    /// Variables
    // @NotBlank(message = "A StartDate is required.")
    protected OffsetDateTime startDateTime;
    // @NotBlank(message = "A EndDate is required.")
    protected OffsetDateTime endDateTime;
    // // @NotBlank(message = "Please assign a customer to this booking.")
    // protected Long[] customer_ids;
    // // @NotBlank(message = "Please assign a worker to this booking.")
    // protected Long[] employees_ids;
    protected Long[] user_ids;

    //Constructors
    public Booking_Request(String startDateTime,
                           String endDateTime,
                        //    Long[] customer_ids,
                        //    Long[] employee_ids,
                           Long[] user_ids) {
        this.startDateTime  = OffsetDateTime.parse(startDateTime, DateTimeStatic.getFormatter());
        this.endDateTime    = OffsetDateTime.parse(endDateTime, DateTimeStatic.getFormatter());
        // this.customer_ids   = customer_ids;
        // this.employees_ids  = employee_ids;
        this.user_ids  = user_ids;
    }

    // Getters
    public OffsetDateTime getStartDate()      { return startDateTime;     }
    public OffsetDateTime getEndDate()        { return endDateTime;       }
    // public Long[] getCustomerIds()           { return customer_ids;      }
    // public Long[] getEmployeeIds()           { return employees_ids;     }
    public Long[] getUserIds()           { return user_ids;     }
}