package com.wed18305.assignment1.web;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import com.wed18305.assignment1.Responses.Response;
import com.wed18305.assignment1.Responses.Response_Booking;
import com.wed18305.assignment1.Responses.Response_CreateBooking;
import com.wed18305.assignment1.Responses.Response_Timeslots;
import com.wed18305.assignment1.Requests.Booking_Request;
import com.wed18305.assignment1.Requests.Delete_Request;
import com.wed18305.assignment1.Requests.Get_Request;
import com.wed18305.assignment1.Requests.Timeslot_Request;
import com.wed18305.assignment1.model.Entity_Booking;
import com.wed18305.assignment1.model.Entity_Schedule;
import com.wed18305.assignment1.services.Booking_Service;
// import com.wed18305.assignment1.services.Service_Service;
import com.wed18305.assignment1.model.Entity_User;
import com.wed18305.assignment1.model.Entity_UserType.UserTypeID;
import com.wed18305.assignment1.services.User_Service;
import com.wed18305.assignment1.tools.Container_Users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "http://localhost:3000")
public class Booking_Controller {

    @Autowired
    private Booking_Service bookingService;
    @Autowired
    private User_Service userService;
    // @Autowired
    // private Service_Service serviceService;

    /**
     * Create New Booking 
     * POST ENDPOINT: http://localhost:8080/api/booking/createBooking
     * INPUT JSON {"startDateTime":"uuuu-MM-dd'T'HH:mmXXXXX", (Format)
     *             "endDateTime"  :"2020-09-07T17:00+10:00",
     *             "user_ids" : ["1", "2"], // Input an Array of Values
     * 
     * Convert Start/EndDateTime to a zero hour UTC format, then save to the server/database.
     * 
     */
    @PostMapping("createBooking")
    public ResponseEntity<Response> createNewBooking(@Valid @RequestBody Booking_Request br, BindingResult result, Principal p) { 
        // Binding validation checks
        if (result.hasErrors()) {
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Adjust the time back to UTC zero hour
        OffsetDateTime sdUTC = br.getStartDate().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime edUTC = br.getEndDate().withOffsetSameInstant(ZoneOffset.UTC);

        // Create a Booking entity using the Booking_Request
        Entity_Booking booking = new Entity_Booking(sdUTC, edUTC);

        // Is a Customer Logged In?
        Optional<Entity_User> loggedInUser = userService.findByUsername(p.getName());
        if (!loggedInUser.isPresent() || !loggedInUser.get().getType().getId().equals(UserTypeID.CUSTOMER.id)) {
             Response response = new Response(false, "ERROR!", "No customer ids provided", null);
             return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        //Get the users
        Container_Users users; //= new Container_Users();
        try {
            users = new Container_Users(userService.findAllById(br.getUserIds()));     
            users.addUser(userService.findById(loggedInUser.get().getId()).get());
        } catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        //Do we have a Customer?
        if(users.getCustomers().isEmpty()){
            Response response = new Response(false, "ERROR!", "No customers making a booking.", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        //Do we have at least one employee
        if(users.getEmployees().isEmpty()){
            Response response = new Response(false, "ERROR!", "No employee ids provided", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Does Booking Fit Into Employee's Work Schedule?
        String busyEmployees = null;

        for (Entity_User employee : users.getEmployees()) {

            // Can Employee Attend Booking?
            boolean canAttend = true;

            // Is an Employee Working During this Time?
            for (Entity_Schedule schedule : employee.getSchedules()) {

                if (br.getStartDate().compareTo(schedule.getStartDateTime()) < 0 && // Does booking start before the schedule's start date?
                    br.getEndDate().compareTo(schedule.getEndDateTime()) > 0) {     // Does booking end after the schedule's end date?
                        canAttend = false;
                }
            }

            // Are Employee's Already Booked During These Times?
            for (Entity_Booking bk : employee.getBookings()) {

                    // Does first booking start before the second?       // Does first booking end before the second or when it starts?                                            
                if (br.getStartDate().isBefore(bk.getStartDateTime()) && br.getEndDate().compareTo(bk.getStartDateTime()) <= 0 ||

                    // Does first booking end after the second?          // Does first booking start after the second or when it ends?    
                   (br.getEndDate()  .isAfter (bk.getEndDateTime()  ) && br.getStartDate().compareTo(bk.getEndDateTime()) >= 0  )) {
                        canAttend = true;
                }
                else {  canAttend = false; }
            }

            // Booking Can't Be Assigned to This Employee
                 if (!canAttend && busyEmployees == null) { busyEmployees = employee.getName(); }
            else if (!canAttend && busyEmployees != null) { busyEmployees += " & " + employee.getName(); }
        }

        // Employee/s can't attend. Print error.
        if (busyEmployees != null) {
            Response response = new Response(false, "ERROR!", busyEmployees + " is unavailble during this time. Cannot attend booking.", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Save new Booking
        Entity_Booking booking1 = null;
        Response_CreateBooking bkgResponse = null;
        try {
            // Save Booking
            booking1 = bookingService.saveOrUpdateBooking(booking);
            Set<Entity_Booking> bookings = new HashSet<Entity_Booking>();
            bookings.add(booking1);
            userService.addBookingsToUsers(users.getUsers(), bookings);
            bkgResponse = new Response_CreateBooking(booking1);
        } catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Check booking result
        if (booking1 == null) {
            Response response = new Response(false, "ERROR!", "booking returned NULL", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        } else {
            Response response = new Response(true, "booking created!", null, bkgResponse);
            return new ResponseEntity<Response>(response, HttpStatus.CREATED);
        }
    }

    /**
     * Get Employee Unavailable Timeslots Based on Their Bookings 
     * POST ENDPOINT: http://localhost:8080/api/booking/getBookedTimeslots
     * INPUT JSON {"date":"uuuu-MM-dd" (Format),
     *             "userID": <String<number>> (Format)}
     */
    @PostMapping("getBookedTimeslots")
    public ResponseEntity<Response> getBookedTimeSlots(@Valid @RequestBody Timeslot_Request tr, BindingResult result) {

        // Was a Valid Employee Passed In?
        Optional<Entity_User> passedInEmployee = userService.findById(tr.getUserID());
        if(!passedInEmployee.isPresent() || !passedInEmployee.get().getType().getId().equals(UserTypeID.EMPLOYEE.id)){
             Response response = new Response(false, "ERROR!", "No valid employee provided.", null);
             return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Retrieve Employee's Booked Times
        Entity_User employee = passedInEmployee.get();
        Response_Timeslots tsResponse = null;
        try {
            Iterable<Entity_Booking> bookings = userService.findBookingsByDate(employee, tr.getDate());

            // Did we Retrieve Any Approved Bookings?
            if (!bookings.iterator().hasNext()) { // No.
                Response response = new Response(false, "WARNING: No approved bookings were found for this employee.", null, null);
                return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
            }
            tsResponse = new Response_Timeslots(bookings);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        Response response = new Response(true, "Booked times found!", null, tsResponse);
        return new ResponseEntity<Response>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Approve Existing Booking 
     * PATCH ENDPOINT: http://localhost:8080/api/booking/approveBooking
     * INPUT JSON {"id":1 }
     */
    @PatchMapping("approveBooking")
    public ResponseEntity<Response> approveBooking(@Valid @RequestBody Get_Request gr, BindingResult result) {

        // Binding validation checks
        if (result.hasErrors()) {
            
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Find Booking by ID
        Optional<Entity_Booking> book = bookingService.findById(gr.getId());
        Response_Booking bkgResponse = null;
        if (book.isPresent()) {
            Response response = new Response(false, "ERROR!", "Booking doesn't exist!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Approve Booking
        try {
            Entity_Booking currentBooking = book.get();
            currentBooking.approveBooking();
            bookingService.saveOrUpdateBooking(currentBooking);
            bkgResponse = new Response_Booking(currentBooking);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", "Booking couldn't be updated!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
         }

        Response response = new Response(true, "Booking approved!", null, bkgResponse);
        return new ResponseEntity<Response>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Deny Existing Booking 
     * PATCH ENDPOINT: http://localhost:8080/api/booking/denyBooking
     * INPUT JSON {"id":1 }
     */
    @PatchMapping("denyBooking")
    public ResponseEntity<Response> denyBooking(@Valid @RequestBody Get_Request gr, BindingResult result) {

        // Binding validation checks
        if (result.hasErrors()) {
            
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Find Booking by ID
        Optional<Entity_Booking> book = bookingService.findById(gr.getId());
        if (book.isPresent()) {
            Response response = new Response(false, "ERROR!", "Booking doesn't exist!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Deny Booking
        Response_Booking bkgResponse = null;
        try {
            Entity_Booking currentBooking = book.get();
            currentBooking.denyBooking();
            bookingService.saveOrUpdateBooking(currentBooking);
            bkgResponse = new Response_Booking(currentBooking);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", "Booking couldn't be updated!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
         }

        Response response = new Response(true, "Booking denied!", null, bkgResponse);
        return new ResponseEntity<Response>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Complete Existing Booking 
     * PATCH ENDPOINT: http://localhost:8080/api/booking/completeBooking
     * INPUT JSON {"id":1 }
     */
    @PatchMapping("completeBooking")
    public ResponseEntity<Response> completeBooking(@Valid @RequestBody Get_Request gr, BindingResult result) {

        // Binding validation checks
        if (result.hasErrors()) {
            
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Find Booking by ID
        Optional<Entity_Booking> book = bookingService.findById(gr.getId());
        if (book.isPresent()) {
            Response response = new Response(false, "ERROR!", "Booking doesn't exist!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Complete Booking
        Response_Booking bkgResponse = null;
        try {
            Entity_Booking currentBooking = book.get();
            currentBooking.completeBooking();
            bookingService.saveOrUpdateBooking(currentBooking);
            bkgResponse = new Response_Booking(currentBooking);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", "Booking couldn't be labelled as complete!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
         }

        Response response = new Response(true, "Booking labelled as completed!", null, bkgResponse);
        return new ResponseEntity<Response>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Cancel Existing Booking 
     * PATCH ENDPOINT: http://localhost:8080/api/booking/cancelBooking
     * INPUT JSON {"id":1 }
     */
    @PatchMapping("cancelBooking")
    public ResponseEntity<Response> cancelBooking(@Valid @RequestBody Get_Request gr, BindingResult result) {

        // Binding validation checks
        if (result.hasErrors()) {
            
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Find Booking by ID
        Optional<Entity_Booking> book = bookingService.findById(gr.getId());
        if (book.isPresent()) {
            Response response = new Response(false, "ERROR!", "Booking doesn't exist!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Is the Booking Going to Run Within 48 Hours?
        Entity_Booking currentBooking = book.get();
        OffsetDateTime twoDaysFromNow = OffsetDateTime.now().plusDays(2);
        if (twoDaysFromNow.compareTo(currentBooking.getStartDateTime()) >= 0) {
            Response response = new Response(false, "ERROR!", "Booking will be run within 48 hours. Cannot be cancelled.", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Attempt to Complete Booking
        Response_Booking bkgResponse = null;
        try {
            
            currentBooking.cancelBooking();
            bookingService.saveOrUpdateBooking(currentBooking);
            bkgResponse = new Response_Booking(currentBooking);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", "Booking couldn't be labelled as cancelled!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
         }

        Response response = new Response(true, "Booking labelled as cancelled!", null, bkgResponse);
        return new ResponseEntity<Response>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Get All Bookings (Admins can view all bookings, if they're not more than 7 days old)
     * GET ENDPOINT: http://localhost:8080/api/booking/getAllBookings
     */
    @GetMapping("getAllBookings")
    public ResponseEntity<Response> getAllBookings(Principal p) {

        // Make sure the logged in user exists
        Optional<Entity_User> user = userService.findByUsername(p.getName());
        if(user.isPresent() == false){
            //shouldn't be able to get here but just incase
            Response response = new Response(false, "ERROR!", "Nobody's logged in!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }
        
        // Get All Bookings
        Iterable<Entity_Booking> bookings =  null;
        Response_Booking bkgResponse = null;
        try {
            bookings = bookingService.findAll();
            bkgResponse = new Response_Booking(bookings);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Any Bookings Found?
        if (!bookings.iterator().hasNext()) { // If size < 1
            Response response = new Response(false, "ERROR!", "No bookings within the repository!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        } else {
            Response response = new Response(true, "Bookings found!", null, bkgResponse);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
    }

    /**
     * Get An Employee's Bookings (That have been approved.)
     * GET ENDPOINT: http://localhost:8080/api/booking/getUserBookings
     */
    @GetMapping("getUserBookings")
    public ResponseEntity<Response> getUserBookings(Principal p) {

        // Make sure the logged in user exists
        Optional<Entity_User> user = userService.findByUsername(p.getName());
        if(user.isPresent() == false){
            //shouldn't be able to get here but just incase
            Response response = new Response(false, "ERROR!", "Nobody's logged in!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Get the Employee's Bookings
        Entity_User curUser = user.get();
        Iterable<Entity_Booking> bookings =  null;
        Response_Booking bkgResponse = null;
        try {
            // Get Certain Bookings Based on UserType
                 if (isEmployee(curUser)) { bookings = userService.findApprovedUserBookings(curUser.getId()); }
            else if (isCustomer(curUser)) { bookings = userService.findUserBookings        (curUser.getId()); }
        
            bkgResponse = new Response_Booking(bookings);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Any Bookings Found?
        if (!bookings.iterator().hasNext()) { // If size < 1
            Response response = new Response(false, "ERROR!", "User has no bookings associated with them!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        } else {
            Response response = new Response(true, "Bookings found!", null, bkgResponse);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
    }

    /**
     * Get All Upcoming Bookings
     * GET ENDPOINT: http://localhost:8080/api/booking/getUpcomingBookings
     */
    @GetMapping("getUpcomingBookings")
    public ResponseEntity<Response> getUpcomingBookings(Principal p) {

        // Make sure the logged in user exists
        Optional<Entity_User> user = userService.findByUsername(p.getName());
        if(user.isPresent() == false){
            //shouldn't be able to get here but just incase
            Response response = new Response(false, "ERROR!", "Nobody's logged in!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Get All Bookings
        Entity_User curUser = user.get();
        Iterable<Entity_Booking> bookings =  null;
        Response_Booking bkgResponse = null;
        // try {
            // Get Certain Upcoming Bookings Based on UserType
                 if (isAdmin(curUser))    { bookings = bookingService.findAllUpcoming();                          } 
            else if (isEmployee(curUser)) { bookings = userService.findApprovedUpcomingBookings(curUser.getId()); }
            else if (isCustomer(curUser)) { bookings = userService.findUpcomingUserBookings    (curUser.getId()); }
        
            bkgResponse = new Response_Booking(bookings);
        // }
        // catch (Exception e) {
        //     Response response = new Response(false, "ERROR!", e.getMessage(), null);
        //     return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        // }

        // Any Bookings Found?
        if (!bookings.iterator().hasNext()) { // If size < 1
            Response response = new Response(false, "ERROR!", "User has no upcoming bookings associated with them!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        } else {
            Response response = new Response(true, "Bookings found!", null, bkgResponse);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
    }

    /**
     * Get All Completed Bookings
     * GET ENDPOINT: http://localhost:8080/api/booking/getCompletedBookings
     */
    @GetMapping("getCompletedBookings")
    public ResponseEntity<Response> getCompletedBookings(Principal p) {

        // Make sure the logged in user exists
        Optional<Entity_User> user = userService.findByUsername(p.getName());
        if(user.isPresent() == false){
            //shouldn't be able to get here but just incase
            Response response = new Response(false, "ERROR!", "Nobody's logged in!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Get All Bookings
        Entity_User curUser = user.get();
        Iterable<Entity_Booking> bookings =  null;
        Response_Booking bkgResponse = null;
        try {
            // Get Certain Completed Bookings Based on UserType
                 if (isAdmin(curUser))    { bookings = bookingService.findAllCompleted();                          }
            else if (isEmployee(curUser)) { bookings = userService.findApprovedCompletedBookings(curUser.getId()); }
            else if (isCustomer(curUser)) { bookings = userService.findCompletedUserBookings    (curUser.getId()); }
        
            bkgResponse = new Response_Booking(bookings);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Any Bookings Found?
        if (!bookings.iterator().hasNext()) { // If size < 1
            Response response = new Response(false, "ERROR!", "User has no completed associated with them!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        } else {
            Response response = new Response(true, "Bookings found!", null, bkgResponse);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
    }

    /**
     * Delete Existing Booking 
     * POST ENDPOINT: http://localhost:8080/api/booking/deleteBooking
     * INPUT JSON {"input":[1] }
     */
    @DeleteMapping("deleteBooking")
    public ResponseEntity<Response> deleteBooking(@Valid @RequestBody Delete_Request dr, BindingResult result) {

        // Binding validation checks
        if (result.hasErrors()) {
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            bookingService.deleteManyById(dr.getLong());
        }
        catch (IllegalArgumentException e) {
            Response response = new Response(false, "ERROR!", "Id of booking is null!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            Response response = new Response(false, "ERROR!", "Unable to delete booking!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        Response response = new Response(true, "Booking deleted!", null, null);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    // Helper Methods
    private boolean isAdmin(Entity_User user)    { return user.getType().getId().equals(UserTypeID.ADMIN.id);    }
    private boolean isEmployee(Entity_User user) { return user.getType().getId().equals(UserTypeID.EMPLOYEE.id); }
    private boolean isCustomer(Entity_User user) { return user.getType().getId().equals(UserTypeID.CUSTOMER.id); }
    
}