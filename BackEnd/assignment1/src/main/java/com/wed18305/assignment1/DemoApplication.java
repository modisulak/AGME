package com.wed18305.assignment1;

import com.wed18305.assignment1.model.Entity_Service;
import com.wed18305.assignment1.model.Entity_User;
import com.wed18305.assignment1.model.Entity_UserType;
import com.wed18305.assignment1.repositories.Booking_Repository;
import com.wed18305.assignment1.repositories.Schedule_Repository;
import com.wed18305.assignment1.repositories.Service_Repository;
import com.wed18305.assignment1.repositories.UserType_Repository;
import com.wed18305.assignment1.repositories.User_Repository;
import com.wed18305.assignment1.services.User_Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@SpringBootApplication
public class DemoApplication {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class);
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
    	return new HttpSessionEventPublisher();
	}

	// Comment out CommandLineRunner when not using it for testing
	@Bean

	public CommandLineRunner demo(User_Repository UserRepository, 
								  UserType_Repository TypeRepository, 
								  Service_Repository ServiceRepository, 
								  Booking_Repository BookingRepository,
								  Schedule_Repository ScheduleRepository,
								  User_Service UsrService) {

		return (args) -> {
			//save the three types
			Entity_UserType admin = new Entity_UserType("admin");
			Entity_UserType employee = new Entity_UserType("employee");
			Entity_UserType customer = new Entity_UserType("customer");
			TypeRepository.save(admin);//1
			TypeRepository.save(employee);//2
			TypeRepository.save(customer);//3
 
			Entity_User jack = new Entity_User("Jack", "Jacky", "1234", "0000000000", customer);
			Entity_User chloe = new Entity_User("Chloe", "O'Brian", "1234", "0000000000", customer);
			Entity_User kim = new Entity_User("Kim", "Bauer", "1234", "0000000000", customer);
			Entity_User david = new Entity_User("David", "Palmer", "1234", "0000000000", admin);
			Entity_User michelle = new Entity_User("Michelle", "Dessler", "1234", "0000000000", employee);
			Entity_User leslie = new Entity_User("Leslie", "Messler", "1234", "0000000000", employee);			

			// save a few customers
			UserRepository.save(jack);
			UserRepository.save(chloe);
			UserRepository.save(kim);
			UserRepository.save(david);
			UserRepository.save(michelle);

			// //Create schedules for employees
			// ArrayList<User_model> employees = new ArrayList<User_model>();
			// employees.add(chloe);
			// employees.add(jack);
			// ArrayList<Schedule> schedules = new ArrayList<Schedule>();
			// Schedule s1 = new Schedule(LocalDateTime.parse("1999-01-01 12:30", DateTimeStatic.getFormatter()),
			// 							LocalDateTime.parse("1999-01-01 13:30", DateTimeStatic.getFormatter()));
			// ScheduleRepository.save(s1);
			// Schedule s2 = new Schedule(LocalDateTime.parse("1999-01-01 13:30", DateTimeStatic.getFormatter()),
			// 							LocalDateTime.parse("1999-01-01 14:30", DateTimeStatic.getFormatter()));
			// ScheduleRepository.save(s2);
			// schedules.add(s1);
			// schedules.add(s2);
			// UsrService.addSchedulesToEmployees(employees, schedules);

			// Save And Print Booking Data
			// BookingRepository.save(new Booking(LocalDateTime.parse("1999-01-01 12:30", formatter), LocalDateTime.parse("1999-01-01 12:30", formatter), customers, employees));

			// Save Services
			Entity_Service falafel = new Entity_Service("Freddie's Falafels");
			Entity_Service hotDogs = new Entity_Service("Joe's HotDogs");
			Entity_Service service3 = new Entity_Service("Service3");
			Entity_Service service4 = new Entity_Service("Service4");
			Entity_Service service5 = new Entity_Service("Service5");
			ServiceRepository.save(falafel);
			ServiceRepository.save(hotDogs);
			ServiceRepository.save(service3);
			ServiceRepository.save(service4);
			ServiceRepository.save(service5);

			// //Give the employees services
			// ArrayList<User_model> hotdogUsers = new ArrayList<User_model>();
			// ArrayList<Entity_Service> servicesOne = new ArrayList<Entity_Service>();
			// ArrayList<User_model> falafelUsers = new ArrayList<User_model>();
			// ArrayList<Entity_Service> servicesTwo = new ArrayList<Entity_Service>();
			// hotdogUsers.add(chloe);
			// hotdogUsers.add(jack);
			// hotdogUsers.add(david);
			// servicesOne.add(hotDogs);
			// UsrService.addServicesToEmployees(hotdogUsers, servicesOne);
			// servicesTwo.add(falafel);
			// falafelUsers.add(chloe);
			// UsrService.addServicesToEmployees(falafelUsers, servicesTwo);
      	};
	}
}
