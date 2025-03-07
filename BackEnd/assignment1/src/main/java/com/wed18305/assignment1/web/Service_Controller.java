package com.wed18305.assignment1.web;

import javax.validation.Valid;

import com.wed18305.assignment1.Requests.Service_Request;
import com.wed18305.assignment1.Responses.Response;
import com.wed18305.assignment1.model.Entity_Service;
import com.wed18305.assignment1.services.Service_Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
@CrossOrigin(origins = "http://localhost:3000")
public class Service_Controller {

    @Autowired
    public Service_Service serviceService;

    /**
     * Create New Service 
     * <p>
     * POST ENDPOINT: http://localhost:8080/api/service/createService
     * <p>
     * INPUT JSON {"name" : "Freddie's Falaf"}
     * OR
     * INPUT JSON {"name" : "Freddie's Falaf", "length" : "15"} //length must be an integer
     */
    @PostMapping("createService")
    public ResponseEntity<Response> createNewService(@Valid @RequestBody Service_Request sr, BindingResult result) {
        // Binding validation checks
        if (result.hasErrors()) {
            Response response = new Response(false, "ERROR!", result.getFieldErrors(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Save new Service
        Entity_Service service1 = null;
        try {

            // Create a Service entity using the Service_Request
            Entity_Service service;
            if(sr.getLength() == 0){
                service = new Entity_Service(sr.getName());
            }else{
                service = new Entity_Service(sr.getName(),sr.getLength());
            }

            // Save Service
            service1 = serviceService.saveOrUpdateService(service);

        } catch (Exception e) {
            
            Response response = new Response(false, "ERROR!", e.getMessage(), null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }

        // Check Service result
        if (service1 == null) {

            Response response = new Response(false, "ERROR!", "Service returned NULL", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);

        } else {

            Response response = new Response(true, "Service created!", null, service1);
            return new ResponseEntity<Response>(response, HttpStatus.CREATED);
        }
    }


    /**
     * anyone registered can call this endpoint.
     * @return an JsonArray of all services
     */
    @GetMapping("getServices")
    public ResponseEntity<Response> getServices() {
        Iterable<Entity_Service> services = serviceService.findAll();
        if(services.iterator().hasNext() == false){
            //failure
            Response response = new Response(false, "ERROR!", "No services found!", null);
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }
        //Success
        Response response = new Response(true, "Services returned!", null, services);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }
}